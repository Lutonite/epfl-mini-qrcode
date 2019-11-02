package qrcode;

import java.util.HashMap;
import java.util.Map;

/**
 * Extensions file. This file contains the methods and definitions which are used in order
 * to do some bonuses. This is done so we have a clear separation from what is required from
 * the project documentation and the extensions that we made as a bonus.
 *
 * Bonuses we have done:
 *      - addAlignmentsPattern for all versions of QR codes
 *      - evaluate function
 *      - numeric and alphanumeric encoding with detection of which method to use
 *      - support for versions from 1 to 40
 *
 * @author Kelvin Kappeler
 * @author Loïc Herman
 */

public class Extensions {

    public final static QRCodeInfos.CorrectionLevel CORRECTION_LEVEL = QRCodeInfos.CorrectionLevel.HIGH;

    private final static int ALIGNMENT_PATTERNS_FIRST_POSITION = 6;

    /**
     * @see MatrixConstruction#constructMatrix(int, int);
     *
     * More or less the same function, except it uses our own QRCodeInfos class to extract properties
     * needed for versions higher than 4 with ECC different than Low.
     *
     * @param matrix The matrix reference of the QR Code
     * @param infos The QRCodeInfos object with the properties needed
     */
    public static void constructMatrix(int[][] matrix, QRCodeInfos infos) {
        MatrixConstruction.addFinderPatterns(matrix);
        addAlignmentPatterns(matrix, infos);
        MatrixConstruction.addTimingPatterns(matrix);
        MatrixConstruction.addDarkModule(matrix);
        addFormatInformation(matrix, infos);
    }

