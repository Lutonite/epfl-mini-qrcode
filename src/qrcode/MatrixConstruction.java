package qrcode;

public class MatrixConstruction {

    /**
     * Change this boolean to enable the bonuses that we have done which could potentially break
     * automatic graders or add methods that are not mentioned in the documentation. This will make it
     * so most methods will use the ones defined in the Extensions class and static subclasses.
     *
     * @see Extensions
     */
    public static boolean USE_EXTENSIONS = false;

	/*
	 * Constants defining the color in ARGB format
	 *
	 * W = White ARGB integer
	 * B = Black ARGB integer
	 * 
	 * both needs to have their alpha component to 255
	 */
	public final static int W = 0xFF_FF_FF_FF;
	public final static int B = 0xFF_00_00_00;

	/**
	 * Pattern definitions, any pattern can be added with the following properties:
	 * 		- int[][] patternMatrix (required)       The pattern, for alternating patterns it must only be the recurring part
	 * 	 	- boolean borders		(default: false) Whether the pattern requires white borders to be added
	 * 	 	- boolean alternating	(default: false) Whether the pattern is an alternating sequence or not
	 */
	public enum Pattern {
		FINDERPATTERN 	(1),
		ALIGNMENTPATTERN(2),
		TIMINGPATTERNCOL(3),
		TIMINGPATTERNROW(4);

		private int[][] patternMatrix;
		private boolean recurring = false;
		private boolean borders = false;

		Pattern(int patternCode) {
			switch(patternCode) {
				case 1:
					patternMatrix = new int[][] {
							{1,1,1,1,1,1,1},
							{1,0,0,0,0,0,1},
							{1,0,1,1,1,0,1},
							{1,0,1,1,1,0,1},
							{1,0,1,1,1,0,1},
							{1,0,0,0,0,0,1},
							{1,1,1,1,1,1,1}
					};
					borders = true;
					break;
				case 2:
					patternMatrix = new int[][] {
							{1,1,1,1,1},
							{1,0,0,0,1},
							{1,0,1,0,1},
							{1,0,0,0,1},
							{1,1,1,1,1}
					};
					break;
				case 3:
					patternMatrix = new int[][] {
							{1},
							{0}
					};
					recurring = true;
					break;
				case 4:
					patternMatrix = new int[][] {
							{1, 0}
					};
					recurring = true;
					break;
			}
		}
		
		private int[] sizeOfMatrix() {
			return new int[] {patternMatrix.length, patternMatrix[0].length};
		}

		// Getters
		public int[][] getPatternMatrix() { return patternMatrix; }
		public int[] getSize() { return sizeOfMatrix(); }
		public boolean isRecurring() { return recurring; }
		public boolean hasBorders() { return borders; }
	}

	/**
	 * Interface declaration for Anchor's directions
	 */
	private interface Direction {
		int[] translateValues (int[] size);
	}

