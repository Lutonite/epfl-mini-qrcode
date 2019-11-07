package qrcode;

public class Main {

	public static final String INPUT =  "utfbjewiofhbsjii3zuf9w24zcuirpwh497f482gdchureic6z2394z2fibczt3iv6g2vt534z79528";

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
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData, MASK);

		// Visualization
		Helpers.show(qrCode, SCALING);

		System.out.println(MatrixConstruction.evaluate(qrCode));

	}

}