    /**
     * Method to define the array of positions for which an alignment pattern must be placed.
     * According to ISO/IEC 18004:2000(E), this should be an array which would look like this:
     *
     *       {                            },  // Version 1
     *       {6, 18                       },  // Version 2
     *       {6, 22                       },  // Version 3
     *       {6, 26                       },  // Version 4
     *       {6, 30                       },  // Version 5
     *       {6, 34                       },  // Version 6
     *       {6, 22, 38                   },  // Version 7
     *       {6, 24, 42                   },  // Version 8
     *       {6, 26, 46                   },  // Version 9
     *       {6, 28, 50                   },  // Version 10
     *       {6, 30, 54                   },  // Version 11
     *       {6, 32, 58                   },  // Version 12
     *       {6, 34, 62                   },  // Version 13
     *       {6, 26, 46, 66               },  // Version 14
     *       {6, 26, 48, 70               },  // Version 15
     *       {6, 26, 50, 74               },  // Version 16
     *       {6, 30, 54, 78               },  // Version 17
     *       {6, 30, 56, 82               },  // Version 18
     *       {6, 30, 58, 86               },  // Version 19
     *       {6, 34, 62, 90               },  // Version 20
     *       {6, 28, 50, 72,  94          },  // Version 21
     *       {6, 26, 50, 74,  98          },  // Version 22
     *       {6, 30, 54, 78, 102          },  // Version 23
     *       {6, 28, 54, 80, 106          },  // Version 24
     *       {6, 32, 58, 84, 110          },  // Version 25
     *       {6, 30, 58, 86, 114          },  // Version 26
     *       {6, 34, 62, 90, 118          },  // Version 27
     *       {6, 26, 50, 74,  98, 122     },  // Version 28
     *       {6, 30, 54, 78, 102, 126     },  // Version 29
     *       {6, 26, 52, 78, 104, 130     },  // Version 30
     *       {6, 30, 56, 82, 108, 134     },  // Version 31
     *       {6, 34, 60, 86, 112, 138     },  // Version 32
     *       {6, 30, 58, 86, 114, 142     },  // Version 33
     *       {6, 34, 62, 90, 118, 146     },  // Version 34
     *       {6, 30, 54, 78, 102, 126, 150},  // Version 35
     *       {6, 24, 50, 76, 102, 128, 154},  // Version 36
     *       {6, 28, 54, 80, 106, 132, 158},  // Version 37
     *       {6, 32, 58, 84, 110, 136, 162},  // Version 38
     *       {6, 26, 54, 82, 110, 138, 166},  // Version 39
     *       {6, 30, 58, 86, 114, 142, 170},  // Version 40
     *
     * As this is an array that takes a big code footprint, could potentially be typo-prone (although the one shown
     * here is an exact copy of the table given in ISO/IEC 18004:2000(E) at the Annex E), we wanted to create
     * a function which can generate the right coordinates without having to go through an unnecessary big table.
     *
     * According to ISO/IEC 18004:2000(E), the Alignment Patterns must be evenly spaced between the Timing Patterns
     * and the opposite side of the QR Code, any uneven spacing being accommodated between the timing pattern and
     * the first alignment pattern on the right side of the timing pattern.
     * We will also need the intervals to be even, so that they fit with the timing patterns.
     *
     * This class is used by the QRCodeInfos subclass to store it. Since this is a full-on bonus extension, we
     * preferred to put it here so that it is clearly named inside the Extensions namespace. Theoretically, this should
     * be in the QRCodeInfos sub-class.
     *
     * TODO Create a unit test for the following function.
     *
     * @param version The QR code version, must be within 1 and 40 inclusive
     * @return The list of coordinates for the alignment patterns
     */
    private static int[] getAlignmentPositions(int version) {
        if (version > 40 || version < 1)
            throw new IllegalArgumentException("QR Code versions must be within 1 and 40 included.");

        int patternAmount = version / 7 + 2;
        int[] returnArray = new int[patternAmount];
        int matrixSize = qrcode.QRCodeInfos.getMatrixSize(version); // TODO maybe change that

        returnArray[0] = ALIGNMENT_PATTERNS_FIRST_POSITION; // first position is always 6 (on the column of timing patterns)

        /*
         * This calculation computes the interpolation of the two end points which we know to get the amount of steps
         * between all of the coordinates for the alignment patterns.
         *
         * We need three different things:
         *      - The last position of the QR code which needs to be exactly opposite to the QR code placed on the
         *        timing pattern
         *      - The second to last position in order to find the step between each alignment pattern. This is done
         *        with the following calculation:
         *              x_{n-1} = (x_0 + x_n * (n - 2)) / (n - 1)
         *              where x_n is the nth coordinate and n the amount of coordinates needed
         *        This calculation allows us to find the second to last position. Since we are using integer divisions,
         *        in some rare cases (where the result above has a decimal part above 0.5) the result will not be
         *        rounded properly. This is why we add half of the division up front. The calculation then becomes:
         *              x_{n-1} = (x_0 + x_n * (n - 2) + ((n - 1) / 2)) / (n - 1)
         *        Note that this second calculation only works if we use integer divisions.
         *        We then use a bitwise operation to round down to the closest even number to ensure our alignment
         *        patterns will be synced with the timing patterns. As specified, the difference this will create will
         *        only affect the distance between the timing pattern and the first alignment pattern on its right side.
         *      - Once we have this second to last position, we can safely calculate the step between the patterns and
         *        then calculate the position at which the second module in the line should go. This has to be defined
         *        from the last pattern position to ensure that any rounding loss from the operation mentioned
         *        herein-above is compensated.
         *
         *        Then it is a simple matter of adding the coordinates to the final array.
         */
        int lastPosition =  matrixSize - ALIGNMENT_PATTERNS_FIRST_POSITION - 1; // The column opposite to the timing pattern
        int secondLastPosition =
                (
                        (
                                ALIGNMENT_PATTERNS_FIRST_POSITION +
                                lastPosition * (patternAmount - 2) +
                                (patternAmount - 1) / 2
                        ) / (patternAmount - 1)
                ) & ~1;
        int positionStep = lastPosition - secondLastPosition;
        int secondPosition = lastPosition - (patternAmount - 2) * positionStep;

        for (int i = 1; i < patternAmount; i++) {
            returnArray[i] = secondPosition + (i - 1) * positionStep;
        }

        return returnArray;
    }

