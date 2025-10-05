package gribprocessing.utils;

import calc.GeoPoint;
import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class RoutingUtil {
	public static final int REAL_ROUTING = 0;
	public static final int WHAT_IF_ROUTING = 1;

	private final static NumberFormat DF_2 = new DecimalFormat("##0.00");

	private static RoutingPoint finalDestination = null;
	private static GribHelper.GribConditionData[] wgd = null;
	private static double timeStep = 0D;

	private final static GreatCircle gc = new GreatCircle();
	private static RoutingPoint closest = null;
	private static RoutingPoint finalClosest = null;

	private static int brg = 0;

	private static double smallestDist = Double.MAX_VALUE;

	private static boolean interruptRouting = false;

	private static int getBearing(RoutingPoint center) {
		int brg = 0;
		gc.setStart(new GreatCirclePoint(Math.toRadians(center.getPosition().getLatitude()),
				Math.toRadians(center.getPosition().getLongitude())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(finalDestination.getPosition().getLatitude()),
				Math.toRadians(finalDestination.getPosition().getLongitude())));
//  gc.calculateGreatCircle(10);
//  double gcDistance = Math.toDegrees(gc.getDistance() * 60D);
		GreatCircle.RLData rlRoute = gc.calculateRhumbLine();
		double rlZ = rlRoute.getRv();
		brg = (int) Math.round(Math.toDegrees(rlZ));
		return brg;
	}

	private static int getBearingTo(RoutingPoint center, RoutingPoint dest) {
		int brg = 0;
		gc.setStart(new GreatCirclePoint(Math.toRadians(center.getPosition().getLatitude()),
				Math.toRadians(center.getPosition().getLongitude())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(dest.getPosition().getLatitude()),
				Math.toRadians(dest.getPosition().getLongitude())));
		//  gc.calculateGreatCircle(10);
		//  double gcDistance = Math.toDegrees(gc.getDistance() * 60D);
		GreatCircle.RLData rlData = gc.calculateRhumbLine();
		double rlZ = rlData.getRv();
		brg = (int) Math.round(Math.toDegrees(rlZ));
		return brg;
	}

	public static class RoutingResult {
		RoutingPoint closest;
		List<List<RoutingPoint>> isochronals;
		Map<OutputOption, StringBuffer> bestRoutes; // Content depends on the required output type (TXT, CSV, KML, GPX, JSON).

		public RoutingResult closest(RoutingPoint closest) {
			this.closest = closest;
			return this;
		}
		public RoutingResult isochronals(List<List<RoutingPoint>> isochronals) {
			this.isochronals = isochronals;
			return this;
		}
		public RoutingResult bestRoutes(Map<OutputOption, StringBuffer> bestRoutes) {
			this.bestRoutes = bestRoutes;
			return this;
		}

		public RoutingPoint getClosest() {
			return closest;
		}

		public List<List<RoutingPoint>> getIsochronals() {
			return isochronals;
		}

		public Map<OutputOption, StringBuffer> getBestRoutes() {
			return bestRoutes;
		}
	}

	public static class RoutingResult4JSON {
		List<List<RoutingIsochronPoint>> isochronals;
		Map<OutputOption, StringBuffer> bestRoutes; // Content depends on the required output type (TXT, CSV, KML, GPX, JSON).

		public RoutingResult4JSON isochronals(List<List<RoutingIsochronPoint>> isochronals) {
			this.isochronals = isochronals;
			return this;
		}
		public RoutingResult4JSON bestRoutes(Map<OutputOption, StringBuffer> bestRoutes) {
			this.bestRoutes = bestRoutes;
			return this;
		}

//		public RoutingPoint getClosest() {
//			return this.closest;
//		}

		public List<List<RoutingIsochronPoint>> getIsochronals() {
			return this.isochronals;
		}

		public Map<OutputOption, StringBuffer> getBestRoutes() {
			return this.bestRoutes;
		}
	}

	/**
	 * This is a trick.
	 * @param originalRoutingResult the original routing
	 * @return the shrinked routing
	 */
	public static RoutingResult4JSON transformForJson(RoutingResult originalRoutingResult) {
		RoutingResult4JSON output = new RoutingResult4JSON();
		// Best Route
		output.bestRoutes(originalRoutingResult.getBestRoutes());
		// Isochrons
		List<List<RoutingIsochronPoint>> isochrons = new ArrayList<>();
		originalRoutingResult.getIsochronals().forEach(isochron -> {
			List<RoutingIsochronPoint> newIsochron = new ArrayList<>();
			isochron.forEach(rp -> {
				RoutingIsochronPoint newRP = new RoutingIsochronPoint(rp.getPosition());
				if (rp.getAncestor() != null) {
					newRP.setAncestor(rp.getAncestor().getPosition());
				}
				newIsochron.add(newRP);
			});
			isochrons.add(newIsochron);
		});
		output.isochronals(isochrons);
		return output;
	}

	public static RoutingResult calculateIsochrons(String polarFileName,
	                                               RoutingPoint startFrom,
	                                               RoutingPoint destination,
	                                               List<RoutingPoint> intermediateWP,
	                                               Date fromDate,
	                                               GribHelper.GribConditionData[] gribData,
	                                               double timeInterval,
	                                               int routingForkWidth,
	                                               int routingStep,
	                                               int maxTWS,
	                                               int minTWA,
	                                               boolean stopIfGRIB2old,
	                                               double speedCoeff,
	                                               boolean avoidLand,
	                                               double proximity,
	                                               boolean verbose) throws Exception {
		smallestDist = Double.MAX_VALUE; // Reset, for the next leg
		return calculateIsochrons(
				polarFileName,
				startFrom,
				destination,
				intermediateWP,
				null,
				fromDate,
				gribData,
				timeInterval,
				routingForkWidth,
				routingStep,
				maxTWS,
				minTWA,
				stopIfGRIB2old,
				speedCoeff,
				avoidLand,
				proximity,
				verbose);
	}

	private static RoutingResult calculateIsochrons(String polarFileName,
	                                                RoutingPoint startFrom,
	                                                RoutingPoint destination,
	                                                List<RoutingPoint> intermediateWP,
	                                                List<RoutingPoint> bestRoute,
	                                                Date fromDate,
	                                                GribHelper.GribConditionData[] gribData,
	                                                double timeInterval,
	                                                int routingForkWidth,
	                                                int routingStep,
	                                                int maxTWS,
	                                                int minTWA,
	                                                boolean stopIfGRIB2old,
	                                                double speedCoeff,
	                                                boolean avoidLand,
	                                                double proximity,
	                                                boolean verbose) throws Exception {
		PolarHelper polarHelper = new PolarHelper(polarFileName);
		wgd = gribData;
		finalDestination = destination; // By default
		timeStep = timeInterval;
		closest = null;
		finalClosest = null;

		RoutingPoint center = startFrom;

		int nbIntermediateIndex = 0;
		if (intermediateWP != null) {
			finalDestination = intermediateWP.get(nbIntermediateIndex++);
		}
		double gcDistance = 0D;
		RoutingPoint aimFor = null;
		int bestRouteIndex = 0;
		if (bestRoute != null && bestRoute.size() > 0) {
			aimFor = bestRoute.get(++bestRouteIndex);
			System.out.println("Aiming for [" + aimFor.getPosition() + "], " + (new Date()).toString());
		}

//  System.out.println("Starting routing from " + center.getPosition().toString() + " to " + destination.getPosition().toString());

		// Calculate bearing to destination (from start)
		if (aimFor == null) {
			brg = getBearing(center);
		} else {
			brg = getBearingTo(center, aimFor);
		}
		List<List<RoutingPoint>> allIsochrons = new ArrayList<>();

		// Initialization
		interruptRouting = false;
		timer = System.currentTimeMillis();

		smallestDist = Double.MAX_VALUE;
		List<List<RoutingPoint>> data = new ArrayList<>(1);
		ArrayList<RoutingPoint> one = new ArrayList<>(1);
		center.setDate(fromDate);
		GribHelper.GribCondition wind = GribHelper.gribLookup(center.getPosition(), wgd, fromDate);
		boolean keepLooping = true;
		boolean interruptedBecauseTooOld = false;
		if (wind != null && wind.comment != null && wind.comment.equals("TOO_OLD")) {
			center.setGribTooOld(true);
			// System.out.println("Stop if GRIB too old:" + stopIfGRIB2old);
			if (stopIfGRIB2old) {
				keepLooping = false;
				interruptedBecauseTooOld = true;
				System.err.println("Routing aborted. GRIB exhausted (preference)."); // TODO Better logging
			}
		}
		one.add(center); // Initialize data with the center. One point only.
		data.add(one);

		Date currentDate = fromDate; // new Date(fromDate.getTime() + (long)(timeStep * 3600D * 1000D));
		Date arrivalDate = null;
		// synchronized (allIsochrons)
		{
			// Start from "center"
			while (keepLooping && !interruptRouting) {
		        // timer = logDiffTime(timer, "Milestone 1");
				double localSmallOne = Double.MAX_VALUE;
				List<List<RoutingPoint>> temp = new ArrayList<>();
				Iterator<List<RoutingPoint>> dimOne = data.iterator();
				int nbNonZeroSpeed = 0;
				boolean metLand = false;
				boolean allowOtherRoute = false;
				long before = System.currentTimeMillis();
				while (!interruptRouting && dimOne.hasNext() && keepLooping) {
			        // timer = logDiffTime(timer, "Milestone 2");
					List<RoutingPoint> curve = dimOne.next();
					Iterator<RoutingPoint> dimTwo = curve.iterator();
					nbNonZeroSpeed = 0;
					metLand = false;
					while (!interruptRouting && keepLooping && dimTwo.hasNext()) {
			            // timer = logDiffTime(timer, "Milestone 3");
						RoutingPoint newCurveCenter = dimTwo.next();
						List<RoutingPoint> oneCurve = new ArrayList<>(10);

						wind = GribHelper.gribLookup(newCurveCenter.getPosition(), wgd, currentDate);
						if (wind != null && wind.comment != null && wind.comment.equals("TOO_OLD")) {
							center.setGribTooOld(true);
							// System.out.println("Stop if GRIB too old:" + stopIfGRIB2old);
							if (stopIfGRIB2old) {
								keepLooping = false;
								interruptedBecauseTooOld = true;
								System.err.println("Routing aborted. GRIB exhausted (preference).");
							}
						}
				        // timer = logDiffTime(timer, "Milestone 4");

						// brg = getBearing(newCurveCenter); // 7-apr-2010.
						if (aimFor == null) {
							brg = getBearing(newCurveCenter);
						} else { // Finer Routing
							brg = getBearingTo(newCurveCenter, aimFor);
						}
						// nbNonZeroSpeed = 0;
						// Calculate isochron from center
						for (int bearing = brg - routingForkWidth / 2;
						     keepLooping && !interruptRouting && bearing <= brg + routingForkWidth / 2;
						     bearing += routingStep) {
			                // timer = logDiffTime(timer, "Milestone 5");
							int windDir = 0;
							if (wind != null) {
								windDir = wind.winddir;
							} else {
//              Context.getInstance().fireLogging("Wind is null..., aborting (out of the GRIB)\n");
//              System.out.println("Aborting routing from " + center.getPosition().toString() + " to " + destination.getPosition().toString()+ ", wind is null.");
								//      keepLooping = false;
								continue;
							}
							int twa;
							twa = bearing - windDir;
							while (twa < 0) {
								twa += 360;
							}
							double wSpeed = 0.0D;
							if (wind != null) { // Should be granted already...
								wSpeed = wind.windspeed;
							}
							// In case user said to avoid TWS > xxx
							if (maxTWS > -1) {
								if (wSpeed > maxTWS) {
//                Context.getInstance().fireLogging("Avoiding too much wind (" + GnlUtilities.XXXX12.format(wSpeed) + " over " + Integer.toString(maxTWS) + ")\n");
//                System.out.println(".", LoggingPanel.RED_STYLE); // Takes a long time!
									wSpeed = 0;
									allowOtherRoute = true;
									continue;
								}
							}
							double speed = 0D;
							if (minTWA > -1 && twa < minTWA || twa > (360 - minTWA)) {
//              Context.getInstance().fireLogging("Avoiding too close wind (" + Integer.toString(twa) + " below " + Integer.toString(minTWA) + ")\n");
//              System.out.println(".", LoggingPanel.RED_STYLE); // Takes a long time!
								speed = 0D;
								allowOtherRoute = true;
								continue; // Added 22-Jun-2009
							} else {
//              if (minTWA > -1)
//                System.out.println(".", LoggingPanel.GREEN_STYLE); // Takes a long time!
								speed = polarHelper.getSpeed(wSpeed, twa, speedCoeff);
							}

							if (speed < 0.0D) {
								speed = 0.0D;
							}

							if (speed > 0D) {
								nbNonZeroSpeed++;
								double dist = timeInterval * speed;
								arrivalDate = new Date(currentDate.getTime() + (long) (timeStep * 3_600D * 1_000D));
								GreatCirclePoint dr = GreatCircle.dr(new GreatCirclePoint(Math.toRadians(newCurveCenter.getPosition().getLatitude()),
												Math.toRadians(newCurveCenter.getPosition().getLongitude())),
										dist,
										bearing);
								GeoPoint forecast = new GeoPoint(Math.toDegrees(dr.getL()), Math.toDegrees(dr.getG()));
								// System.out.println("Routing point [" + forecast.toString() + "] in " + (World.isInLand(forecast)?"land <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<":"the water"));
								// Avoid the land
								// if (avoidLand && (World.isInLand(forecast) || World.isRouteCrossingLand(newCurveCenter.getPosition(), forecast) != null))
								if (avoidLand && World.isInLand(forecast)) {
									// System.out.println("..........................Avoiding land...");
									metLand = true;
									speed = 0D;
									allowOtherRoute = true;
									nbNonZeroSpeed--;
									continue;
								}

								Point forecastPoint = new Point((int) Math.round(forecast.getLongitude() * 1_000), (int) Math.round(forecast.getLatitude() * 1_000));
								RoutingPoint ip = new RoutingPoint(forecastPoint);

								// Add to Data
								ip.setPosition(forecast);
								ip.setAncestor(newCurveCenter);
								ip.setBsp(speed);        // Speed from the center
								ip.setHdg(bearing);      // Bearing from the center
								ip.setTwa(twa);          // twa from center
								ip.setTws(wSpeed);       // tws from center
								ip.setTwd(windDir);      // twd from center
								ip.setDate(arrivalDate); // arrival date at this point
								if (wind != null && wind.comment != null && wind.comment.equals("TOO_OLD")) {
									ip.setGribTooOld(true);
								}
								oneCurve.add(ip);
							}
				            // timer = logDiffTime(timer, "Milestone 6");
						}
				        // timer = logDiffTime(timer, "Milestone 7");
						if (!interruptRouting) {
							temp.add(oneCurve);
						}
					}
				}
				// long after = System.currentTimeMillis();
				// System.out.println("Isochron calculated in " + Long.toString(after - before) + " ms.");
				// Start from the finalCurve, the previous envelope, for the next calculation
				// Flip data
			    // timer = logDiffTime(timer, "Milestone 8");
				data = temp;
				List<RoutingPoint> finalCurve = null;
				if (!interruptRouting) {
			        // timer = logDiffTime(timer, "Milestone 8-bis");
//        System.out.println("Reducing...");
//        before = System.currentTimeMillis();
					finalCurve = calculateEnvelope(data, center, verbose);
					// System.out.printf(">> Envelope: %d points.\n", finalCurve.size());
					if (aimFor != null) {
						if (isPointIn(aimFor, finalCurve, center)) {
							try {
								aimFor = bestRoute.get(++bestRouteIndex);
							} catch (IndexOutOfBoundsException ioobe) {
								aimFor = finalDestination;
							}
							System.out.println("Aiming for [" + aimFor.getPosition() + "]");
							// localSmallOne = Double.MAX_VALUE;
							smallestDist = Double.MAX_VALUE; // Reset, for the next leg
						}
					}
//        System.out.println("Reducing completed in " + Long.toString(System.currentTimeMillis() - before) + " ms\n");
//        System.out.println(" completed in " + Long.toString(System.currentTimeMillis() - before) + " ms\n");
				}
				// Calculate distance to destination, from the final curve
				Iterator<RoutingPoint> finalIterator = null;
      			// timer = logDiffTime(timer, "Milestone 9");
				if (finalCurve != null) {
					try {
						finalIterator = finalCurve.iterator();
					} catch (Exception ex) {
						if (!interruptRouting) {
							ex.printStackTrace();
						}
					}
				}
				// System.out.println("finalIterator.hasNext() : [" + finalIterator.hasNext() + "]");
				while (!interruptRouting && finalIterator != null && finalIterator.hasNext()) {
					// timer = logDiffTime(timer, "Milestone 10");
					RoutingPoint forecast = finalIterator.next();
					gc.setStart(new GreatCirclePoint(Math.toRadians(forecast.getPosition().getLatitude()),
							Math.toRadians(forecast.getPosition().getLongitude())));
					if (aimFor == null) {
						gc.setArrival(new GreatCirclePoint(Math.toRadians(finalDestination.getPosition().getLatitude()),
								Math.toRadians(finalDestination.getPosition().getLongitude())));
					} else {
						gc.setArrival(new GreatCirclePoint(Math.toRadians(aimFor.getPosition().getLatitude()),
								Math.toRadians(aimFor.getPosition().getLongitude())));
					}
					try {
						gc.calculateGreatCircle(10);
						gcDistance = Math.toDegrees(gc.getDistance() * 60D);
						if (gcDistance < localSmallOne) {
							localSmallOne = gcDistance;
							closest = forecast;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				// timer = logDiffTime(timer, "Milestone 11");
				// System.out.println("Local:" + localSmallOne + ", Smallest:" + smallestDist);
				if (localSmallOne < smallestDist) {
					smallestDist = localSmallOne;
					finalClosest = closest;
					// System.out.println("Still progressing...\n");
				} else if (localSmallOne == smallestDist) {
					// Not progressing
					keepLooping = false;
					System.out.println("Not progressing (stuck at " + DF_2.format(smallestDist) + " nm), aborting.");
				} else if (Math.abs(localSmallOne - smallestDist) < (smallestDist * 0.9)) {
					// When tacking for example... TODO Explore that one.
					if (verbose) {
						System.out.println("Corner case... localSmallOne:" + DF_2.format(localSmallOne) + ", smallesrDist:" + DF_2.format(smallestDist));
					}
				} else {
					keepLooping = false;
					if (verbose) {
						System.out.println("Destination reached? aiming WP [" + (aimFor != null ? aimFor.getPosition().toString() : "none") + "] finalDestination [" + finalDestination.getPosition().toString() + "]");
						System.out.println("LocalSmallOne:" + DF_2.format(localSmallOne));
						System.out.println("SmallestDistance:" + DF_2.format(smallestDist));
					}
					if ((allowOtherRoute && nbNonZeroSpeed == 0) || metLand) {
						keepLooping = true; // Try again, even if the distance was not shrinking
						// smallestDist = localSmallOne;
						if (metLand) {
							System.out.println("--------------- Try again, maybe met land. (smallest:" + DF_2.format(smallestDist) + ", local:" + DF_2.format(localSmallOne) + ", prox:" + proximity + ") --------------");
							// JOptionPane.showMessageDialog(null, "Met Land?", "Bing", JOptionPane.PLAIN_MESSAGE);
							if (smallestDist < proximity) {
								keepLooping = false;
								System.out.println("Close enough. Done.");
							} else {
								smallestDist *= 1.5; // Boo... Should do some backtracking
							}
						}
						smallestDist = localSmallOne;
					}
					if (localSmallOne != Double.MAX_VALUE) {
						if (intermediateWP != null) {
							smallestDist = Double.MAX_VALUE; // Reset, for the next leg
							keepLooping = true;
							finalCurve = new ArrayList<>();
							finalCurve.add(closest);
							center = closest;
							center.setDate(currentDate);

							if (nbIntermediateIndex < intermediateWP.size()) {
								finalDestination = intermediateWP.get(nbIntermediateIndex++);
							} else {
								if (!finalDestination.getPosition().equals(destination.getPosition())) {
									finalDestination = destination;
								} else {
									keepLooping = false;
									if (verbose) {
										System.out.println("Destination reached, aiming (inter-WP) [" + (aimFor != null ? aimFor.getPosition().toString() : "none") + "] finalDestination [" + finalDestination.getPosition().toString() + "]");
									}
								}
							}
						}
						if (!keepLooping && verbose) { // End of Routing
							System.out.println("Finished (" + DF_2.format(smallestDist) + " vs " + DF_2.format(localSmallOne) +
									").\n(Nb Non Zero Speed points:" + NumberFormat.getInstance().format(nbNonZeroSpeed) + ")");
						}
						if (nbNonZeroSpeed == 0) {
							if (interruptedBecauseTooOld) {
								System.err.printf("GRIB exhausted, %d isochrons\n", allIsochrons.size());
							} else {
								System.err.printf("Routing aborted after %d isochrons\n", allIsochrons.size());
							}
						}
					} else {
						if (interruptedBecauseTooOld) {
							System.err.printf("GRIB exhausted, %d isochrons\n", allIsochrons.size());
						} else {
							System.err.printf("Routing aborted after %d isochrons\n", allIsochrons.size());
						}
					}
				}
				allowOtherRoute = false;

				// timer = logDiffTime(timer, "Milestone 12");
				if (keepLooping) {
					allIsochrons.add(finalCurve);
					data = new ArrayList<>();
					data.add(finalCurve);
					currentDate = arrivalDate;
				}
				if (verbose) {
					System.out.println("Isochrone # " + Integer.toString(allIsochrons.size()) + ", smallest distance to arrival:" +
							DF_2.format(smallestDist) + " nm. " + (keepLooping ? "Still processing." : "Done processing."));
				}
				// timer = logDiffTime(timer, "Milestone 13");
			}
			if (interruptRouting) {
				logDiffTime(timer, "Routing interrupted.");
				// System.out.println("Routing interrupted.");
				System.err.println("Routing aborted on user's request.");
			}
		}
		// timer = logDiffTime(timer, "Milestone 14");
		RoutingResult result = new RoutingResult().isochronals(allIsochrons).closest(finalClosest);
		// timer = logDiffTime(timer, "Milestone 15");
		return result;
	}

	// TODO See when this is used
	public static List<List<RoutingPoint>> refineRouting(String polarFileName,
	                                                     RoutingPoint startFrom,
	                                                     RoutingPoint destination,
	                                                     List<List<RoutingPoint>> previousIsochrons,
	                                                     RoutingPoint closestPoint,
	                                                     List<RoutingPoint> intermediateWP,
	                                                     Date fromDate,
	                                                     GribHelper.GribConditionData[] gribData,
	                                                     double timeInterval,
	                                                     int routingForkWidth,
	                                                     int routingStep,
	                                                     int maxTWS,
	                                                     int minTWA,
	                                                     boolean stopIfGRIB2old,
	                                                     double speedCoeff,
	                                                     boolean verbose) throws Exception {
		smallestDist = Double.MAX_VALUE; // Reset, for the next leg
		List<RoutingPoint> bestRoute = RoutingUtil.getBestRoute(closestPoint, previousIsochrons);
		// The route goes from destination to origin. Revert it.
		bestRoute = revertList(bestRoute);

//    for (RoutingPoint rp : bestRoute)
//      System.out.println("Best : " + rp.getPosition().toString());

		return calculateIsochrons(
				polarFileName,
				startFrom,
				destination,
				intermediateWP,
				bestRoute,
				fromDate,
				gribData,
				timeInterval,
				routingForkWidth,
				routingStep,
				maxTWS,
				minTWA,
				stopIfGRIB2old,
				speedCoeff,
				false,                     // TASK Tossion.
				25.0,
				verbose).isochronals;
	}

	public static <T> List<T> revertList(List<T> list) {
		List<T> inverted = new ArrayList<>(list.size());
		int listSize = list.size();
		for (int i = listSize - 1; i >= 0; i--) {
			inverted.add(list.get(i));
		}
		return inverted;
	}

	public static List<RoutingPoint> getBestRoute(RoutingPoint closestPoint, List<List<RoutingPoint>> allIsochrons) {
		List<RoutingPoint> bestRoute = new ArrayList<>(allIsochrons.size());
		boolean go = true;
		RoutingPoint start = closestPoint;
		bestRoute.add(start);
		while (go) {
			RoutingPoint next = start.getAncestor();
			if (next == null) {
				go = false;
			} else {
				bestRoute.add(next);
				start = next;
			}
		}
		return bestRoute;
	}

	public static RoutingPoint getClosestRoutingPoint(List<List<RoutingPoint>> isochrons,
	                                                  int draggedIsochronIdx,
	                                                  GeoPoint mousePosition) {
		RoutingPoint rp = null;
		List<RoutingPoint> isochron = isochrons.get(isochrons.size() - draggedIsochronIdx - 1);
		// System.out.println("Dragged isochron has " + isochron.size() + " point(s)");
		double minDist = Double.MAX_VALUE;
		for (RoutingPoint lrp : isochron) {
			double dist = GreatCircle.getDistanceInNM(new GreatCirclePoint(lrp.getPosition()), new GreatCirclePoint(mousePosition));
			if (dist < minDist) {
				minDist = dist;
				rp = lrp;
			}
		}
//  System.out.println("Smallest distance is " + minDist);
		return rp;
	}

	private static long timer = 0L;

	private static long logDiffTime(long before, String mess) {
		long after = System.currentTimeMillis();
		System.out.println(mess + " (" + Long.toString(after - before) + " ms)");
		return after;
	}

	// Possible optimization ?
	private static List<RoutingPoint> calculateEnvelope(List<List<RoutingPoint>> bulkPoints, RoutingPoint center, boolean verbose) {
		List<RoutingPoint> returnCurve = new ArrayList<>();
		long before = System.currentTimeMillis();
		// Put ALL the points in the finalCurve
		Iterator<List<RoutingPoint>> dimOne = bulkPoints.iterator();
		while (!interruptRouting && dimOne.hasNext()) {
			List<RoutingPoint> curve = dimOne.next();
			Iterator<RoutingPoint> dimTwo = curve.iterator();
			while (!interruptRouting && dimTwo.hasNext()) {
				RoutingPoint newPoint = dimTwo.next();
				returnCurve.add(newPoint);
			}
		}
		String mess = "Reducing from " + NumberFormat.getInstance().format(returnCurve.size()) + " ";
		int origNum = returnCurve.size();
		// Calculate final curve - Here is the skill
		dimOne = bulkPoints.iterator();
		while (!interruptRouting && dimOne.hasNext()) {
			Polygon currentPolygon = new Polygon();
			currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // center
			List<RoutingPoint> curve = dimOne.next();
			Iterator<RoutingPoint> dimTwo = curve.iterator();
			while (!interruptRouting && dimTwo.hasNext()) {
				RoutingPoint newPoint = dimTwo.next();
				currentPolygon.addPoint(newPoint.getPoint().x,
						newPoint.getPoint().y);
			}
			currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // close
			Iterator<List<RoutingPoint>> dimOneBis = bulkPoints.iterator();
			while (!interruptRouting && dimOneBis.hasNext()) {
				List<RoutingPoint> curveBis = dimOneBis.next();
				if (curveBis.equals(curve)) {
					continue;
				}
				Iterator<RoutingPoint> dimTwoBis = curveBis.iterator();
				while (!interruptRouting && dimTwoBis.hasNext()) {
					RoutingPoint isop = dimTwoBis.next();
					if (currentPolygon.contains(isop.getPoint())) {
						// Remove from the final Curve if it's inside (and not removed already)
						// if (returnCurve.contains(isop.getPoint())) // Demanding...
						{
							returnCurve.remove(isop.getPoint());
							// System.out.println("Removing point, len now " + returnCurve.size());
						}
					}
				}
			}
		}
		long after = System.currentTimeMillis();
		int finalNum = returnCurve.size();
		float ratio = 100f * (float) (origNum - finalNum) / (float) origNum;
		if (verbose) {
			System.out.println(mess + "to " + NumberFormat.getInstance().format(returnCurve.size()) + " point(s) (gained " + ratio + "%), curve reduction calculated in " + NumberFormat.getInstance().format(after - before) + " ms");
		}

		return returnCurve;
	}

	private static boolean isPointIn(RoutingPoint rp, List<RoutingPoint> lrp, RoutingPoint center) {
		Polygon currentPolygon = new Polygon();
		currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // center
		for (RoutingPoint p : lrp)
			currentPolygon.addPoint(p.getPoint().x, p.getPoint().y);
		currentPolygon.addPoint(center.getPoint().x, center.getPoint().y); // close
		return currentPolygon.contains(rp.getPoint());
	}

	public static void interruptRoutingCalculation() {
		System.out.println("Interrupting the routing.");
		timer = System.currentTimeMillis();
		interruptRouting = true;
	}

//	private static Pattern pattern = null;
//	private static Matcher matcher = null;

	// TODO What-If routing

	public enum OutputOption {
		CSV, GPX, TXT, KML, JSON
	}

	public final static SimpleDateFormat SDF_DMY  = new SimpleDateFormat("d MMM yyyy");
	public final static DecimalFormat DF2    = new DecimalFormat("00");
	public final static DecimalFormat DF3    = new DecimalFormat("000");
	public final static DecimalFormat XX22   = new DecimalFormat("##00.00");
	public final static DecimalFormat XXX12  = new DecimalFormat("###0.00");

	static {
		SDF_DMY.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	public static StringBuffer outputRouting(GeoPoint from,
											 GeoPoint to,
											 RoutingPoint closestPoint,
											 List<List<RoutingPoint>> allCalculatedIsochrons,
											 OutputOption outputOption) {
		return outputRouting(from, to, closestPoint, allCalculatedIsochrons, Arrays.asList(new OutputOption[] {outputOption})).get(outputOption);
	}

	public static Map<OutputOption, StringBuffer> outputRouting(GeoPoint from,
															    GeoPoint to,
															    RoutingPoint closestPoint,
															    List<List<RoutingPoint>> allCalculatedIsochrons,
															    List<OutputOption> outputOptions) {

		Map<OutputOption, StringBuffer> allOutputs = new HashMap<>();
		outputOptions.forEach(opt -> {
			allOutputs.put(opt, new StringBuffer());
		});


		String kmlPlaces = "";
		String kmlRoute = "";
		int firstKMLHeading = -1;

		// Reverse, for the clipboard
		boolean generateGPXRoute = true;
		// StringBuffer output = new StringBuffer();
		// Opening tags
		if (outputOptions.contains(OutputOption.CSV)) {
			allOutputs.get(OutputOption.CSV).append("L;(dec L);G;(dec G);Date(MDY);UTC;TWS;TWD;BSP;HDG\n");
		}
		if (outputOptions.contains(OutputOption.GPX)) {
			allOutputs.get(OutputOption.GPX).append(
					"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
							"<gpx version=\"1.1\" \n" +
							"     creator=\"OpenCPN\" \n" +
							"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
							"  xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
							"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n" +
							"  xmlns:opencpn=\"http://www.opencpn.org\">\n");
			if (generateGPXRoute) {
				Date d = new Date();
				allOutputs.get(OutputOption.GPX).append("  <rte>\n" +
						"    <name>Weather Wizard route (" + SDF_DMY.format(d) + ")</name>\n" +
						"    <extensions>\n" +
						"      <opencpn:start>" + from.toString() + "</opencpn:start>\n" +
						"      <opencpn:end>" + to.toString() + "</opencpn:end>\n" +
						"      <opencpn:viz>1</opencpn:viz>\n" +
						"      <opencpn:guid>" + UUID.randomUUID().toString() + "</opencpn:guid>\n" +
						"    </extensions>\n" +
						"    <type>Routing</type>\n" +
						"    <desc>Routing from Weather Wizard (generated " + d.toString() + ")</desc>\n" +
						"    <number>" + (d.getTime()) + "</number>\n");
			}
		}
		if (outputOptions.contains(OutputOption.TXT)) {
			Date d = new Date();
			allOutputs.get(OutputOption.TXT).append("Weather Wizard route (" + SDF_DMY.format(d) + ") generated " + d.toString() + ")\n");
		}
		if (outputOptions.contains(OutputOption.KML)) {
			Date d = new Date();
			allOutputs.get(OutputOption.KML).append(
					"<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
							"<kml xmlns=\"http://earth.google.com/kml/2.0\" \n" +
							"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
							"     xsi:schemaLocation=\"http://earth.google.com/kml/2.0 ../xsd/kml21.xsd\">\n" +
							"   <Document>\n" +
							"      <name>Weather Wizard route (" + SDF_DMY.format(d) + ")</name>\n"); // TASK Add from/to
		}
		if (outputOptions.contains(OutputOption.JSON)) {
			// output.append("{\n" + "  \"waypoints\": [\n");
			allOutputs.get(OutputOption.JSON).append("{\"waypoints\": [");
		}

		if (closestPoint != null && allCalculatedIsochrons != null) {
			Calendar cal = new GregorianCalendar();
			List<RoutingPoint> bestRoute = RoutingUtil.getBestRoute(closestPoint, allCalculatedIsochrons);
			int routeSize = bestRoute.size();
			String dateCSV = "", timeCSV = "", dateGPX = "", dateTXT_KML = "", dateJSON = "";
			RoutingPoint rp = null;
			RoutingPoint ic = null; // Isochron Center
			// for (int r=0; r<routeSize; r++) // 0 is the closest point, the last calculated
			for (int r = routeSize - 1; r >= 0; r--) { // 0 is the closest point, the last calculated
				rp = bestRoute.get(r);
				if (r == 0) { // Last one
					ic = rp;
				} else {
					ic = bestRoute.get(r - 1);
				}
				if (rp.getDate() == null) {
					dateCSV = timeCSV = dateGPX = dateTXT_KML = dateJSON = "";
				} else {
					cal.setTime(rp.getDate());

					int year = cal.get(Calendar.YEAR);
					int month = cal.get(Calendar.MONTH);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					int hours = cal.get(Calendar.HOUR_OF_DAY);
					int minutes = cal.get(Calendar.MINUTE);
					int seconds = cal.get(Calendar.SECOND);
					if (outputOptions.contains(OutputOption.CSV)) {
						dateCSV = DF2.format(month + 1) + "/" + DF2.format(day) + "/" + Integer.toString(year);
						timeCSV = DF2.format(hours) + ":" + DF2.format(minutes);
					}
					if (outputOptions.contains(OutputOption.GPX)) {
						dateGPX = Integer.toString(year) + "-" +
								DF2.format(month + 1) + "-" +
								DF2.format(day) + "T" +
								DF2.format(hours) + ":" +
								DF2.format(minutes) + ":" +
								DF2.format(seconds) + "Z";
					}
					if (outputOptions.contains(OutputOption.TXT) || outputOptions.contains(OutputOption.KML)) {
						dateTXT_KML = rp.getDate().toString();
					}
					if (outputOptions.contains(OutputOption.JSON)) {
						dateJSON = Integer.toString(year) + "-" +
								DF2.format(month + 1) + "-" +
								DF2.format(day) + "T" +
								DF2.format(hours) + ":" +
								DF2.format(minutes) + ":" +
								DF2.format(seconds) + "Z";
					}
				}
				// Route points
				if (outputOptions.contains(OutputOption.CSV)) {
					String lat = GeomUtil.decToSex(rp.getPosition().getLatitude(), GeomUtil.SWING, GeomUtil.NS);
					String lng = GeomUtil.decToSex(rp.getPosition().getLongitude(), GeomUtil.SWING, GeomUtil.EW);
					String tws = XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());

					allOutputs.get(OutputOption.CSV).append(lat + ";" +
							Double.toString(rp.getPosition().getLatitude()) + ";" +
							lng + ";" +
							Double.toString(rp.getPosition().getLongitude()) + ";" +
							dateCSV + ";" +
							timeCSV + ";" +
							tws + ";" +
							twd + ";" +
							bsp + ";" +
							hdg + "\n");
				}
				if (outputOptions.contains(OutputOption.GPX)) {
					if (generateGPXRoute) {
						NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
						nf.setMaximumFractionDigits(2);
						allOutputs.get(OutputOption.GPX).append(
								 "       <rtept lat=\"" + rp.getPosition().getLatitude() + "\" lon=\"" + rp.getPosition().getLongitude() + "\">\n" +
										"            <name>" + DF3.format(routeSize - r) + "_WW</name>\n" +
										"            <desc>Waypoint " + Integer.toString(routeSize - r) + ";VMG=" + nf.format(ic.getBsp()) + ";</desc>\n" +
										//  "            <sym>triangle</sym>\n" +
										"            <sym>empty</sym>\n" +
										"            <type>WPT</type>\n" +
										"            <extensions>\n" +
										"                <opencpn:prop>A,0,1,1,1</opencpn:prop>\n" +
										"                <opencpn:viz>1</opencpn:viz>\n" +
										"                <opencpn:viz_name>0</opencpn:viz_name>\n" +
										"            </extensions>\n" +
										"        </rtept>\n");
					} else {
						allOutputs.get(OutputOption.GPX).append(
								 "  <wpt lat=\"" + rp.getPosition().getLatitude() + "\" lon=\"" + rp.getPosition().getLongitude() + "\">\n" +
										"    <time>" + dateGPX + "</time>\n" +
										"    <name>" + DF3.format(r) + "_WW</name>\n" +
										"    <sym>triangle</sym>\n" +
										"    <type>WPT</type>\n" +
										"    <extensions>\n" +
										"            <opencpn:guid>142646-1706866-1264115693</opencpn:guid>\n" +
										"            <opencpn:viz>1</opencpn:viz>\n" +
										"            <opencpn:viz_name>1</opencpn:viz_name>\n" +
										"            <opencpn:shared>1</opencpn:shared>\n" +
										"    </extensions>\n" +
										"  </wpt>\n");
					}
				}
				if (outputOptions.contains(OutputOption.TXT)) {
					String tws = XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					allOutputs.get(OutputOption.TXT).append(rp.getPosition().toString() + " : " + dateTXT_KML + ", tws:" + tws + ", twd:" + twd + ", bsp:" + bsp + ", hdg:" + hdg + "\n");
				}
				if (outputOptions.contains(OutputOption.KML)) {
					if (firstKMLHeading == -1) {
						firstKMLHeading = ic.getHdg();
					}
					kmlRoute += (rp.getPosition().getLongitude() + "," + rp.getPosition().getLatitude() + ",0\n");
					String tws = XX22.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = XX22.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					kmlPlaces +=
							("         <Placemark>\n" +
									"           <name>WayPoint " + Integer.toString(routeSize - r) + "</name>\n" +
									"           <description>\n" +
									"            <![CDATA[\n" +
									"              <b>" + dateTXT_KML + "</b>\n" +
									"              <table>\n" +
									"                <tr><td>TWS</td><td>" + tws + " knots</td></tr>\n" +
									"                <tr><td>TWD</td><td>" + twd + "&deg;</td></tr>\n" +
									"                <tr><td>BSP</td><td>" + bsp + " knots</td></tr>\n" +
									"                <tr><td>HDG</td><td>" + hdg + "&deg;</td></tr>\n" +
									"              </table>\n" +
									"            ]]>\n" +
									"           </description>\n" +
									"           <LookAt>\n" +
									"             <longitude>" + rp.getPosition().getLongitude() + "</longitude>\n" +
									"             <latitude>" + rp.getPosition().getLatitude() + "</latitude>\n" +
									"             <range>50000</range>\n" +
									"             <tilt>45</tilt>\n" +
									"             <heading>" + Integer.toString(ic.getHdg()) + "</heading>\n" +
									"           </LookAt>\n" +
									"           <Point>\n" +
									"             <coordinates>" + rp.getPosition().getLongitude() + "," + rp.getPosition().getLatitude() + ",0 </coordinates>\n" +
									"           </Point>\n" +
									"         </Placemark>\n");
				}
				if (outputOptions.contains(OutputOption.JSON)) {
					String tws = XXX12.format(ic.getTws());
					String twd = Integer.toString(ic.getTwd());
					String bsp = XXX12.format(ic.getBsp());
					String hdg = Integer.toString(ic.getHdg());
					/* output.append(
							 "    {\n" +
									"      \"datetime\":\"" + date + "\",\n" +
									"      \"position\": {\n" +
									"                  \"latitude\":\"" + rp.getPosition().getLatitude() + "\",\n" +
									"                  \"longitude\":\"" + rp.getPosition().getLongitude() + "\"\n" +
									"                },\n" +
									"      \"tws\":" + tws + ",\n" +
									"      \"twd\":" + twd + ",\n" +
									"      \"bsp\":" + bsp + ",\n" +
									"      \"hdg\":" + hdg + "\n" +
									"    }" + (r == 0 ? "" : ",") + "\n"); */
					allOutputs.get(OutputOption.JSON).append(
							"{  \"datetime\":\"" + dateJSON + "\"," +
									" \"position\": {" +
									" \"latitude\":\"" + rp.getPosition().getLatitude() + "\"," +
									" \"longitude\":\"" + rp.getPosition().getLongitude() + "\"" +
									" }," +
									" \"tws\":" + tws + "," +
									" \"twd\":" + twd + "," +
									" \"bsp\":" + bsp + "," +
									" \"hdg\":" + hdg + "" +
									" }" + (r == 0 ? "" : ",") );
				}
			}
			// Closing tags
			if (outputOptions.contains(OutputOption.GPX)) {
				if (generateGPXRoute) {
					allOutputs.get(OutputOption.GPX).append("  </rte>\n");
				}
				allOutputs.get(OutputOption.GPX).append("</gpx>");
			}
			if (outputOptions.contains(OutputOption.KML)) {
				allOutputs.get(OutputOption.KML).append(
						 "      <Folder>\n" +
								"         <name>Waypoints</name>\n" +
								kmlPlaces +
								"      </Folder>\n");
				allOutputs.get(OutputOption.KML).append(
						 "      <Placemark>\n" +
								"          <name>Suggested route</name>\n" +
								"          <LookAt>\n" +
								"             <longitude>" + from.getLongitude() + "</longitude>\n" +
								"             <latitude>" + from.getLatitude() + "</latitude>\n" +
								"             <range>100000</range>\n" +
								"             <tilt>45</tilt>\n" +
								"             <heading>" + Integer.toString(firstKMLHeading) + "</heading>\n" +
								"          </LookAt>\n" +
								"          <visibility>1</visibility>\n" +
								"          <open>0</open>\n" +
								"          <Style>\n" +
								"             <LineStyle>\n" +
								"                <width>3</width>\n" +
								"                <color>ff00ffff</color>\n" +
								"             </LineStyle>\n" +
								"             <PolyStyle>\n" +
								"                <color>7f00ff00</color>\n" +
								"             </PolyStyle>\n" +
								"          </Style>\n" +
								"          <LineString>\n" +
								"             <extrude>1</extrude>\n" +
								"             <tessellate>1</tessellate>\n" +
								"             <altitudeMode>clampToGround</altitudeMode>\n" +
								"             <coordinates>\n" +
								kmlRoute +
								"             </coordinates>\n" +
								"          </LineString>\n" +
								"       </Placemark>\n");
				allOutputs.get(OutputOption.KML).append(
						 "       <Snippet><![CDATA[created by <a href=\"http://code.google.com/p/weatherwizard/\">The Weather Wizard</a>]]></Snippet>\n" +
								"   </Document>\n" +
								"</kml>");
			}
			if (outputOptions.contains(OutputOption.JSON)) {
				// output.append("  ]\n" + "}\n");
				allOutputs.get(OutputOption.JSON).append("  ]" + "}");
			}
		}
		return allOutputs;
	}
}