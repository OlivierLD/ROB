package util.swing;

import nmea.parser.GeoPos;
import util.LogAnalyzer;
import util.LogToPolarPoints;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// https://mathbits.com/MathBits/Java/Graphics/GraphingMethods.htm

public class SwingPanel
		extends JPanel {
	private Color pointColor = Color.red;
	private List<LogAnalyzer.DatedPosition> positions = null;
	private List<LogToPolarPoints.PolarTriplet> ptList = null;
	private SwingPanel instance = this;

	public SwingPanel() {
		this.clear();
	}

	public void setPointColor(Color c) {
		this.pointColor = c;
	}

	public void clear() {
		this.positions = null;
		this.repaint();
	}

	public void plot(List<?> dataList, boolean progressing, Consumer<Object> plotCallback) {

		int from = (progressing ? 0 : dataList.size() - 1);
		Thread plotter = new Thread(() -> {
			try {
				for (int i = from; i < dataList.size(); i++) {
					final List<?> toPlot = IntStream.range(0, i + 1)
							.mapToObj(x -> dataList.get(x))
							.collect(Collectors.toList());
					SwingUtilities.invokeAndWait(() -> {
						if (toPlot.get(0) instanceof LogAnalyzer.DatedPosition) {
							instance.positions = (List<LogAnalyzer.DatedPosition>) toPlot;
						} else if (toPlot.get(0) instanceof LogToPolarPoints.PolarTriplet) {
							instance.ptList = (List<LogToPolarPoints.PolarTriplet>) toPlot;
						}
						instance.repaint();
					});
				}
				if (plotCallback != null) {
					plotCallback.accept(null);
				}
			} catch (InterruptedException | InvocationTargetException ie) {
				ie.printStackTrace();
			}
		}, "plotter");
		plotter.start();
	}

	private final static boolean DEBUG = false;

	@Override
	public void paintComponent(Graphics gr) {
		gr.setColor(Color.white);
		gr.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (this.positions != null) {
			gr.setColor(pointColor);
			// Draw point and / or path
			// 1 - get min and max
//			double minLat = Double.MAX_VALUE,
//					minLng = Double.MAX_VALUE,
//					maxLat = -minLat,
//					maxLng = -minLng;
//			for (LogAnalyzer.DatedPosition pos : positions) {
//				minLat = Math.min(minLat, pos.getPosition().lat);
//				maxLat = Math.max(maxLat, pos.getPosition().lat);
//				minLng = Math.min(minLng, pos.getPosition().lng);
//				maxLng = Math.max(maxLng, pos.getPosition().lng);
//			}
			// TODO Limit to the current buffer size (when animating) ? See 'progressing' boolean in plot method. Use stream().limit .
			final double minLat = positions.stream().mapToDouble(pos -> pos.getPosition().lat).min().orElseThrow(NoSuchElementException::new);
			final double maxLat = positions.stream().mapToDouble(pos -> pos.getPosition().lat).max().orElseThrow(NoSuchElementException::new);
			final double minLng = positions.stream().mapToDouble(pos -> pos.getPosition().lng).min().orElseThrow(NoSuchElementException::new);
			final double maxLng = positions.stream().mapToDouble(pos -> pos.getPosition().lng).max().orElseThrow(NoSuchElementException::new);

			double widthRatio = (double) this.getWidth() / ((maxLng - minLng) * 1.1);
			double heightRatio = (double) this.getHeight() / ((maxLat - minLat) * 1.1);
			final double ratio = Math.min(widthRatio, heightRatio);

			Function<Double, Integer> posLngToCanvas = lng -> {
				int stepOne = (this.getWidth() / 2) + (int) Math.round((lng - minLng) * (ratio * 1.1));
				int stepTwo = stepOne - (this.getWidth() / 2);
				return stepTwo;
			};
			Function<Double, Integer> posLatToCanvas = lat -> {
				int stepOne = (this.getHeight() / 2) - (int) Math.round((lat - minLat) * (ratio * 1.1));
				int stepTwo = stepOne + (this.getHeight() / 2);
				return stepTwo;
			};

			if (DEBUG) {
				System.out.println(String.format("TopLeft: %s, Bottom-Right: %s", new GeoPos(maxLat, minLng), new GeoPos(minLat, maxLng)));
				System.out.println(String.format("Ratio: W: %f, H: %f, r:%f, deltaLat: %f, deltaLng: %f", widthRatio, heightRatio, ratio, (maxLat - minLat), (maxLng - minLng)));
				// Display canvas coordinates of min-max
				int xCanvas = posLngToCanvas.apply(minLng);
				int yCanvas = posLatToCanvas.apply(minLat);
				System.out.println(String.format("\t(MinLat, MinLng) - Plotting %s => x: %d, y=%d (canvas %d x %d)",
						new GeoPos(minLat, minLng).toString(), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
				xCanvas = posLngToCanvas.apply(maxLng);
				yCanvas = posLatToCanvas.apply(minLat);
				System.out.println(String.format("\t(MinLat, MaxLng) - Plotting %s => x: %d, y=%d (canvas %d x %d)",
						new GeoPos(minLat, maxLng).toString(), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
				xCanvas = posLngToCanvas.apply(maxLng);
				yCanvas = posLatToCanvas.apply(maxLat);
				System.out.println(String.format("\t(MaxLat, MaxLng) - Plotting %s => x: %d, y=%d (canvas %d x %d)",
						new GeoPos(maxLat, maxLng).toString(), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
				xCanvas = posLngToCanvas.apply(minLng);
				yCanvas = posLatToCanvas.apply(maxLat);
				System.out.println(String.format("\t(MaxLat, MinLng) - Plotting %s => x: %d, y=%d (canvas %d x %d)",
						new GeoPos(maxLat, maxLng).toString(), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
			}
			// Plot
			final AtomicInteger idx = new AtomicInteger(0);
			positions.stream().forEach(pos -> {
//				int xCanvas = (this.getWidth() / 2) + (int) Math.round((pos.getPosition().lng - _minLng) * (ratio / 2));
//				int yCanvas = (this.getHeight() / 2) - (int) Math.round((pos.getPosition().lat - _minLat) * (ratio / 2));
				int xCanvas = posLngToCanvas.apply(pos.getPosition().lng);
				int yCanvas = posLatToCanvas.apply(pos.getPosition().lat);

				if (DEBUG) {
					System.out.println(String.format("\tPlotting (%d) %s => x: %d, y=%d (canvas %d x %d)",
							idx.get(), pos.getPosition().toString(), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
//					if (idx.get() < 10) {
//						gr.setColor(Color.blue);
//					} else {
//						gr.setColor(pointColor);
//					}
				}
				gr.fillOval(xCanvas - 1, yCanvas - 1, 3, 3);
				idx.set(idx.get() + 1);
			});
		} else if (ptList != null) {
			gr.setColor(pointColor);

			final double minTWA = 0;
			final double maxTWA = 360;
			final double minBSP = 0;
			final double maxBSP = ptList.stream().mapToDouble(data -> data.getBsp()).max().orElseThrow(NoSuchElementException::new);

			double widthRatio = (double) this.getWidth() / ((2 * maxBSP) * 1.1);
			double heightRatio = (double) this.getHeight() / ((2 * maxBSP) * 1.1);
			final double ratio = Math.min(widthRatio, heightRatio);

			Function<Double, Integer> xToCanvas = x -> {
				int stepOne = (this.getWidth() / 2) + (int) Math.round((x - minBSP) * (ratio * 1.1));
				int stepTwo = stepOne; //  - (this.getWidth() / 2);
				return stepTwo;
			};
			Function<Double, Integer> yToCanvas = y -> {
				int stepOne = (this.getHeight() / 2) - (int) Math.round((y - minBSP) * (ratio * 1.1));
				int stepTwo = stepOne; // + (this.getHeight() / 2);
				return stepTwo;
			};

			if (DEBUG) {
				// TODO Do it
			}
			// Plot grid...
			gr.setColor(Color.gray);
			int centerX = xToCanvas.apply(0d) - 1;
			int centerY = yToCanvas.apply(0d) - 1;
			gr.fillOval(centerX, centerY, 3, 3);
			for (int i=1; i<maxBSP; i++) {
				int radius = (int)Math.round(i * (ratio * 1.1));
				// https://mathbits.com/MathBits/Java/Graphics/GraphingMethods.htm
				gr.drawArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, -90, 360);
			}
			gr.drawLine(centerX, centerY, centerX, 0); // Axis

			// Plot Points
			gr.setColor(pointColor);
			final AtomicInteger idx = new AtomicInteger(0);
			ptList.stream().forEach(plt -> {
				double x = plt.getBsp() * Math.sin(Math.toRadians(plt.getTwa()));
				double y = plt.getBsp() * Math.cos(Math.toRadians(plt.getTwa()));
				int xCanvas = xToCanvas.apply(x);
				int yCanvas = yToCanvas.apply(y);

				if (DEBUG) {
					System.out.println(String.format("\tPlotting (%d) %s => x: %d, y=%d (canvas %d x %d)",
							idx.get(), String.format("(BSP:%f, TWA:%f)", plt.getBsp(), plt.getTwa()), xCanvas, yCanvas, this.getWidth(), this.getHeight()));
				}
				gr.fillOval(xCanvas - 1, yCanvas - 1, 3, 3);
				idx.set(idx.get() + 1);
			});
		} else {
			System.out.println(String.format("Size %d x %d", this.getWidth(), this.getHeight()));
		}
	}
}
