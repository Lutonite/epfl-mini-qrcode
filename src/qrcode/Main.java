package qrcode;

public class Main {

	public static final String INPUT = "";

	/*
	 * Parameters
	 */
	public static final int VERSION = 40;
	public static final int MASK = 2;
	public static final int SCALING = 4;

	public static void main(String[] args) {

		// Encoding
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, VERSION);
		
		// Image
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData);

		// Visualization
		Helpers.show(qrCode, SCALING);

	}

}
