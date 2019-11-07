package qrcode;

public class Main {

	public static final String INPUT = "nmkreonvsniovnwesjiovbnwesjionv";

	/*
	 * Parameters
	 */
	public static final int VERSION = 4;
	public static final int MASK = 2;
	public static final int SCALING = 18;

	public static void main(String[] args) {

		// Encoding
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, VERSION);
		
		// Image
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData);

		// Visualization
		Helpers.show(qrCode, SCALING);

	}

}
