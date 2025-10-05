package chartview.util;


import calc.GeoPoint;
import chart.components.util.MercatorUtil;
import chartview.ctx.WWContext;
import com.sun.media.jai.codec.*;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedImageAdapter;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ImageUtil {
    public final static int NO_CHANGE = 0;
    public final static int SHARPEN = 1;
    public final static int BLUR = 2;

    public static BufferedImage switchColor(BufferedImage bi, Color c) {
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                if (!get(bi, i, j).equals(Color.white)) { // Assuming white becomes tranparent
                    set(bi, i, j, c);
                }
            }
        }
        return bi;
    }

    public static double luminance(int r, int g, int b) {
        double lum = (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
        return lum;
    }

    public static double luminance(Color c) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        return luminance(red, green, blue); // TASK Try luminance(c.getRGB())
    }

    public static double luminance(int pixel) {
        int red = (pixel & 0x00ff0000) >> 16;
        int green = (pixel & 0x0000ff00) >> 8;
        int blue = pixel & 0x000000ff;
        return luminance(red, green, blue);
    }

    public static BufferedImage sharpen(BufferedImage bimg) {
        float[] data =
                {-1, -1, -1,
                 -1, 9, -1,
                 -1, -1, -1};
        Kernel kernel = new Kernel(3, 3, data);
        BufferedImageOp op = new ConvolveOp(kernel);
        bimg = op.filter(bimg, null);
        return bimg;
    }

    public static BufferedImage blur(BufferedImage bimg) {
        float[] blurMatrix =
                {0.0625f, 0.125f, 0.0625f,
                 0.125f, 0.25f, 0.125f,
                 0.0625f, 0.125f, 0.0625f};
        Kernel kernel = new Kernel(3, 3, blurMatrix);
        BufferedImageOp blurFilter = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        bimg = blurFilter.filter(bimg, null);
        return bimg;
    }

    public static BufferedImage blur(BufferedImage bimg, int matrixDim) {
        //  System.out.println("Blur dim:" + matrixDim);
        float[] blurMatrix = new float[matrixDim * matrixDim];
        for (int i = 0; i < blurMatrix.length; i++) {
          blurMatrix[i] = 1f / (float) (matrixDim * matrixDim);
        }
        Kernel kernel = new Kernel(matrixDim, matrixDim, blurMatrix);
        BufferedImageOp blurFilter = new ConvolveOp(kernel,
                ConvolveOp.EDGE_NO_OP, // ConvolveOp.EDGE_ZERO_FILL
                null);
        bimg = blurFilter.filter(bimg, null);
        return bimg;
    }

    public static BufferedImage turnColorTransparent(BufferedImage bi, Color c) {
        return turnColorTransparent(bi, c, NO_CHANGE);
    }

    public static BufferedImage turnColorTransparent(BufferedImage bi, Color c, int option) {
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                Color thisColor = get(bi, i, j);
                if (!thisColor.equals(c)) {
                    set(bi, i, j, thisColor);
                }
            }
        }
        if (option == SHARPEN) {
          bi = sharpen(bi);
        } else if (option == BLUR) {
          bi = blur(bi);
        }
        return bi;
    }

    public static void writeImageToFile(Image img,
                                        String formatName,
                                        String fileName) throws Exception {
        BufferedImage bi = null;
        if (img instanceof BufferedImage) {
          bi = (BufferedImage) img;
        } else {
          bi = toBufferedImage(img);
        }
        ImageIO.write(bi, formatName, new File(fileName));
    }

    public static ColorModel getColorModel(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
        ColorModel cm = pg.getColorModel();
        return cm;
    }

    @Deprecated
    public static Image makeTransparentImage(Component comp, Image im) {
        return makeTransparentImage(comp, im, null);
    }

    /**
     * Turns white to transparent
     *
     * @param comp
     * @param im
     * @param switchTo
     * @return
     * @deprecated use switchColorAndMakeColorTransparent instead
     */
    @Deprecated
    public static Image makeTransparentImage(Component comp,
                                             Image im,
                                             Color switchTo) {
        int w = im.getWidth(comp);
        int h = im.getHeight(comp);
        int[] ipix = new int[w * h];
        int[] mpix = new int[w * h];
        boolean gotfg = false;
        boolean gotma = false;

        Image front = im;
        Image back = im;

        if (w == -1 && h == -1) {
            // LOCALIZE (but deprecated)
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(),
                    "Missing Fax!",
                    "Image processing",
                    JOptionPane.WARNING_MESSAGE);
            return im;
        }

        BufferedImage bi = null;
        if (front instanceof BufferedImage) {
          bi = (BufferedImage) front;
        } else {
          bi = toBufferedImage(front);
        }
        if (switchTo != null) {
            // Increase the number of colors, so another than black can be displayed.
            try {
                boolean ok =
                        new PixelGrabber(bi, 0, 0, w, h, ipix, 0, w).grabPixels();
                if (ok) {
                    Image increased = comp.createImage(new MemoryImageSource(w, h, generateColorModel(), ipix, 0, w));
                    bi = toBufferedImage(increased);
                }
            } catch (Exception ie) {
            }
            bi = switchColor(bi, switchTo);
        }

        try {
            // note that there are problems with pixelgrabber from images created with
            // createimage, but apparently not with images from loadimage
            gotfg = new PixelGrabber(bi, 0, 0, w, h, ipix, 0, w).grabPixels();
            gotma = new PixelGrabber(back, 0, 0, w, h, mpix, 0, w).grabPixels();
        } catch (InterruptedException e) {
        }
        if (gotfg && gotma) {
            for (int i = 0; i < w * h; i++) {
                int ma = 0xff - (mpix[i] & 0xff); // center pixel
                int fg = ipix[i];
                ipix[i] = (fg & 0xffffff) | (ma << 24);
            }
            Image fin = null;
            if (comp != null) {
              fin = comp.createImage(new MemoryImageSource(w, h, ipix, 0, w));
            } else {
              fin = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, ipix, 0, w));
            }
            return (fin);
        } else
            WWContext.getInstance().fireLogging("Composite failed");

        return (im);
    }

    @Deprecated
    public static Image turn2TransparentImage(Component comp,
                                              Image im,
                                              Color toTransparent) {
        int w = im.getWidth(comp);
        int h = im.getHeight(comp);
        int[] ipix = new int[w * h];
        int[] mpix = new int[w * h];
        boolean gotfg = false;
        boolean gotma = false;

        Image front = im;
        Image back = im;

        if (w == -1 && h == -1) {
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(),
                    "Missing Fax!",
                    "Image processing",
                    JOptionPane.WARNING_MESSAGE);
            return im;
        }

        BufferedImage bi = null;
        if (front instanceof BufferedImage) {
          bi = (BufferedImage) front;
        } else {
          bi = toBufferedImage(front);
        }
        // Increase the number of colors, so another than black can be displayed.
        try {
            boolean ok =
                    new PixelGrabber(bi, 0, 0, w, h, ipix, 0, w).grabPixels();
            if (ok) {
                Image increased = comp.createImage(new MemoryImageSource(w, h, generateColorModel(), ipix, 0, w));
                bi = toBufferedImage(increased);
            }
        } catch (Exception ie) {
            ie.printStackTrace();
        }
        bi = turnColorTransparent(bi, toTransparent);

        try {
            // note that there are problems with pixelgrabber from images created with
            // createimage, but apparently not with images from loadimage
            gotfg = new PixelGrabber(bi, 0, 0, w, h, ipix, 0, w).grabPixels();
            gotma = new PixelGrabber(back, 0, 0, w, h, mpix, 0, w).grabPixels();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (gotfg && gotma) {
            for (int i = 0; i < w * h; i++) {
                int ma = 0xff - (mpix[i] & 0xff); // center pixel
                int fg = ipix[i];
                ipix[i] = (fg & 0xffffff) | (ma << 24);
            }
            Image fin = null;
            if (comp != null) {
              fin = comp.createImage(new MemoryImageSource(w, h, ipix, 0, w));
            } else {
              fin = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, ipix, 0, w));
            }
            return (fin);
        } else
            WWContext.getInstance().fireLogging("Composite failed");

        return (im);
    }

    private static ColorModel generateColorModel() {
        // Generate 16-color model
        byte[] r = new byte[16];
        byte[] g = new byte[16];
        byte[] b = new byte[16];

        r[0] = 0;
        g[0] = 0;
        b[0] = 0;
        r[1] = 0;
        g[1] = 0;
        b[1] = (byte) 192;
        r[2] = 0;
        g[2] = 0;
        b[2] = (byte) 255;
        r[3] = 0;
        g[3] = (byte) 192;
        b[3] = 0;
        r[4] = 0;
        g[4] = (byte) 255;
        b[4] = 0;
        r[5] = 0;
        g[5] = (byte) 192;
        b[5] = (byte) 192;
        r[6] = 0;
        g[6] = (byte) 255;
        b[6] = (byte) 255;
        r[7] = (byte) 192;
        g[7] = 0;
        b[7] = 0;
        r[8] = (byte) 255;
        g[8] = 0;
        b[8] = 0;
        r[9] = (byte) 192;
        g[9] = 0;
        b[9] = (byte) 192;
        r[10] = (byte) 255;
        g[10] = 0;
        b[10] = (byte) 255;
        r[11] = (byte) 192;
        g[11] = (byte) 192;
        b[11] = 0;
        r[12] = (byte) 255;
        g[12] = (byte) 255;
        b[12] = 0;
        r[13] = (byte) 80;
        g[13] = (byte) 80;
        b[13] = (byte) 80;
        r[14] = (byte) 192;
        g[14] = (byte) 192;
        b[14] = (byte) 192;
        r[15] = (byte) 255;
        g[15] = (byte) 255;
        b[15] = (byte) 255;

        return new IndexColorModel(4, 16, r, g, b);
    }

    public static Color get(BufferedImage image, int i, int j) {
        return new Color(image.getRGB(i, j));
    }

    // change color of pixel (i, j) to c

    public static void set(BufferedImage image, int i, int j, Color c) {
        image.setRGB(i, j, c.getRGB());
    }

    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return (cm == null ? null : cm.hasAlpha());
    }

    public static BufferedImage toBufferedImage(Image image) {
        return toBufferedImage(image, 0D);
    }

    public static BufferedImage toBufferedImage(Image image, double rotation) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null),
                    image.getHeight(null),
                    transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null || rotation != 0D) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            if (rotation == 0D) {
              bimage = new BufferedImage(image.getWidth(null),
                      image.getHeight(null),
                      type);
            } else {
                if (Math.abs(rotation) == (Math.PI / 2D) || Math.abs(rotation) == (3 * Math.PI / 2D)) {
                  bimage = new BufferedImage(image.getHeight(null),
                          image.getWidth(null),
                          type);
                }
            }
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        Graphics2D g2d = (Graphics2D) g;
        // Rotate here?
        if (rotation != 0D) {
            AffineTransform at = new AffineTransform();
            double tx = (image.getWidth(null) / 2) - (image.getHeight(null) / 2);
            double ty = (image.getHeight(null) / 2) - (image.getWidth(null) / 2);
            at.translate(-tx, -ty);
            at.rotate(rotation, image.getWidth(null) / 2, image.getHeight(null) / 2);
            g2d.drawImage(image, at, null);
        } else {
            // Paint the image onto the buffered image
            g2d.drawImage(image, 0, 0, null);
        }
        g2d.dispose();

        return bimage;
    }

    public static GeoPoint getGeoPos(int x, int y,
                                     double _north, double _south, double _east, double _west,
                                     int w, int h) {
        GeoPoint gp = null;
        if (_north != _south && _east != _west) {
            double l = 0.0D;
            double g = 0.0D;
            double gAmpl = Math.abs(_east - _west);
            double lAmpl = Math.abs(MercatorUtil.getIncLat(_north) - MercatorUtil.getIncLat(_south));
//    double graph2chartRatio = (double)w / gAmpl;
            double graph2chartRatio = (double) h / lAmpl;
            g = (double) x / graph2chartRatio + _west;
            if (g < -180D) {
              g += 360D;
            }
            if (g > 180D) {
              g -= 360;
            }
            double incSouth = MercatorUtil.getIncLat(_south);
            double incLat = (double) (h - y) / graph2chartRatio + incSouth;
            l = MercatorUtil.getInvIncLat(incLat);
            gp = new GeoPoint(l, g);
        }
        return gp;
    }

    /**
     * @param bi     original image
     * @param top    top latitude, in degrees
     * @param bottom bottom latitude, in degrees
     * @param left   left longitude, in degrees
     * @param right  right longitude, in degrees
     * @return the Anaximandre fax
     */
    public static BufferedImage mercatorToAnaximandre(BufferedImage bi,
                                                      Color faxColor,
                                                      double top, double bottom,
                                                      double left, double right) {
        long before = System.currentTimeMillis();

        Raster raster = bi.getData();

        int minx = raster.getMinX();
        int miny = raster.getMinY();
        int width = raster.getWidth();
        int height = raster.getHeight();
        WWContext.getInstance().fireLogging("This image: from " + minx + "/" + miny + ", w:" + width + ", h:" + height);

        int[] pixels = raster.getPixels(minx, miny, width, height, (int[]) null);

//  System.out.println("Is " + (width * height * 3) + " = " + pixels.length + " ?");   
        if (pixels.length % 3 != 0) {
            throw new RuntimeException("Raster size mod 3 is not 0.\nW:" + width + " , H:" + height + ", len:" + pixels.length);
        }

        // Calculate the dimension of the square projection image
        double deltaG = Math.abs(left - right);
        if (deltaG > 180D) {
          deltaG = 360D - deltaG;
        }
        double deltaL = Math.abs(top - bottom);
        double squareFactor = (double) width / deltaG;
        int newHeight = (int) Math.round(deltaL * squareFactor);

        // Width remains unchanged
        BufferedImage image = new BufferedImage(width, newHeight, BufferedImage.TYPE_INT_RGB); // bi.getType());
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, newHeight);
        int x = 0, y = 0;
        g2d.setColor(faxColor);
        for (int i = 0; i < pixels.length; i += 3) {
            int[] onePixel = new int[3];
            onePixel[0] = pixels[i];       // red
            onePixel[1] = pixels[i + 1];   // green
            onePixel[2] = pixels[i + 2];   // blue
//    System.out.println("#" + Integer.toString(onePixel[0], 16) + Integer.toString(onePixel[1], 16) + Integer.toString(onePixel[2], 16));
            /*
             * Detect the black pixels.
             *
             * Detect the latitude - in mercator projection, turn it to square projection,
             * and write it to its new location.
             */
            GeoPoint geoPos = getGeoPos(x, y, top, bottom, right, left, width, height);
            if (onePixel[0] == 0 && onePixel[1] == 0 && onePixel[2] == 0) { // black
                // x is unchanged
                int squareY = newHeight - (int) Math.round((geoPos.getLatitude() - bottom) * squareFactor);
                g2d.drawLine(x, squareY, x, squareY);
            }
            x++;
            if (x >= width) {
                x = 0;
                y++;
            }
        }
        long after = System.currentTimeMillis();
        WWContext.getInstance().fireLogging("Processed in " + Long.toString(after - before) + "  ms.");

        return image;
    }

    public static BufferedImage mercatorToAnaximandre2(BufferedImage bi,
                                                       double top, double bottom,
                                                       double left, double right) {
        long before = System.currentTimeMillis();

        Raster raster = bi.getData();

        int minx = raster.getMinX();
        int miny = raster.getMinY();
        int width = raster.getWidth();
        int height = raster.getHeight();
        WWContext.getInstance().fireLogging("This image: from " + minx + "/" + miny + ", w:" + width + ", h:" + height);

        int[] pixels = raster.getPixels(minx, miny, width, height, (int[]) null);

        //  System.out.println("Is " + (width * height * 3) + " = " + pixels.length + " ?");
        if (pixels.length % 3 != 0) {
            throw new RuntimeException("Raster size mod 3 is not 0");
        }

        // Calculate the dimension of the square projection image
        double deltaG = Math.abs(left - right);
        if (deltaG > 180D) {
          deltaG = 360D - deltaG;
        }
        double deltaL = Math.abs(top - bottom);
        double squareFactor = (double) width / deltaG;
        int newHeight = (int) Math.round(deltaL * squareFactor);

        // Width remains unchanged
        BufferedImage image = new BufferedImage(width, newHeight, BufferedImage.TYPE_INT_RGB); // bi.getType());
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, newHeight);
        int x = 0, y = 0;
        for (int i = 0; i < pixels.length; i += 3) {
            int[] onePixel = new int[3];
            onePixel[0] = pixels[i];       // red
            onePixel[1] = pixels[i + 1];   // green
            onePixel[2] = pixels[i + 2];   // blue
            //    System.out.println("#" + Integer.toString(onePixel[0], 16) + Integer.toString(onePixel[1], 16) + Integer.toString(onePixel[2], 16));
            /*
             * Detect the black pixels.
             *
             * Detect the latitude - in mercator projection, turn it to square projection,
             * and write it to its new location.
             */
            GeoPoint geoPos = getGeoPos(x, y, top, bottom, right, left, width, height);
            Color currentColor = new Color(onePixel[0], onePixel[1], onePixel[2]);
            g2d.setColor(currentColor);
            // x is unchanged
            int squareY = newHeight - (int) Math.round((geoPos.getLatitude() - bottom) * squareFactor);
            g2d.drawLine(x, squareY, x, squareY);
            x++;
            if (x >= width) {
                x = 0;
                y++;
            }
        }
        long after = System.currentTimeMillis();
        WWContext.getInstance().fireLogging("Processed in " + Long.toString(after - before) + " ms.");

        return image;
    }

    public static Image readImage(String fileName)
            throws Exception {
        Image faxImg = null;
        if (fileName.toUpperCase().endsWith(".TIF") || fileName.toUpperCase().endsWith(".TIFF")) {
            File file = new File(fileName);
            if (file.exists()) {
                SeekableStream s = new FileSeekableStream(file);
                TIFFDecodeParam param = null;
                ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
                // Which of the multiple images in the TIFF file do we want to load
                // 0 refers to the first, 1 to the second and so on.
                RenderedImageAdapter ria = new RenderedImageAdapter(dec.decodeAsRenderedImage());
                faxImg = ria.getAsBufferedImage();
                s.close();
            }
        } else {
            faxImg = new ImageIcon(new File(fileName).toURI().toURL()).getImage();
        }
        return faxImg;
    }

    public static Image readImage(InputStream is, boolean tif)
            throws Exception {
        Image faxImg = null;
        if (tif) {
            TIFFDecodeParam param = null;
            ImageDecoder dec = ImageCodec.createImageDecoder("tiff", is, param);
            // Which of the multiple images in the TIFF file do we want to load
            // 0 refers to the first, 1 to the second and so on.
            RenderedImageAdapter ria = new RenderedImageAdapter(dec.decodeAsRenderedImage());
            faxImg = ria.getAsBufferedImage();
        } else {
            byte[] imageData = new byte[is.available()];
            int offset = 0;
            int numRead = 0;
            while (offset < imageData.length && (numRead = is.read(imageData, offset, imageData.length - offset)) >= 0) {
              offset += numRead;
            }
            faxImg = new ImageIcon(imageData).getImage();
        }
        is.close();
        return faxImg;
    }

    public static BufferedImage makeColorTransparent(Image img, Color color) {
        return makeColorTransparent(img, color, NO_CHANGE);
    }

    public static BufferedImage makeColorTransparent(Image img, Color color, int option) {
        return makeColorTransparent(img, color, option, null);
    }

    public static BufferedImage makeColorTransparent(Image img, Color color, int option, String[] messElements) {
        double lum = luminance(color);
        if (messElements != null) {
          WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("processing-image", messElements));
        }
        BufferedImage image = toBufferedImage(img);
        BufferedImage dimg = new BufferedImage(image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB); // To be used for set/getRGB, Alpha Red Green Blue
        Graphics2D g = dimg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(image, null, 0, 0);
        g.dispose();
        for (int i = 0; i < dimg.getHeight(); i++) {
            for (int j = 0; j < dimg.getWidth(); j++) {
                try {
                    double localLum = luminance(dimg.getRGB(j, i));
                    double abs = Math.abs(lum - localLum);
                    boolean closeEnough = abs < 2;
                    //   if (closeEnough && abs > 0d)
                    //     System.out.println("For i:" + i + ", j:" + j + ", Lum:" + lum + ", local:" + localLum + ", abs: " + Math.abs(lum - localLum) + (closeEnough ? " CLOSE ENOUGH" : ""));

                    if (dimg.getRGB(j, i) == color.getRGB() || closeEnough) { // Luminance [0..255]
                        dimg.setRGB(j, i, 0x008F1C1C); // 00 8f 1c 1c
                    }
                } catch (ArrayIndexOutOfBoundsException aiob) {
                    // Keep going
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (option == SHARPEN) {
          dimg = sharpen(dimg);
        } else if (option == BLUR) {
          dimg = blur(dimg);
        }
        return dimg;
    }

    public static BufferedImage switchColorAndMakeColorTransparent(Image img,
                                                                   Color turnThis,
                                                                   Color intoThat,
                                                                   Color colorToTransparent) {
        return switchColorAndMakeColorTransparent(img, turnThis, intoThat, colorToTransparent, NO_CHANGE);
    }

    public static BufferedImage switchColorAndMakeColorTransparent(Image img,
                                                                   Color turnThis,
                                                                   Color intoThat,
                                                                   Color colorToTransparent,
                                                                   int option) {
        return switchColorAndMakeColorTransparent(img, turnThis, intoThat, colorToTransparent, option, null);
    }

    public static BufferedImage switchColorAndMakeColorTransparent(Image img,
                                                                   Color turnThis,
                                                                   Color intoThat,
                                                                   Color colorToTransparent,
                                                                   int option,
                                                                   String[] messElements) {
        if (messElements != null) {
          WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("processing-image", messElements));
        }
        BufferedImage image = toBufferedImage(img);
        BufferedImage dimg = new BufferedImage(image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB); // To be used for set/getRGB, Alpha Red Green Blue
        Graphics2D g = dimg.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(image, null, 0, 0);
        g.dispose();
        for (int i = 0; i < dimg.getHeight(); i++) {
            for (int j = 0; j < dimg.getWidth(); j++) {
                if (turnThis != null && dimg.getRGB(j, i) == turnThis.getRGB()) {
                  dimg.setRGB(j, i, intoThat.getRGB()); // Switch Color
                }
                if ((dimg.getRGB(j, i) & 0x00FFFFFF) == (colorToTransparent.getRGB() & 0x00FFFFFF)) {
//        System.out.println("Making " + Integer.toHexString(dimg.getRGB(j, i) & 0x00ffffff) + " transparent.");
                    dimg.setRGB(j, i, 0x008F1C1C); // 00 8f 1c 1c, Make transparent
                } else if (turnThis == null) {
//        System.out.println("Turning " + dimg.getRGB(j, i) + " into " + intoThat.getRGB());
                    dimg.setRGB(j, i, intoThat.getRGB()); // Switch Color
                }
            }
        }
        if (option == SHARPEN) {
          dimg = sharpen(dimg);
        } else if (option == BLUR) {
          dimg = blur(dimg);
        }
        return dimg;
    }

    public static BufferedImage switchAnyColorAndMakeColorTransparent(Image img,
                                                                      Color intoThat,
                                                                      Color colorToTransparent) {
        return switchAnyColorAndMakeColorTransparent(img, intoThat, colorToTransparent, NO_CHANGE);
    }

    public static BufferedImage switchAnyColorAndMakeColorTransparent(Image img,
                                                                      Color intoThat,
                                                                      Color colorToTransparent,
                                                                      int option) //,
    // String[] messElements)
    {
        return switchColorAndMakeColorTransparent(img, null, intoThat, colorToTransparent, option, null);
    }

    /*
     * For some tests
     */
    public static void main2(String[] args) throws Exception {
        Image faxImg =
                readImage("C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2007-gif\\sfc\\NOAA_sfc_2007-09-25.gif");
        double top = 65D;
        double bottom = 14D;
        double left = 135D;
        double right = -115D;

        BufferedImage image = mercatorToAnaximandre(toBufferedImage(faxImg), Color.red, top, bottom, left, right);
        Image img = makeTransparentImage(null, Toolkit.getDefaultToolkit().createImage(image.getSource()), Color.red);
        ImageIO.write(toBufferedImage(img), "png", new File("generatedSurface.png"));

        top = 64D;
        bottom = 12D;
        left = 134.5D;
        right = -114D;
        faxImg = readImage("C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2007-gif\\500mb\\NOAA_500mb_2007-09-27.gif");

        image = mercatorToAnaximandre(toBufferedImage(faxImg), Color.black, top, bottom, left, right);
        img = makeTransparentImage(null, Toolkit.getDefaultToolkit().createImage(image.getSource()), Color.cyan);
        ImageIO.write(toBufferedImage(img), "png", new File("generated500.png"));
    }

    public static int countColors(Image img) {
        BufferedImage image = toBufferedImage(img);
        ArrayList<Color> colors = new ArrayList<Color>();
        if (image != null) { // Count number of colors
            int w = image.getWidth();
            int h = image.getHeight();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int pixel = image.getRGB(x, y);
//        String colorStr = Integer.toHexString(pixel);
                    int red = (pixel & 0x00ff0000) >> 16;
                    int green = (pixel & 0x0000ff00) >> 8;
                    int blue = pixel & 0x000000ff;
                    Color color = new Color(red, green, blue);
                    //add the first color on array
                    if (colors.size() == 0) {
                        colors.add(color);
//          System.out.println("1. Added " + colorStr + " (" + color.getRGB() + ")");
                    }
                    // check for redudancy
                    else {
                        if (!(colors.contains(color))) {
                            colors.add(color);
//            System.out.println("2. Added " + colorStr + " (" + color.getRGB() + ")");
                        }
                    }
                }
            }
//    System.out.println("There are " + colors.size() + " colors in this image");
        }
        return colors.size();
    }

    public static Color mostUsedColor(Image img) {
        Color mostUsedColor = null;
        BufferedImage image = toBufferedImage(img);
        HashMap<Color, Integer> nbPixelPerColor = new HashMap<Color, Integer>();
        if (image != null) { // Count number of colors
            int w = image.getWidth();
            int h = image.getHeight();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int pixel = image.getRGB(x, y);
//        String colorStr = Integer.toHexString(pixel);
                    int red = (pixel & 0x00ff0000) >> 16;
                    int green = (pixel & 0x0000ff00) >> 8;
                    int blue = pixel & 0x000000ff;
                    Color color = new Color(red, green, blue);
                    nbPixelPerColor.put(color, (nbPixelPerColor.get(color) == null) ? 1 : nbPixelPerColor.get(color) + 1);
                }
            }
            Set<Color> keys = nbPixelPerColor.keySet();

            int max = 0;
            for (Color c : keys) {
//      System.out.println(Integer.toHexString(c.getRGB()) + " = " + nbPixelPerColor.get(c) + " pixel(s).");
                if (nbPixelPerColor.get(c) > max) {
                    max = nbPixelPerColor.get(c);
                    mostUsedColor = c;
                }
            }
        }
        return mostUsedColor;
    }

    public static void main(String... args) {
        if (false) {
            int max = Integer.MAX_VALUE;
            System.out.println("Max:" + max + ", " + Integer.toHexString(max));
            System.out.println("00 8f 1c 1c = " + Integer.toString(0x8f) + " " +
                    Integer.toString(0x1c) + " " +
                    Integer.toString(0x1c));
        }
        if (true) {
//    String imageLoc = "C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2010\\01\\sfc\\color_4_tests.png";
            String imageLoc = "D:\\OlivSoft\\all-scripts\\WeatherFaxes\\faxes\\uk00.png";
            try {
                JPanel jp = new JPanel();
                Image original = ImageUtil.readImage(imageLoc);
                System.out.println("White: 0x" + Integer.toHexString(Color.white.getRGB()).toUpperCase());
                int nbColor = countColors(original);

                Image img = null;
                if (false) {
                    img = ImageUtil.makeTransparentImage(jp, original);
                    ImageIO.write(toBufferedImage(img), "png", new File("01.png"));
                }
                if (false) {
                    img = ImageUtil.turn2TransparentImage(jp, original, Color.white);
                    ImageIO.write(toBufferedImage(img), "png", new File("02.png"));
                }
                if (false) {
                    img = ImageUtil.makeColorTransparent(original, Color.white);
                    ImageIO.write(toBufferedImage(img), "png", new File("03.png"));
                }
                if (true) {
                    long before = System.currentTimeMillis();
                    img = ImageUtil.switchColorAndMakeColorTransparent(original, Color.black, Color.red, Color.white);
                    ImageIO.write(toBufferedImage(img), "png", new File("04.png"));
                    long after = System.currentTimeMillis();
                    System.out.println("Done in " + Long.toString(after - before) + " ms.");
                }
                if (true) {
                    long before = System.currentTimeMillis();
                    img = ImageUtil.switchAnyColorAndMakeColorTransparent(original, Color.red, Color.white);
                    ImageIO.write(toBufferedImage(img), "png", new File("05.png"));
                    long after = System.currentTimeMillis();
                    System.out.println("Done in " + Long.toString(after - before) + " ms.");
                }
                if (true) {
                    long before = System.currentTimeMillis();
                    img = ImageUtil.switchAnyColorAndMakeColorTransparent(original, Color.red, new Color(0xfffefefe));
                    ImageIO.write(toBufferedImage(img), "png", new File("06.png"));
                    long after = System.currentTimeMillis();
                    System.out.println("Done in " + Long.toString(after - before) + " ms.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                System.out.println("Done.");
            }
        }
    }
}