    /**
     * Revised function to add the alignment patterns for any QR code version
     *
     * @param matrix The matrix reference to edit
     * @param qrCodeInfos The properties class of the QRCode
     */
    public static void addAlignmentPatterns(int[][] matrix, QRCodeInfos qrCodeInfos) {
        if (qrCodeInfos.getVersion() < 2) return;

        int[] coordinates = qrCodeInfos.getAlignmentPatternsCoordinates();

        for (int i : coordinates) {
            for (int j : coordinates) {
                if (matrix[i][j] == 0) {
                    MatrixConstruction.addPattern(
                            MatrixConstruction.Pattern.ALIGNMENTPATTERN,
                            MatrixConstruction.Anchor.CENTER,
                            matrix,
                            j, i);
                }
            }
        }
    }

    public static void addFormatInformation(int[][] matrix, QRCodeInfos infos) {
        boolean[] formatSequence = infos.getFormatSequence();

        for (int i = 0; i < formatSequence.length; i++) {
            matrix[i < 6 ? i : i == 6 ? i + 1 : 8][i > 8 ? 14 - i : i == 8 ? i - 1 : 8] = formatSequence[i] ? MatrixConstruction.B : MatrixConstruction.W;
            matrix[i < 7 ? 8 : matrix.length - 8 + i - 7][i > 6 ? 8 :  matrix.length - i - 1] = formatSequence[i] ? MatrixConstruction.B : MatrixConstruction.W;
        }
    }


    /**
     * Inner extensions class used to extend the functions defined in QRCodeInfos.java.
     * We are only changing the functions to be able to implement QR Code versions from 1 to 40.
     *
     * It would have helped if the QRCodeInfos class was not final, but we can't modify this file, so this is
     * a quick workaround.
     */
    public static class QRCodeInfos {

        public enum CorrectionLevel {
            LOW     (0),
            MEDIUM  (1),
            QUARTILE(2),
            HIGH    (3);

