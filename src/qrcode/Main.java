package qrcode;

public class Main {

	public static final String INPUT =  "Hello";

	/*
	 * Parameters
	 */
	public static final int VERSION = 10;
	public static final int MASK = 0;
	public static final int SCALING = 20;

	public static final boolean USE_EXTENSIONS = true;

	public static void main(String[] args) {

		// Encoding
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, VERSION);
		
		// Image
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData,MASK);

		// Visualization
		Helpers.show(qrCode, SCALING);

		System.out.println(MatrixConstruction.evaluate(qrCode));
	}

}
