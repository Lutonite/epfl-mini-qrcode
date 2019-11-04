package qrcode;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.nayuki.qrcodegen.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;

class RandomizedTests {


    public static int[][] readMatrix(String name) {
        return imageToMatrix(read(name));
    }
    private static int[][] imageToMatrix(BufferedImage image) {
        int[][] matrix = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                matrix[i][j] = image.getRGB(i, j);
            }
        }
        return matrix;
    }
    private static BufferedImage read(String name) {
        String projectPath = System.getProperty("user.dir");
        String path = projectPath + File.separator + "images/" + name;
        try {

            if (!name.contains(".png")) {
                path = path + ".png";
            }
            File pathToFile = new File(path);
            BufferedImage image = ImageIO.read(pathToFile);
            return image;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("The image '"+path+"' does not exist or could not load");
        }
    }

    /**
     * compare a matrix loaded from file with a 2D-array given in arguments.
     * @param matrix the 2-dimensional array
     * @param imagePath the path of the image to compare with the matrix
     * @return true if the 2 images are similar, false otherwise
     */
    public static boolean compare(int [][] matrix,String imagePath) {
        int[][] expected= readMatrix(imagePath);
        if(expected.length != matrix.length || expected.length!=expected[0].length || matrix.length!=matrix[0].length) {
            throw new IllegalArgumentException("The size of the two QR code does not match: matrix:"+matrix.length+"  image:"+expected.length);
        }
        boolean similar = true;
        for(int x=0;x<matrix.length;x++) {
            for(int y=0;y<matrix.length;y++) {
                if(matrix[x][y]!=expected[x][y]) {
                    similar = false;
                }
            }
        }

        return similar;
    }

    @Test
    void testVersions1to40withRandomValuesOnLowECC() {
        for (int v = 1; v <= 40; v++) {
            for (int m = 0; m < 7; m++) {
                // qrcodegen uses utf8, but we use iso. So we will force byte mode
                // but not add any utf8 related characters
                String uuid = RandomStringUtils.randomAlphanumeric(v * 9) + "e";

                List<QrSegment> segments = QrSegment.makeSegments(uuid);
                QrCode qr = QrCode.encodeSegments(segments, QrCode.Ecc.LOW, v, v, m, false);
                BufferedImage img = qr.toImage(1, 0);
                try {
                    ImageIO.write(img, "png", new File(
                            System.getProperty("user.dir")
                                    + File.separator
                                    + "images"
                                    + File.separator
                                    + "temp.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Extensions.CORRECTION_LEVEL = Extensions.QRCodeInfos.CorrectionLevel.LOW;

                boolean[] encodedData = DataEncoding.byteModeEncoding(uuid, v);
                int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(v, encodedData, m);

                Helpers.writeMatrix("testLow", qrCode);

                assertTrue(compare(qrCode, "temp"), "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

    @Test
    void testVersions1to40withRandomValuesOnMediumECC() {
        for (int v = 1; v <= 40; v++) {
            for (int m = 0; m < 7; m++) {
                // qrcodegen uses utf8, but we use iso. So we will force byte mode
                // but not add any utf8 related characters
                String uuid = RandomStringUtils.randomAlphanumeric(v * 6) + "e";

                List<QrSegment> segments = QrSegment.makeSegments(uuid);
                QrCode qr = QrCode.encodeSegments(segments, QrCode.Ecc.MEDIUM, v, v, m, false);
                BufferedImage img = qr.toImage(1, 0);
                try {
                    ImageIO.write(img, "png", new File(
                            System.getProperty("user.dir")
                                    + File.separator
                                    + "images"
                                    + File.separator
                                    + "temp.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Extensions.CORRECTION_LEVEL = Extensions.QRCodeInfos.CorrectionLevel.MEDIUM;

                boolean[] encodedData = DataEncoding.byteModeEncoding(uuid, v);
                int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(v, encodedData, m);

                Helpers.writeMatrix("testMedium", qrCode);

                assertTrue(compare(qrCode, "temp"), "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

    @Test
    void testVersions1to40withRandomValuesOnQuartileECC() {
        for (int v = 1; v <= 40; v++) {
            for (int m = 0; m < 7; m++) {
                // qrcodegen uses utf8, but we use iso. So we will force byte mode
                // but not add any utf8 related characters
                String uuid = RandomStringUtils.randomAlphanumeric(v * 5) + "e";

                List<QrSegment> segments = QrSegment.makeSegments(uuid);
                QrCode qr = QrCode.encodeSegments(segments, QrCode.Ecc.QUARTILE, v, v, m, false);
                BufferedImage img = qr.toImage(1, 0);
                try {
                    ImageIO.write(img, "png", new File(
                            System.getProperty("user.dir")
                                    + File.separator
                                    + "images"
                                    + File.separator
                                    + "temp.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Extensions.CORRECTION_LEVEL = Extensions.QRCodeInfos.CorrectionLevel.QUARTILE;

                boolean[] encodedData = DataEncoding.byteModeEncoding(uuid, v);
                int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(v, encodedData, m);

                Helpers.writeMatrix("testQuartile", qrCode);

                assertTrue(compare(qrCode, "temp"), "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

    @Test
    void testVersions1to40withRandomValuesOnHighECC() {
        for (int v = 1; v <= 40; v++) {
            for (int m = 0; m < 7; m++) {
                // qrcodegen uses utf8, but we use iso. So we will force byte mode
                // but not add any utf8 related characters
                String uuid = RandomStringUtils.randomAlphanumeric(v * 5) + "e";

                List<QrSegment> segments = QrSegment.makeSegments(uuid);
                QrCode qr = QrCode.encodeSegments(segments, QrCode.Ecc.HIGH, v, v, m, false);
                BufferedImage img = qr.toImage(1, 0);
                try {
                    ImageIO.write(img, "png", new File(
                            System.getProperty("user.dir")
                                    + File.separator
                                    + "images"
                                    + File.separator
                                    + "temp.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Extensions.CORRECTION_LEVEL = Extensions.QRCodeInfos.CorrectionLevel.HIGH;

                boolean[] encodedData = DataEncoding.byteModeEncoding(uuid, v);
                int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(v, encodedData, m);

                Helpers.writeMatrix("testHigh", qrCode);

                assertTrue(compare(qrCode, "temp"), "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

}