            // Error Correction Codewords Characteristics (see Tables 13 to 22 of ISO/IEC 18004:2000(E))
            // VERSION -> CORRECTION LEVEL -> BLOCK -> {TOTAL ECC BLOCKS, TOTAL ECC, TOTAL DATA CODEWORD}
            private int[][][][] eccCharacteristics = new int[][][][] {
                    {
                            {{7, 1, 19}},
                            {{10, 1, 16}},
                            {{13, 1, 13}},
                            {{17, 1, 9}}
                    },
                    {
                            {{10, 1, 34}},
                            {{16, 1, 28}},
                            {{22, 1, 22}},
                            {{28, 1, 16}}
                    },
                    {
                            {{15, 1, 55}},
                            {{26, 1, 44}},
                            {{18, 2, 17}},
                            {{22, 2, 13}}
                    },
                    {
                            {{20, 1, 80}},
                            {{18, 2, 32}},
                            {{26, 2, 24}},
                            {{16, 4, 9}}
                    },
                    {
                            {{26, 1, 108}},
                            {{24, 2, 43}},
                            {{18, 2, 15}, {18, 2, 16}},
                            {{22, 2, 11}, {22, 2, 12}}
                    },
                    {
                            {{18, 2, 68}},
                            {{16, 4, 27}},
                            {{24, 4, 19}},
                            {{28, 4, 15}}
                    },
                    {
                            {{20, 2, 78}},
                            {{18, 4, 31}},
                            {{18, 2, 14}, {18, 4, 15}},
                            {{26, 4, 13}, {26, 1, 14}}
                    },
                    {
                            {{24, 2, 97}},
                            {{22, 2, 38}, {22, 2, 39}},
                            {{22, 4, 18}, {22, 2, 19}},
                            {{26, 4, 14}, {26, 2, 15}}
                    },
                    {
                            {{30, 2, 116}},
                            {{22, 3, 36}, {22, 2, 37}},
                            {{20, 4, 16}, {20, 4, 17}},
                            {{24, 4, 12}, {24, 4, 13}}
                    },
                    {
                            {{18, 2, 68}, {18, 2, 69}},
                            {{26, 4, 43}, {26, 1, 44}},
                            {{24, 6, 19}, {24, 2, 20}},
                            {{28, 6, 15}, {28, 2, 16}}
                    },
                    {
                            {{20, 4, 81}},
                            {{30, 1, 50}, {30, 4, 51}},
                            {{28, 4, 22}, {28, 4, 23}},
                            {{24, 3, 12}, {24, 8, 13}}
                    },
                    {
                            {{24, 2, 92}, {24, 2, 93}},
                            {{22, 6, 36}, {22, 2, 37}},
                            {{26, 4, 20}, {26, 6, 21}},
                            {{28, 7, 14}, {28, 4, 15}}
                    },
                    {
                            {{26, 4, 107}},
                            {{22, 8, 37}, {22, 1, 38}},
                            {{24, 8, 20}, {24, 4, 21}},
                            {{22, 12, 11}, {22, 4, 12}}
                    },
                    {
                            {{30, 3, 115}, {30, 1, 116}},
                            {{24, 4, 40}, {24, 5, 41}},
                            {{20, 11, 16}, {20, 5, 17}},
                            {{24, 11, 12}, {24, 5, 13}}
                    },
                    {
                            {{22, 5, 87}, {22, 1, 88}},
                            {{24, 5, 41}, {24, 5, 42}},
                            {{30, 5, 24}, {30, 7, 25}},
                            {{24, 11, 12}, {24, 7, 13}}
                    },
                    {
                            {{24, 5, 98}, {24, 1, 99}},
                            {{28, 7, 45}, {28, 3, 46}},
                            {{24, 15, 19}, {24, 2, 20}},
                            {{30, 3, 15}, {30, 13, 16}}
                    },
                    {
                            {{28, 1, 107}, {28, 5, 108}},
                            {{28, 10, 46}, {28, 1, 47}},
                            {{28, 1, 22}, {28, 15, 23}},
                            {{28, 2, 14}, {28, 17, 15}}
                    },
                    {
                            {{30, 5, 120}, {30, 1, 121}},
                            {{26, 9, 43}, {26, 4, 44}},
                            {{28, 17, 22}, {28, 1, 23}},
                            {{28, 2, 14}, {28, 19, 15}}
                    },
                    {
                            {{28, 3, 113}, {28, 4, 114}},
                            {{26, 3, 44}, {26, 11, 45}},
                            {{26, 17, 21}, {26, 4, 22}},
                            {{26, 9, 13}, {26, 16, 14}}
                    },
                    {
                            {{28, 3, 107}, {28, 5, 108}},
                            {{26, 3, 41}, {26, 13, 42}},
                            {{30, 15, 24}, {30, 5, 25}},
                            {{28, 15, 15}, {28, 10, 16}}
                    },
                    {
                            {{28, 4, 116}, {28, 4, 117}},
                            {{26, 17, 42}},
                            {{28, 17, 22}, {28, 6, 23}},
                            {{30, 19, 16}, {30, 6, 17}}
                    },
                    {
                            {{28, 2, 111}, {28, 7, 112}},
                            {{28, 17, 46}},
                            {{30, 7, 24}, {30, 16, 25}},
                            {{24, 34, 13}}
                    },
                    {
                            {{30, 4, 121}, {30, 5, 122}},
                            {{28, 4, 47}, {28, 14, 48}},
                            {{30, 11, 24}, {30, 14, 25}},
                            {{30, 16, 15}, {30, 14, 16}}
                    },
                    {
                            {{30, 6, 117}, {30, 4, 118}},
                            {{28, 6, 45}, {28, 14, 46}},
                            {{30, 11, 24}, {30, 16, 25}},
                            {{30, 30, 16}, {30, 2, 17}}
                    },
                    {
                            {{26, 8, 106}, {26, 4, 107}},
                            {{28, 8, 47}, {28, 13, 48}},
                            {{30, 7, 24}, {30, 22, 25}},
                            {{30, 22, 15}, {30, 13, 16}}
                    },
                    {
                            {{28, 10, 114}, {28, 2, 115}},
                            {{28, 19, 46}, {28, 4, 47}},
                            {{28, 28, 22}, {28, 6, 23}},
                            {{30, 33, 16}, {30, 4, 17}}
                    },
                    {
                            {{30, 8, 122}, {30, 4, 123}},
                            {{28, 22, 45}, {28, 3, 46}},
                            {{30, 8, 23}, {30, 26, 24}},
                            {{30, 12, 15}, {30, 28, 16}}
                    },
                    {
                            {{30, 3, 117}, {30, 10, 118}},
                            {{28, 3, 45}, {28, 23, 46}},
                            {{30, 4, 24}, {30, 31, 25}},
                            {{30, 11, 15}, {30, 31, 16}}
                    },
                    {
                            {{30, 7, 116}, {30, 7, 117}},
                            {{28, 21, 45}, {28, 7, 46}},
                            {{30, 1, 23}, {30, 37, 24}},
                            {{30, 19, 15}, {30, 26, 16}}
                    },
                    {
                            {{30, 5, 115}, {30, 10, 116}},
                            {{28, 19, 47}, {28, 10, 48}},
                            {{30, 15, 24}, {30, 25, 25}},
                            {{30, 23, 15}, {30, 25, 16}}
                    },
                    {
                            {{30, 13, 115}, {30, 3, 116}},
                            {{28, 2, 46}, {28, 29, 47}},
                            {{30, 42, 24}, {30, 1, 25}},
                            {{30, 23, 15}, {30, 28, 16}}
                    },
                    {
                            {{30, 17, 115}},
                            {{28, 10, 46}, {28, 23, 47}},
                            {{30, 10, 24}, {30, 35, 25}},
                            {{30, 19, 15}, {30, 35, 16}}
                    },
                    {
                            {{30, 17, 115}, {30, 1, 116}},
                            {{28, 14, 46}, {28, 21, 47}},
                            {{30, 29, 24}, {30, 19, 25}},
                            {{30, 11, 15}, {30, 46, 16}}
                    },
                    {
                            {{30, 13, 115}, {30, 6, 116}},
                            {{28, 14, 46}, {28, 23, 47}},
                            {{30, 44, 24}, {30, 7, 25}},
                            {{30, 59, 16}, {30, 1, 17}}
                    },
                    {
                            {{30, 12, 121}, {30, 7, 122}},
                            {{28, 12, 47}, {28, 26, 48}},
                            {{30, 39, 24}, {30, 14, 25}},
                            {{30, 22, 15}, {30, 41, 16}}
                    },
                    {
                            {{30, 6, 121}, {30, 14, 122}},
                            {{28, 6, 47}, {28, 34, 48}},
                            {{30, 46, 24}, {30, 10, 25}},
                            {{30, 2, 15}, {30, 64, 16}}
                    },
                    {
                            {{30, 17, 122}, {30, 4, 123}},
                            {{28, 29, 46}, {28, 14, 47}},
                            {{30, 49, 24}, {30, 10, 25}},
                            {{30, 24, 15}, {30, 46, 16}}
                    },
                    {
                            {{30, 4, 122}, {30, 18, 123}},
                            {{28, 13, 46}, {28, 32, 47}},
                            {{30, 48, 24}, {30, 14, 25}},
                            {{30, 42, 15}, {30, 32, 16}}
                    },
                    {
                            {{30, 20, 117}, {30, 4, 118}},
                            {{28, 40, 47}, {28, 7, 48}},
                            {{30, 43, 24}, {30, 22, 25}},
                            {{30, 10, 15}, {30, 67, 16}}
                    },
                    {
                            {{30, 19, 118}, {30, 6, 119}},
                            {{28, 18, 47}, {28, 31, 48}},
                            {{30, 34, 24}, {30, 34, 25}},
                            {{30, 20, 15}, {30, 61, 16}}
                    }
            };

