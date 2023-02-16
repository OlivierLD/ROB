package main.images;

import chartview.util.gifutil.GIFInputStream;
import chartview.util.gifutil.GIFOutputStream;
import chartview.util.gifutil.Gif;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownload {

    final static String FAX_ONE = "https://tgftp.nws.noaa.gov/fax/PYAA12.gif";
    final static String FAX_TWO = "https://www2.wetter3.de/Fax/00_UKMet_Boden+00.gif";

    public static void imageGif2GifDownloader(String faxURL, String fName) {
        try {
            URL chartUrl = new URL(faxURL);
            URLConnection urlConn = chartUrl.openConnection();
            Gif gifImage = new Gif();
            gifImage.init(new GIFInputStream(urlConn.getInputStream()));

            File f = new File(fName);
            gifImage.write(new GIFOutputStream(new FileOutputStream(f)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void imageGifToPngDownloader(String faxURL, String pngName) {
        try {
            URL chartUrl = new URL(faxURL);
            Image image = ImageIO.read(chartUrl);
            ImageIO.write((RenderedImage) image, "png", new File(pngName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String... args) {
        if (true) {
            imageGif2GifDownloader(FAX_ONE, "local.image.one.gif");
            imageGif2GifDownloader(FAX_TWO, "local.image.two.gif");
        }

        if (true) {
            imageGifToPngDownloader(FAX_ONE, "local.image.one.png");
            imageGifToPngDownloader(FAX_TWO, "local.image.two.png");
        }
    }

}
