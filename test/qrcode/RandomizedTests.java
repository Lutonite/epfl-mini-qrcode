package qrcode;

import static org.junit.jupiter.api.Assertions.*;

import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;

class RandomizedTests {

    /*
     * These methods have been copied from Helpers.
     */
    static int[][] readMatrix(String name) {
        return imageToMatrix(read(name));
    }
    static int[][] imageToMatrix(BufferedImage image) {
        int[][] matrix = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                matrix[i][j] = image.getRGB(i, j);
            }
        }
        return matrix;
    }
    static BufferedImage read(String name) {
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
    static boolean compare(int [][] matrix,String imagePath) {
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

    private boolean generateAndCompare(String uuid,
                               QrCode.Ecc ecl1,
                               Extensions.QRCodeInfos.CorrectionLevel ecl2,
                               int v,
                               int m) {

        List<QrSegment> segs = new ArrayList<>();
        segs.add(QrSegment.makeBytes(uuid.getBytes(StandardCharsets.ISO_8859_1)));

        QrCode qr1 = QrCode.encodeSegments(segs, ecl1, v, v, m , false);
        BufferedImage img = qr1.toImage(1, 0);
        try {
            ImageIO.write(img, "png",
                    new File(System.getProperty("user.dir") + File.separator + "images" + File.separator + "temp.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Extensions.CORRECTION_LEVEL = ecl2;

        boolean[] encodedData = DataEncoding.byteModeEncoding(uuid, v);
        int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(v, encodedData, m);

        return compare(qrCode, "temp");
    }

    @BeforeEach
    void init() {
        MatrixConstruction.USE_EXTENSIONS = true;
    }

    @Test
    void testVersions1to4withRandomValuesWithoutExtensionsOnLowECC() {
        MatrixConstruction.USE_EXTENSIONS = false;

        for (int v = 1; v <= 4; v++) {
            for (int m = 0; m < 7; m++) {
                String uuid = RandomStringUtils.randomAlphanumeric(v*9) + "c";

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.LOW,
                        Extensions.QRCodeInfos.CorrectionLevel.LOW, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }

        MatrixConstruction.USE_EXTENSIONS = true;
    }

    @Test
    void testVersions1to4withEmptyStringWithoutExtensionsOnLowECC() {
        MatrixConstruction.USE_EXTENSIONS = false;

        for (int v = 1; v <= 4; v++) {
            for (int m = 0; m < 7; m++) {
                String uuid = "";

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.LOW,
                        Extensions.QRCodeInfos.CorrectionLevel.LOW, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }

        MatrixConstruction.USE_EXTENSIONS = true;
    }

    @Test
    void testVersions1to40withEmptyStringOnAllECC() {
        for (int v = 1; v <= 40; v++) {
            for (int m = 0; m < 7; m++) {
                String uuid = "";

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.LOW,
                        Extensions.QRCodeInfos.CorrectionLevel.LOW, v, m),
                        "TEST FAILED - ECC L - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.MEDIUM,
                        Extensions.QRCodeInfos.CorrectionLevel.MEDIUM, v, m),
                        "TEST FAILED - ECC M - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.QUARTILE,
                        Extensions.QRCodeInfos.CorrectionLevel.QUARTILE, v, m),
                        "TEST FAILED - ECC Q - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.HIGH,
                        Extensions.QRCodeInfos.CorrectionLevel.HIGH, v, m),
                        "TEST FAILED - ECC H - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

    @Test
    void testVersions1to40withRandomValuesOnLowECC() {
        for (int v = 1; v <= 40; v++) {
            for (int m = 0; m < 7; m++) {
                // qrcodegen uses utf8, but we use iso. So we will force byte mode
                // but not add any utf8 related characters
                String uuid = RandomStringUtils.randomAlphanumeric(v * 9) + "e";

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.LOW,
                        Extensions.QRCodeInfos.CorrectionLevel.LOW, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
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

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.MEDIUM,
                        Extensions.QRCodeInfos.CorrectionLevel.MEDIUM, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
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

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.QUARTILE,
                        Extensions.QRCodeInfos.CorrectionLevel.QUARTILE, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
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

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.HIGH,
                        Extensions.QRCodeInfos.CorrectionLevel.HIGH, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

}