            private int[] errorCorrectionLevelBits = new int[] {
                    1,
                    0,
                    3,
                    2
            };

            private Map<Integer, ErrorCorrectionBlock[]> errorCorrectionBlockList;
            private int id;

            private Map<Integer, ErrorCorrectionBlock[]> createErrorCorrectionBlockList(int correctionLevel) {
                Map<Integer, ErrorCorrectionBlock[]> eccList = new HashMap<>();

                for (int v = 0; v < eccCharacteristics.length; v++) {
                    int[][] blocks = eccCharacteristics[v][correctionLevel];
                    ErrorCorrectionBlock[] eccBlocks = new ErrorCorrectionBlock[blocks.length];

                    for (int i = 0; i < blocks.length; i++) {
                        eccBlocks[i] = new ErrorCorrectionBlock(blocks[i][1], blocks[i][2]);
                    }

                    eccList.put(v, eccBlocks);
                }

                return eccList;
            }

            CorrectionLevel(int id) {
                this.id = id;
                errorCorrectionBlockList = createErrorCorrectionBlockList(id);
            }

            public Map<Integer, ErrorCorrectionBlock[]> getErrorCorrectionBlockList() { return errorCorrectionBlockList; }
            public int getErrorCorrectionLevelBit() { return errorCorrectionLevelBits[this.id]; }
            public ErrorCorrectionBlocks getErrorCorrectionBlocks(int version) {
                ErrorCorrectionBlock[] list = errorCorrectionBlockList.get(version - 1);
                if (errorCorrectionBlockList.get(version - 1).length == 2)
                    return new ErrorCorrectionBlocks(eccCharacteristics[version - 1][this.id][0][0], list[0], list[1]);
                else
                    return new ErrorCorrectionBlocks(eccCharacteristics[version - 1][this.id][0][0], list[0]);
            }
        }