	/**
	 * Anchor points, unused anchor points are commented out but they can be added anytime.
	 * Implements the Direction interface to provide the translateValues function which, given the size of a rectangular
	 * 		pattern int[y][x] in the form of an array {y, x}, returns the translation coordinates to access the NORTH_EAST
	 * 		position. This simplifies any possible pattern iteration done afterwards.
	 *
	 * Commented anchor points are never used in this project and will probably never be used for QR code generation.
	 */
	public enum Anchor implements Direction {
		NORTH 	   {public int[] translateValues(int[] size) { return new int[] {0        , size[1]/2}; }},
		NORTH_EAST {public int[] translateValues(int[] size) { return new int[] {0        , size[1]  }; }},
//		EAST 	   {public int[] translateValues(int[] size) { return new int[] {size[0]/2, size[1]  }; }},
//		SOUTH_EAST {public int[] translateValues(int[] size) { return new int[] {size[0]  , size[1]  }; }},
//		SOUTH	   {public int[] translateValues(int[] size) { return new int[] {size[0]  , size[1]/2}; }},
		SOUTH_WEST {public int[] translateValues(int[] size) { return new int[] {size[0]  , 0        }; }},
		WEST	   {public int[] translateValues(int[] size) { return new int[] {size[0]/2, 0        }; }},
		NORTH_WEST {public int[] translateValues(int[] size) { return new int[] {0        , 0        }; }},
		CENTER	   {public int[] translateValues(int[] size) { return new int[] {size[0]/2, size[1]/2}; }}
	}

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @param mask
	 *            The mask used on the data. If not valid (e.g: -1), then no mask is
	 *            used.
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, int mask) {
	    if (USE_EXTENSIONS) {
	    	// We use our own QRCodeInfos class which is defined from the version and the correction level
			// Other parameters such as the mask are determined with the evaluator done in this file.
			int[][] matrix = initializeMatrix(version);

			Extensions.QRCodeInfos qrCodeInfos =
					new Extensions.QRCodeInfos(
							version,
							mask,
							Extensions.CORRECTION_LEVEL
					);

			Extensions.constructMatrix(matrix, qrCodeInfos);

			addDataInformation(matrix, data, mask);

			return matrix;
        } else {
            /*
             * PART 2
             */
            int[][] matrix = constructMatrix(version, mask);
            /*
             * PART 3
             */
            addDataInformation(matrix, data, mask);

            return matrix;
        }
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** PART 2 *********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create a matrix (2D array) ready to accept data for a given version and mask
	 * 
	 * @param version
	 *            the version number of QR code (has to be between 1 and 4 included)
	 * @param mask
	 *            the mask id to use to mask the data modules. Has to be between 0
	 *            and 7 included to have a valid matrix. If the mask id is not
	 *            valid, the modules would not be not masked later on, hence the
	 *            QRcode would not be valid
	 * @return the qrcode with the patterns and format information modules
	 *         initialized. The modules where the data should be remain empty.
	 */
	public static int[][] constructMatrix(int version, int mask) {
		int[][] m = initializeMatrix(version);
		addFinderPatterns(m);
		addAlignmentPatterns(m, version);
		addTimingPatterns(m);
		addDarkModule(m);
		addFormatInformation(m, mask);
		return m;
	}

	/**
	 * Create an empty 2d array of integers of the size needed for a QR code of the
	 * given version
	 * 
	 * @param version
	 *            the version number of the qr code (has to be between 1 and 4
	 *            included
	 * @return an empty matrix
	 */
	public static int[][] initializeMatrix(int version) {
		int size = QRCodeInfos.getMatrixSize(version);
		return new int[size][size];
	}

	/**
	 * Overloaded method for non-recurring patterns without maximum coordinates.
	 *
	 * @see MatrixConstruction#addPattern(Pattern, Anchor, int[][], int, int, int, int)
	 */
	public static void addPattern(Pattern p, Anchor a, int[][] matrix, int x, int y) {
		if (p.isRecurring())
			throw new IllegalArgumentException("Recurring patterns must have maximum coordinates");

		addPattern(p, a, matrix, x, y, -1, -1);
	}

	/**
	 * Adds a given pattern from specific coordinates around a given anchor in a matrix
	 *
	 * @param p A pattern to add
	 * @param a The anchor from which the pattern should be added
	 * @param matrix The matrix reference where the pattern should be added
	 * @param x The starting x coordinate from the anchor
	 * @param y The starting y coordinate from the anchor
	 * @param maxX The maximum x coordinate from the anchor (only used when the pattern is recurring)
	 * @param maxY The maximum y coordinate from the anchor (only used when the pattern is recurring)
	 */
	public static void addPattern(Pattern p, Anchor a, int[][] matrix, int x, int y, int maxX, int maxY) {
		for (int j = 0; j < (p.isRecurring() ? maxY - y + 1 : p.getSize()[0]); j++) {
			for (int i = 0; i < (p.isRecurring() ? maxX - x + 1 : p.getSize()[1]); i++) {
				if (p.isRecurring())
					matrix[j + y][i + x] = p.getPatternMatrix()[j % p.getSize()[0]][i % p.getSize()[1]] == 0 ? W : B;
				else
					matrix[j + y - a.translateValues(p.getSize())[0]][i + x - a.translateValues(p.getSize())[1]] =
							p.getPatternMatrix()[j][i] == 0 ? W : B;
			}
		}

		if (p.hasBorders()) {
			for (int j = -1; j <= p.getSize()[0]; j++) {
				for (int i = -1; i <= p.getSize()[1]; i++) {
					int xCord = i + x - a.translateValues(p.getSize())[1];
					int yCord = j + y - a.translateValues(p.getSize())[0];

					if ((j == -1 && yCord >= 0 && i > -1) ||
							(i == -1 && xCord >= 0 && j > -1) ||
							(j == p.getSize()[0] && yCord <= p.getSize()[0] && xCord > -1 && xCord < matrix.length) ||
							(i == p.getSize()[1] && xCord <= p.getSize()[1] && yCord > -1 && yCord < matrix.length)) {
						matrix[yCord][xCord] = W;
					}
				}
			}
		}
	}

	/**
	 * Add all finder patterns to the given matrix with a border of White modules.
	 * 
	 * @param matrix
	 *            the 2D array to modify: where to add the patterns
	 */
	public static void addFinderPatterns(int[][] matrix) {
		addPattern(Pattern.FINDERPATTERN, Anchor.NORTH_WEST, matrix, 0         , 0         );
		addPattern(Pattern.FINDERPATTERN, Anchor.NORTH_EAST, matrix, matrix.length, 0         );
		addPattern(Pattern.FINDERPATTERN, Anchor.SOUTH_WEST, matrix, 0         , matrix.length);
	}

	/**
	 * Add the alignment pattern if needed, does nothing for version 1
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 * @param version
	 *            the version number of the QR code needs to be between 1 and 4
	 *            included
	 */
	public static void addAlignmentPatterns(int[][] matrix, int version) {
		if (version >= 2)
			addPattern(Pattern.ALIGNMENTPATTERN, Anchor.CENTER, matrix, matrix.length - 7, matrix.length - 7);
	}

	/**
	 * Add the timings patterns
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 */
	public static void addTimingPatterns(int[][] matrix) {
		addPattern(Pattern.TIMINGPATTERNROW, Anchor.WEST , matrix, 8, 6, matrix.length - 8, 6);
		addPattern(Pattern.TIMINGPATTERNCOL, Anchor.NORTH, matrix, 6, 8, 6, matrix.length - 8);
	}

	/**
	 * Add the dark module to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code
	 */
	public static void addDarkModule(int[][] matrix) {
		matrix[8][matrix.length - 8] = B;
	}

	/**
	 * Add the format information to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code to modify
	 * @param mask
	 *            the mask id
	 */
	public static void addFormatInformation(int[][] matrix, int mask) {
		boolean[] formatSequence = QRCodeInfos.getFormatSequence(mask);

		for (int i = 0; i < formatSequence.length; i++) {
			matrix[i < 6 ? i : i == 6 ? i + 1 : 8][i > 8 ? 14 - i : i == 8 ? i - 1 : 8] = formatSequence[i] ? B : W;
			matrix[i < 7 ? 8 : matrix.length - 8 + i - 7][i > 6 ? 8 : matrix.length - i - 1] = formatSequence[i] ? B : W;
		}
	}

	/*
	 * =======================================================================
	 * ****************************** PART 3 *********************************
	 * =======================================================================
	 */

	/**
	 * Choose the color to use with the given coordinate using the masking 0
	 * 
	 * @param col
	 *            x-coordinate
	 * @param row
	 *            y-coordinate
	 * @param dataBit
	 *
	 * @return the color with the masking
	 */
	public static int maskColor(int col, int row, boolean dataBit, int masking) {
		boolean applyMask;

		int i = ((row * col) % 2) + ((row * col) % 3);
		switch (masking) {
			case 0: applyMask = (col + row) % 2 == 0; break;
			case 1: applyMask = row % 2 == 0; break;
			case 2: applyMask = col % 3 == 0; break;
			case 3: applyMask = (col + row) % 3 == 0; break;
			case 4: applyMask = ((row / 2) + (col / 3)) % 2 == 0; break;
			case 5: applyMask = i == 0; break;
			case 6: applyMask = i % 2 == 0; break;
			case 7: applyMask = (((row + col) % 2) + ((row * col) % 3)) % 2 == 0; break;
			default: applyMask = false;
		}

		return applyMask ? dataBit ? W : B : dataBit ? B : W;
	}

	/**
	 * Add the data bits into the QR code matrix
	 * 
	 * @param matrix
	 *            a 2-dimensionnal array where the bits needs to be added
	 * @param data
	 *            the data to add
	 */
	public static void addDataInformation(int[][] matrix, boolean[] data, int mask) {
        int x = matrix.length - 1;
        int y = matrix.length - 1;

        int currentBit = 0;
        int direction = -1;

        while (x > 0) {
            // Skip vertical timing pattern
            if (x == 6) x -= 1;

            while (y >= 0 && y < matrix.length) {
                for (int i = 0; i < 2; i++) {
                    int posX = x - i;

                    // If a bit is already placed there
                    if (matrix[posX][y] != 0) continue;

                    boolean bitToPlace;
                    if (currentBit < data.length) {
                        bitToPlace = data[currentBit];
                        currentBit++;
                    } else {
                        bitToPlace = false;
                    }

                    matrix[posX][y] = maskColor(posX, y, bitToPlace, mask);
                }
                y += direction;
            }

            direction = -direction;
            y += direction;

            // since qr codes are always odd sized, we will never get an out of bounds since we skip one column
            // to avoid the vertical timing patten at x = 6.
            x -= 2;
        }
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** BONUS **********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * The mask is computed automatically so that it provides the least penalty
	 * 
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data) {
		int mask = findBestMasking(version, data);

		return renderQRCodeMatrix(version, data, mask);
	}

	/**
	 * Find the best mask to apply to a QRcode so that the penalty score is
	 * minimized. Compute the penalty score with evaluate
	 * 
	 * @param version
	 * 			Version of the QR Code
	 * @param data
	 * 			Data to add
	 * @return the mask number that minimize the penalty
	 */
	public static int findBestMasking(int version, boolean[] data) {
		// TODO BONUS
		return 0;
	}

	private static final int PENALTY_N1 = 3;
	private static final int PENALTY_N2 = 3;
	private static final int PENALTY_N3 = 40;
	private static final int PENALTY_N4 = 10;

	/**
	 * Compute the penalty score of a matrix
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @return the penalty score obtained by the QR code, lower the better
	 */
	public static int evaluate(int[][] matrix) {
		int penality = 0;
		
		//STEP 1
		for (int d = 0; d <= 1; ++d) {
			int count = 0;
			for (int i = 0; i < matrix.length; ++i) {
				for (int j = 0; j < matrix[i].length; ++j) {
					int x = 0;
					int y = 0;
					
					if (d == 0)
						x = 1;
					else
						y = 1;
					
					if ((j == 0 && y == 1) || (i == 0 && x == 1) || (matrix[i-x][j-y] == matrix[i][j]))
						count++;
					else
						count = 0;
					
					if (count == 5)
						penality += PENALTY_N1;
					else if (count > 5)
						++penality;
				}
				count = 0;
			}
		}
		System.out.println("STEP 1 : " + penality);
		int p = penality;
		
		//STEP 2
		for (int i = 0; i < matrix.length; ++i) {
			for (int j = 0; j < matrix[i].length; ++j) {
				if (i != 0 && j != 0 && i != matrix.length -1 && j != matrix.length -1) {
					if (matrix[i][j] == matrix[i][j+1]
						&& matrix[i][j] == matrix[i+1][j+1]
						&& matrix [i][j] == matrix[i][j+1]) {
						penality += 3;
					}
				}
			}
		}
		System.out.println("STEP 2 : " + (penality - p));
		p = penality;
		
		//STEP 3
		int[][] matrixWhiteBorders = new int[matrix.length+2][matrix.length+2];
		for (int i = 0; i < matrixWhiteBorders.length; ++i) {
			for (int j = 0; j < matrixWhiteBorders[i].length; ++j) {
				if (i == 0 || j == 0 || i == matrixWhiteBorders.length - 1 || j == matrixWhiteBorders[i].length - 1) {
					matrixWhiteBorders[i][j] = W;
				} else {
					matrixWhiteBorders[i][j] = matrix[i-1][j-1];
				}
			} 
		}
		
		int[] tab1 = {W, W, W, W, B, W, B, B, B, W, B};
		int[] tab2 = {B, W, B, B, B, W, B, W, W, W, W};
		for (int d = 0; d <= 1; ++d) {
			for (int i = 0; i < matrixWhiteBorders.length; ++i) {
				for (int j = 0; j < matrixWhiteBorders[i].length; ++j) {
					if ((j < matrixWhiteBorders.length - 12 && d == 0) || (i < matrixWhiteBorders.length - 12 && d == 1)) {
						for (int k = 0; k < tab1.length; ++k) {
							if (d == 0) {
								if (tab1[k] != matrixWhiteBorders[i][j+k]) {
									break;
								}
							} else {
								if (tab1[k] != matrixWhiteBorders[i+k][j]) {
									break;
								}
							}
							
							if (k == tab1.length - 1)
								penality += 40;
						}
						for (int k = 0; k < tab2.length; ++k) {
							if (d == 0) {
								if (tab2[k] != matrixWhiteBorders[i][j+k]) {
									break;
								}
							} else {
								if (tab2[k] != matrixWhiteBorders[i+k][j]) {
									break;
								}
							}
							
							if (k == tab2.length - 1)
								penality += 40;
						}
					}
				}
			}
		}

		System.out.println("STEP 3 : " + (penality - p));
		
		int modulesTotal = matrix.length * matrix.length;
		int darkModulesTotal = 0;

		for (int[] ints : matrix) {
			for (int anInt : ints) {
				if (anInt == B) darkModulesTotal++;
			}
		}

		int fivePercentVariances = Math.abs(darkModulesTotal * 2 - modulesTotal) * 10 / modulesTotal;
		System.out.println(fivePercentVariances*2);

		return penality;
	}
}
