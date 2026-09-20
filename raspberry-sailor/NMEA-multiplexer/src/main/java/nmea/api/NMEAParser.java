package nmea.api;

import nmea.ais.AISParser;
import nmea.parser.StringParsers;
import nmea.utils.MuxNMEAUtils;
import utils.DumpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Controller. Common to everyone, final class.
 *
 * This class is final, and can be used as it is.
 *
 * Its job is to detect potential sentences in the NMEA stream of characters.
 * When a sentence (to be validated) is detected, it broadcasts an NMEAEvent
 * to all the registered NMEAListeners, see the {@link #fireDataDetected(NMEAEvent)} method.
 *
 * The NMEAListeners to register are sent to the constructor. They can also be added later on.
 *
 * Uses System variables:
 *  no.ais true|false
 *  nmea.parser.verbose true|false
 *
 * @author Olivier Le Diouris
 * @version 1.0
 * @see nmea.api.NMEAReader
 * @see nmea.api.NMEAClient
 * @see nmea.api.NMEAEvent
 * @see nmea.api.NMEAException
 */
public final class NMEAParser extends Thread {

	private final static boolean VERBOSE = System.getProperty("nmea.parser.verbose", "false").equals("true");

	private String[] deviceFilters = null;
	private String[] sentenceFilters = null;

	private StringBuffer nmeaStream = new StringBuffer();
	private final static long MAX_STREAM_SIZE = 2_048;
	public final static String STANDARD_NMEA_EOS = new String(new char[]{0x0D, 0x0A}); // "\r\n";

	public final static String NMEA_SENTENCE_SEPARATOR = "\n";

	private List<NMEAListener> NMEAListeners; // new ArrayList(2);

	NMEAParser instance;

	/**
	 * @param al The ArrayList of the Listeners instantiated by the NMEAClient
	 */

	public NMEAParser(List<NMEAListener> al) {
		if (VERBOSE) {
			System.out.println(this.getClass().getName() + ":Creating NMEAParser");
		}
		instance = this;
		NMEAListeners = al;
		this.addNMEAListener(new NMEAListener() {
			public void dataRead(NMEAEvent e) {
      //        System.out.println("Received Data:" + e.getContent());
				nmeaStream.append(e.getContent());
				// Send to parser
				String s = "";
				try {
					while (s != null) {
						s = instance.detectSentence();
						if (s != null && s.length() > 6 && (s.startsWith("$") || s.startsWith(AISParser.AIS_PREFIX))) { // Potentially valid
							// TODO ? RegExp on the full sentence. Maybe not too user friendly...
							boolean broadcast = true;
							if (VERBOSE) {
								System.out.printf("Managing sentence [%s],\nDevice filters [%s]\nSentence Filters [%s]",
										s,
										deviceFilters != null ? Arrays.asList(deviceFilters).stream().collect(Collectors.joining(", ")) : null,
										sentenceFilters != null ? Arrays.asList(sentenceFilters).stream().collect(Collectors.joining(", ")) : null);
							}
							broadcast = MuxNMEAUtils.goesThruFilters(s,
									sentenceFilters != null ? Arrays.asList(sentenceFilters) : null,
									deviceFilters != null ? Arrays.asList(deviceFilters) : null,
									VERBOSE);
							if (false) {  // TODO Remove that block
								// Device filters
								if (deviceFilters != null) {
									String thisId = StringParsers.getDeviceID(s);
									// Negative device filters
									for (String deviceID : deviceFilters) {
										if (deviceID.trim().startsWith("~") && thisId.equals(deviceID.trim().substring(1))) {
											broadcast = false;
											break;
										}
									}
									// Positive device filters
									long pos = Arrays.stream(deviceFilters).filter(id -> !id.trim().startsWith("~")).count();
									if (broadcast && pos > 0) {
										broadcast = false;
										for (String deviceID : deviceFilters) {
											if (!deviceID.trim().startsWith("~") && thisId.equals(deviceID.trim())) {
												broadcast = true;
												break;
											}
										}
									}
								}
								// Sentence filters
								if (broadcast && sentenceFilters != null) {
									String thisId = StringParsers.getSentenceID(s);
									// Negative sentence filters
									for (String sentenceId : sentenceFilters) {
										if (sentenceId.trim().startsWith("~") && thisId.equals(sentenceId.trim().substring(1))) {
											broadcast = false;
											break;
										}
									}
									// Positive sentence filters
									long pos = Arrays.stream(sentenceFilters).filter(id -> !id.trim().startsWith("~")).count();
									if (broadcast && pos > 0) {
										broadcast = false;
										for (String sentenceId : sentenceFilters) {
											if (!sentenceId.trim().startsWith("~") && thisId.equals(sentenceId.trim())) {
												broadcast = true;
												break;
											}
										}
									}
								}
							}
							if (broadcast) {
								if (VERBOSE) {
									System.out.printf("  >>> Broadcasting [%s] <<< \n", s.trim());
								}
								instance.fireDataDetected(new NMEAEvent(this, s));
							} else {
								if (VERBOSE) {
									System.out.printf("  >>> Rejecting [%s] <<< \n", s.trim());
								}
							}
						}
					}
				} catch (NMEAException ne) {
					ne.printStackTrace();
				}
			}
		});
	}

	public String[] getDeviceFilters() {
		return this.deviceFilters;
	}

	public void setDeviceFilters(String[] s) {
		if (s != null && s.length == 1 && s[0].trim().isEmpty()) {
			this.deviceFilters = null;
		} else {
			this.deviceFilters = s;
		}
	}

	public String[] getSentenceFilters() {
		return this.sentenceFilters;
	}

	public void setSentenceFilters(String[] sa) {
		if (sa != null && sa.length == 1 && sa[0].trim().isEmpty()) {
			this.sentenceFilters = null;
		} else {
			this.sentenceFilters = sa;
		}
	}

	public StringBuffer getNmeaStream() {
		return this.nmeaStream;
	}

	public void setNmeaStream(StringBuffer s) {
		this.nmeaStream = s;
	}

	public String detectSentence() throws NMEAException {
		String ret = null;
		try {
			if (interesting()) {
				if (VERBOSE) {
					System.out.println("=== NMEAParser ===");
					DumpUtil.displayDualDump(nmeaStream.toString());
				}
				int start = getSentenceStartIndex(nmeaStream);
				int end = nmeaStream.indexOf(NMEA_SENTENCE_SEPARATOR, start);
				ret = nmeaStream.substring(start, end);
//			nmeaStream = nmeaStream.substring(end + NMEA_SENTENCE_SEPARATOR.length());
				nmeaStream.delete(0, end + NMEA_SENTENCE_SEPARATOR.length());
			} else {
				if (VERBOSE) {
					System.out.printf("Rejecting [%s]\n", nmeaStream.toString().trim());
				}
				if (nmeaStream.length() > MAX_STREAM_SIZE) {
					nmeaStream = new StringBuffer(); // Reset to avoid OutOfMemoryException
				}
				return null; // Not enough info
			}
		} catch (NMEAException e) {
			throw e;
		}
		return ret;
	}

	private static int getSentenceStartIndex(StringBuffer sb) {
		int beginIdx = -1;
		int beginIdxNMEA = sb.indexOf("$");
		// With AIS?
		if (!"true".equals(System.getProperty("no.ais"))) { // Fallback on AIS, condition on system variable "no.ais"
			int beginIdxAIS = sb.indexOf(AISParser.AIS_PREFIX);
			if (beginIdxNMEA == -1) {
				beginIdx = beginIdxAIS;
			} else if (beginIdxAIS == -1) {
				beginIdx = beginIdxNMEA;
			} else {
				beginIdx = Math.min(beginIdxNMEA, beginIdxAIS);
			}
		} else {
			beginIdx = beginIdxNMEA;
		}
		return beginIdx;
	}
	/**
	 * Detects a potentially valid NMEA Sentence
	 * @return true if a potential sentence is detected. false if not.
	 * @throws NMEAException Oops
	 */
	private boolean interesting()
					throws NMEAException {
//    if (nmeaPrefix == null || nmeaPrefix.length() == 0)
//      throw new NMEAException("NMEA Prefix is not set");

//  int beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
		int beginIdx = getSentenceStartIndex(nmeaStream);
		int endIdx = nmeaStream.indexOf(NMEA_SENTENCE_SEPARATOR, (beginIdx > -1 ? beginIdx : 0));

		if (beginIdx == -1 && endIdx == -1) {
			return false; // No beginning, no end !
		}
		if (endIdx > -1 && endIdx < beginIdx) { // Seek the beginning of a sentence
//		nmeaStream = nmeaStream.substring(endIdx + NMEA_SENTENCE_SEPARATOR.length());
			nmeaStream.delete(0, endIdx + NMEA_SENTENCE_SEPARATOR.length());
			beginIdx = getSentenceStartIndex(nmeaStream);
		}

		if (beginIdx == -1) {
			return false;
		} else {
			while (true) {
				try {
					// The stream should here begin with $XX
					if (nmeaStream.length() > 6) { // "$" + prefix + XXX, or "!AIVDM"
						endIdx = nmeaStream.indexOf(NMEA_SENTENCE_SEPARATOR, beginIdx);
						if (endIdx > -1) {
							return true; // Take all
						} else {
							return false; // unfinished sentence
						}
					} else {
						return false; // Not long enough - Not even sentence ID
					}
				} catch (Exception e) {
					System.err.println("Oooch!");
					e.printStackTrace();
					System.err.println("nmeaStream.length = " + nmeaStream.length() + ", Stream:[" + nmeaStream + "]");
				}
			} // End of infinite loop
		}
	}

	protected void fireDataDetected(NMEAEvent e) {
		if (this.NMEAListeners != null) {
			synchronized (this.NMEAListeners) {
				this.NMEAListeners.stream().forEach(listener -> {
					if (listener != null) {
						synchronized (listener) {
							listener.dataDetected(e);
						}
					}
				});
			}
		}
	}

	public synchronized void addNMEAListener(NMEAListener l) {
		if (this.NMEAListeners == null) {
			this.NMEAListeners = new ArrayList<>();
		}
		if (!this.NMEAListeners.contains(l)) {
			this.NMEAListeners.add(l);
		}
	}

	public synchronized void removeNMEAListener(NMEAListener l) {
		if (this.NMEAListeners != null) {
			NMEAListeners.remove(l);
		}
	}

	public void run() {
		if (System.getProperty("nmea.parser.verbose", "false").equals("true")) {
			System.out.println(this.getClass().getName() + ":Parser Running");
		}
	}
}