        private final int version;
        private final int mask;
        private final CorrectionLevel correctionLevel;
        private final int[] alignmentPatternsCoordinates;
        private final ErrorCorrectionBlocks errorCorrectionBlocks;

        QRCodeInfos(int version, int mask, CorrectionLevel correctionLevel) {
            if (version < 1 || version > 40)
                throw new IllegalArgumentException("Version must be between 1 and 40 included.");
            this.version = version;
            this.mask = mask;
            this.correctionLevel = correctionLevel;
            this.alignmentPatternsCoordinates = getAlignmentPositions(this.version);
            this.errorCorrectionBlocks = correctionLevel.getErrorCorrectionBlocks(this.version);
        }

        // Getters
        public int getVersion() { return version; }
        public CorrectionLevel getCorrectionLevel() { return correctionLevel; }
        public int[] getAlignmentPatternsCoordinates() { return alignmentPatternsCoordinates; }

        /**
         * Get the maximum input length for a given QR code version
         *
         * @return the maximum number of bytes of data that can be encoded for the given version
         */
        public int getMaxInputLength() {
            if(version > 40 || version < 1) {
                throw new UnsupportedOperationException("The version has to be between 1 and 40 included.");
            }
            return errorCorrectionBlocks.getAmountDataCodewords() - 2 ;
        }

        /**
         * Get the number of error correction codewords needed for a given version
         *
         * @return
         */
        public int getECCLength() {
            if(version > 40 || version < 1) {
                throw new UnsupportedOperationException("The version has to be between 1 and 40 included.");
            }
            return errorCorrectionBlocks.getAmountErrorCorrectionCodewords();
        }

