package qrcode;

public class Main {

	public static final String INPUT = "éééée";

	/*
	 * Parameters
	 */
	public static final int VERSION = 1;
	public static final int MASK = 0;
	public static final int SCALING = 20;

	public static void main(String[] args) {

		// Encoding
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, VERSION);
		
		// Image
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData, MASK);

		// Visualization
		Helpers.show(qrCode, SCALING);

	}

}
