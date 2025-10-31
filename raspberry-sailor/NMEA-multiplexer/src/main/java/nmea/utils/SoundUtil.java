package nmea.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SoundUtil {
    private static final int EXTERNAL_BUFFER_SIZE = 128_000;

    public static void playSound(URL sound) throws Exception {
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(sound);
        } catch (Exception e) {
            // System.err.println(e.getLocalizedMessage());
            throw e;
            // e.printStackTrace();
            // System.exit(1);
        }

        AudioFormat audioFormat = audioInputStream.getFormat();
        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//            System.exit(1);
            throw e;
        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
            throw e;
        }

        line.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                int nBytesWritten = line.write(abData, 0, nBytesRead);
            }
        }
        line.drain();
        line.close();
    }

    // For tests
    public static void main(String[] args) {
        try {
            // final URL url = new File("misc/vista.wav").toURI().toURL();
            final URL url = new File("misc/loud-fog-horn.wav").toURI().toURL();
            // final URL url = new File("misc/loud-boat-horn.wav").toURI().toURL();
            playSound(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}