        /**
         * Get the number of codewords encoding the data for a given version
         *
         * @return the number of codewords in the version
         */
        public int getCodeWordsLength() {
            if(version > 40 || version < 1) {
                throw new UnsupportedOperationException("The version has to be between 1 and 40 included.");
            }
            return errorCorrectionBlocks.getAmountDataCodewords() ;
        }

        /**
         * A somewhat copy of the QRCodeInfos method to be able to generate format sequences for different masks
         *
         * @see qrcode.QRCodeInfos#getFormatSequence(int)
         *
         * @return An array of booleans corresponding to the bits that need to be placed in the QR code
         */
        public boolean[] getFormatSequence() {
            int code = ((correctionLevel.getErrorCorrectionLevelBit() & 0x3) << 3) | (mask & 0x7);
            int current = code << 10;

            int poly = 0b10100110111;
            int size = 15;
            while(((0b1<<(size-1)) & current) ==0) {
                size--;
                if(size == 0) {
                    throw new IllegalAccessError();
                }
            }

            while(size>10) {
                int paddedPoly = poly<<(size-11);

                current = paddedPoly^current;


                while(((0b1<<(size-1)) & current) == 0) {
                    size--;
                    if(size == 0) {
                        throw new IllegalAccessError();
                    }
                }
            }

            int format = (code<<10 | (current& 0x3FF)) ^ 0b101010000010010;

            boolean[] formatPixels = new boolean[15];
            for(int i=0;i<formatPixels.length;i++) {
                formatPixels[i] = !(((format >> (14 - i)) & 0b1) == 0);
            }

            return formatPixels;
        }

    }

    /*
     * The following to classes are built to be in accordance with ISO/IEC 18004:2000(E)
     * They allow to easily link all of the data in Tables 13 to 22 to their corresponding version and correction level.
     *
     * They are constructed in the QRCodeInfos class using the same tables (possibly the most disgusting code ever,
     * but it is how the spec has been built...)
     */

    /**
     * Class to hold every error correction blocks for each versions of QR codes.
     */
    public static final class ErrorCorrectionBlocks {
        private final int errorCorrectionCodewordsPerBlock;
        private final ErrorCorrectionBlock[] errorCorrectionBlockList;

        ErrorCorrectionBlocks (int errorCorrectionCodewordsPerBlock, ErrorCorrectionBlock... errorCorrectionBlocks) {
            this.errorCorrectionCodewordsPerBlock = errorCorrectionCodewordsPerBlock;
            this.errorCorrectionBlockList = errorCorrectionBlocks;
        }

        public int getErrorCorrectionCodewordsPerBlock() { return errorCorrectionCodewordsPerBlock; }
        public int getAmountErrorCorrectionCodewords() { return errorCorrectionCodewordsPerBlock * getAmountBlocks(); }
        public int getAmountDataCodewords() {
            int sum = 0;
            for (ErrorCorrectionBlock ecb : errorCorrectionBlockList) {
                sum += ecb.getAmount() * ecb.getDataCodewordsAmount();
            }
            return sum;
        }
        public int getAmountBlocks() {
            int sum = 0; for (ErrorCorrectionBlock ecb : errorCorrectionBlockList) { sum += ecb.getAmount(); } return sum;
        }
        public ErrorCorrectionBlock[] getErrorCorrectionBlockList() { return errorCorrectionBlockList; }
    }

    /**
     * Class to define one correction block in a version for a certain correction level.
     */
    public static final class ErrorCorrectionBlock {
        private final int amount;
        private final int dataCodewordsAmount;

        ErrorCorrectionBlock (int amount, int dataCodewordsAmount) {
            this.amount = amount;
            this.dataCodewordsAmount = dataCodewordsAmount;
        }

        public int getAmount() { return amount; }
        public int getDataCodewordsAmount() { return dataCodewordsAmount; };
    }

}
