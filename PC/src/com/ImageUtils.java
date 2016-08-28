package com;

import com.io.IOUtils;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtils {

    /**
     *
     * Metoda zwraca typ pliku graficznego na podstawie sygnatury
     */
    public static String getFileTypeBySignature(byte[] b) {

        if (b.length >= 4
                && (b[0] & 0xFF) == 0x47
                && (b[1] & 0xFF) == 0x49
                && (b[2] & 0xFF) == 0x46
                && (b[3] & 0xFF) == 0x38) {
            return "gif";
        }

        if (b.length >= 4
                && (b[0] & 0xFF) == 0x89
                && (b[1] & 0xFF) == 0x50
                && (b[2] & 0xFF) == 0x4e
                && (b[3] & 0xFF) == 0x47) {
            return "png";
        }

        if (b.length >= 3
                && (b[0] & 0xFF) == 0xFF
                && (b[1] & 0xFF) == 0xD8
                && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }

        if (b.length >= 2
                && (b[0] & 0xFF) == 0x42
                && (b[1] & 0xFF) == 0x4d) {
            return "bmp";
        }

        return null;
    }

    public static BufferedImage read(File file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return read(in);
        }

    }

    public static BufferedImage read(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(in, bout, false);
        return read(bout.toByteArray());
    }

    public static BufferedImage read(byte[] buff) throws IOException {
        ImageIO.setUseCache(false);
        return ImageIO.read(new ByteArrayInputStream(buff));
    }

    public final static class ImageProperties {

        final public int width;
        final public int height;
        final public char[] comment;
        final public String type;

        public ImageProperties(int width, int height, char[] comment, String type) {
            this.width = width;
            this.height = height;
            this.comment = comment;
            this.type = type;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(type).append(" ").append(width).append("x").append(height);
            if (comment != null) {
                sb.append(" (").append(comment).append(")");
            }
            return (sb.toString());
        }
    }

    final public static ImageProperties getJpegProperties(InputStream in)
            throws FileNotFoundException, IOException {

        try {
            // check for "magic" header
            byte[] buf = new byte[2];
            int count = in.read(buf, 0, 2);
            if (count < 2) {
                throw new RuntimeException("Not a valid Jpeg file!");
            }
            if ((buf[0]) != (byte) 0xFF || (buf[1]) != (byte) 0xD8) {
                throw new RuntimeException("Not a valid Jpeg file!");
            }

            int width = 0;
            int height = 0;
            char[] comment = null;

            boolean hasDims = false;
            boolean hasComment = false;
            int ch = 0;

            while (ch != 0xDA && !(hasDims && hasComment)) {
                /*
                 * Find next marker (JPEG markers begin with 0xFF)
                 */
                while (ch != 0xFF) {
                    ch = in.read();
                }
                /*
                 * JPEG markers can be padded with unlimited 0xFF's
                 */
                while (ch == 0xFF) {
                    ch = in.read();
                }
                /*
                 * Now, ch contains the value of the marker.
                 */

                int length = 256 * in.read();
                length += in.read();
                if (length < 2) {
                    throw new RuntimeException("Not a valid Jpeg file!");
                }
                /*
                 * Now, length contains the length of the marker.
                 */

                if (ch >= 0xC0 && ch <= 0xC3) {
                    in.read();
                    height = 256 * in.read();
                    height += in.read();
                    width = 256 * in.read();
                    width += in.read();
                    for (int foo = 0; foo < length - 2 - 5; foo++) {
                        in.read();
                    }
                    hasDims = true;
                } else
                    if (ch == 0xFE) {
                        // that's the comment marker
                        comment = new char[length - 2];
                        for (int foo = 0; foo < length - 2; foo++) {
                            comment[foo] = (char) in.read();
                        }
                        hasComment = true;
                    } else {
                        // just skip marker
                        for (int foo = 0; foo < length - 2; foo++) {
                            in.read();
                        }
                    }
            }
            return (new ImageProperties(width, height, comment, "jpeg"));

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    final public static ImageProperties getGifProperties(File file) throws FileNotFoundException, IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[10];
            int count = in.read(buf, 0, 10);
            if (count < 10) {
                throw new RuntimeException("Not a valid Gif file!");
            }
            if ((buf[0]) != (byte) 'G' || (buf[1]) != (byte) 'I' || (buf[2]) != (byte) 'F') {
                throw new RuntimeException("Not a valid Gif file!");
            }

            int w1 = (buf[6] & 0xff) | (buf[6] & 0x80);
            int w2 = (buf[7] & 0xff) | (buf[7] & 0x80);
            int h1 = (buf[8] & 0xff) | (buf[8] & 0x80);
            int h2 = (buf[9] & 0xff) | (buf[9] & 0x80);

            int width = w1 + (w2 << 8);
            int height = h1 + (h2 << 8);

            return (new ImageProperties(width, height, null, "gif"));

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Wczytywanie rozszerzonych plików JPEG, np z paletą CMYK
     */
    /*
     public static BufferedImage readJPEG(InputStream is) throws IOException {
     Iterator readers = ImageIO.getImageReadersByFormatName("JPEG");
     ImageReader reader = null;
     while (readers.hasNext()) {
     reader = (ImageReader) readers.next();
     if (reader.canReadRaster()) {
     break;
     }
     }
     //Stream the image file (the original CMYK image)
     ImageInputStream input = ImageIO.createImageInputStream(is);
     reader.setInput(input);
     //Read the image raster
     Raster raster = reader.readRaster(0, null);
     //Create a new RGB image
     BufferedImage bi = new BufferedImage(raster.getWidth(), raster.getHeight(),
     BufferedImage.TYPE_4BYTE_ABGR);

     bi.getRaster().setRect(raster);
     return bi;
     }
     */
    public static void saveAsPng(BufferedImage img, OutputStream out)
            throws IOException {
        // tworzy pusty obraz z bialym tlem i naklada na niego przezroczysty png

        BufferedImage bi = new BufferedImage(img.getWidth(),
                img.getHeight(), BufferedImage.TYPE_INT_RGB);
        bi.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
        ImageIO.write(bi, "png", out);
    }

    public static void saveAsGif(BufferedImage img, OutputStream out) throws IOException {
        ImageIO.write(img, "gif", out);
    }

    public static void saveAsJpg(BufferedImage img, OutputStream out,
            double quality) throws IOException {
        ImageWriter writer = null;
        Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
        if (iter.hasNext()) {
            writer = (ImageWriter) iter.next();
        }
        if (writer == null)
            return;
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);

            JPEGImageWriteParam iwp = new JPEGImageWriteParam(new Locale("pl"));
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality((float) quality);
            writer.write(null, new IIOImage(img, null, null), iwp);
        } finally {
            writer.dispose();
        }
    }

    public static BufferedImage resizeIfToBig(BufferedImage img, int maxWidth, int maxHeight) {

        if (img.getWidth() > maxWidth || img.getHeight() > maxHeight) {

            double xH = (double) maxHeight / (double) img.getHeight();
            double xW = (double) maxWidth / (double) img.getWidth();
            double dt = 1;

            if (xH < 1 || xW < 1) {
                if (xH < xW) {
                    dt = xH;
                } else {
                    dt = xW;
                }
            }

            int oW = (int) Math.round(img.getWidth() * dt);
            int oH = (int) Math.round(img.getHeight() * dt);

            BufferedImage bdest = new BufferedImage(oW, oH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bdest.createGraphics();
            AffineTransform at
                    = AffineTransform.getScaleInstance((double) oW / img.getWidth(),
                            (double) oH / img.getHeight());
            g.drawRenderedImage(img, at);
            return bdest;
        }
        return img;
    }

}
