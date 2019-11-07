package qrcode;

import static org.junit.jupiter.api.Assertions.*;

import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrSegment;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class RandomizedTests {

    /*
     * These methods have been copied from Helpers.
     */
    static int[][] imageToMatrix(BufferedImage image) {
        int[][] matrix = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                matrix[i][j] = image.getRGB(i, j);
            }
        }
        return matrix;
    }
    static boolean compare(int [][] matrix, BufferedImage img) {
        int[][] expected= imageToMatrix(img);
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

    private boolean generateAndCompare(String uuid, QrCode.Ecc ecl1,
                                       Extensions.QRCodeInfos.CorrectionLevel ecl2,
                                       int v, int m, boolean masking) {
        List<QrSegment> segs = new ArrayList<>();
        segs.add(QrSegment.makeBytes(uuid.getBytes(StandardCharsets.ISO_8859_1)));
        QrCode qr1;
        if (masking) qr1 = QrCode.encodeSegments(segs, ecl1, v, v, -1, false);
        else qr1 = QrCode.encodeSegments(segs, ecl1, v, v, m, false);
        BufferedImage img = qr1.toImage(1, 0);

        Extensions.CORRECTION_LEVEL = ecl2;
        boolean[] encodedData = DataEncoding.byteModeEncoding(uuid, v);
        int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(v, encodedData, m);

        return compare(qrCode, img);
    }
    private boolean generateAndCompare(String uuid, QrCode.Ecc ecl1,
                                       Extensions.QRCodeInfos.CorrectionLevel ecl2,
                                       int v, int m) {
        return generateAndCompare(uuid, ecl1, ecl2, v, m, false);
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
                String uuid = RandomStringUtils.randomAlphanumeric(v * 5) + "e";

                assertTrue(generateAndCompare(uuid, QrCode.Ecc.HIGH,
                        Extensions.QRCodeInfos.CorrectionLevel.HIGH, v, m),
                        "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
            }
        }
    }

    // The following tests are not working because the Java library we used does not compute the mask the same way
    // we do (the website version Nayuki uses the same penalty system as we do, but the Java version does not)
    /*@Test
    void testVersions1to4withRandomValuesAndBestMaskingWithoutExtensionsOnLowECC() {
        MatrixConstruction.USE_EXTENSIONS = false;

        for (int v = 1; v <= 4; v++) {
            String uuid = RandomStringUtils.randomAlphanumeric(v * 9 + 1);

            int m = MatrixConstruction.findBestMasking(v, DataEncoding.byteModeEncoding(uuid, v));

            assertTrue(generateAndCompare(uuid, QrCode.Ecc.LOW,
                    Extensions.QRCodeInfos.CorrectionLevel.LOW, v, m, true),
                    "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
        }

        MatrixConstruction.USE_EXTENSIONS = true;
    }

    @Test
    void testVersions1to40withRandomValuesAndBestMaskingOnLowECC() {
        for (int v = 1; v <= 40; v++) {
            String uuid = RandomStringUtils.randomAlphanumeric(v * 9 + 1);

            int m = MatrixConstruction.findBestMasking(v, DataEncoding.byteModeEncoding(uuid, v));

            assertTrue(generateAndCompare(uuid, QrCode.Ecc.LOW,
                    Extensions.QRCodeInfos.CorrectionLevel.LOW, v, m, true),
                    "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
        }
    }

    @Test
    void testVersions1to40withRandomValuesAndBestMaskingOnMediumECC() {
        for (int v = 1; v <= 40; v++) {
            String uuid = RandomStringUtils.randomAlphanumeric(v * 6) + "e";

            int m = MatrixConstruction.findBestMasking(v, DataEncoding.byteModeEncoding(uuid, v));

            assertTrue(generateAndCompare(uuid, QrCode.Ecc.MEDIUM,
                    Extensions.QRCodeInfos.CorrectionLevel.MEDIUM, v, m, true),
                    "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
        }
    }

    @Test
    void testVersions1to40withRandomValuesAndBestMaskingOnQuartileECC() {
        for (int v = 1; v <= 40; v++) {
            String uuid = RandomStringUtils.randomAlphanumeric(v * 5) + "e";

            int m = MatrixConstruction.findBestMasking(v, DataEncoding.byteModeEncoding(uuid, v));

            assertTrue(generateAndCompare(uuid, QrCode.Ecc.QUARTILE,
                    Extensions.QRCodeInfos.CorrectionLevel.QUARTILE, v, m, true),
                    "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
        }
    }

    @Test
    void testVersions1to40withRandomValuesAndBestMaskingOnHighECC() {
        for (int v = 1; v <= 40; v++) {
            String uuid = RandomStringUtils.randomAlphanumeric(v * 5) + "e";

            int m = MatrixConstruction.findBestMasking(v, DataEncoding.byteModeEncoding(uuid, v));

            assertTrue(generateAndCompare(uuid, QrCode.Ecc.HIGH,
                    Extensions.QRCodeInfos.CorrectionLevel.HIGH, v, m, true),
                    "TEST FAILED - VERSION: " + v + " MASK: " + m + " TEXT: " + uuid);
        }
    }*/

}
