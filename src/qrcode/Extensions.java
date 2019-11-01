package qrcode;

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

    private final static int ALIGNMENT_PATTERNS_FIRST_POSITION = 6;

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
     * TODO Create a unit test for the following function.
     *
     * @param version The QR code version, must be within 1 and 40 inclusive
     * @return The list of coordinates for the alignment patterns
     */
    public static int[] getAlignmentPositions(int version) {
        if (version > 40 || version < 1)
            throw new IllegalArgumentException("QR Code versions must be within 1 and 40 included.");

        int patternAmount = version / 7 + 2;
        int[] returnArray = new int[patternAmount];
        int matrixSize = QRCodeInfos.getMatrixSize(version);

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
     * @param version The version of the QR code
     */
    public static void addAlignmentPatterns(int[][] matrix, int version) {
        if (version < 2) return;

        int[] coordinates = getAlignmentPositions(version);

        for (int i : coordinates) {
            for (int j : coordinates) {
                if (matrix[i][j] == 0 || j == 6) {
                    MatrixConstruction.addPattern(
                            MatrixConstruction.Pattern.ALIGNMENTPATTERN,
                            MatrixConstruction.Anchor.CENTER,
                            matrix,
                            j, i);
                }
            }
        }
    }


    /**
     * Inner extensions class used to extend the functions defined in QRCodeInfos.java.
     * We are only changing the functions to be able to implement QR Code versions from 1 to 40.
     *
     * It would have helped if the QRCodeInfos class was not final, but we can't modify this file, so this is
     * a quick workaround.
     */
    // public static class QRCodeInfos {}

}
