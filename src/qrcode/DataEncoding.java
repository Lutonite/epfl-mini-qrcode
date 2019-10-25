package qrcode;

import java.nio.charset.StandardCharsets;

import reedsolomon.ErrorCorrectionEncoding;

public final class DataEncoding {

	/**
	 * @param input
	 * @param version
	 * @return
	 */
	public static boolean[] byteModeEncoding(String input, int version) {
		return bytesToBinaryArray(addErrorCorrection(fillSequence(addInformations(encodeString(input, QRCodeInfos.getMaxInputLength(version))), QRCodeInfos.getCodeWordsLength(version)), QRCodeInfos.getECCLength(version)));
	}

	/**
	 * @param input
	 *            The string to convert to ISO-8859-1
	 * @param maxLength
	 *          The maximal number of bytes to encode (will depend on the version of the QR code) 
	 * @return A array that represents the input in ISO-8859-1. The output is
	 *         truncated to fit the version capacity
	 */
	public static int[] encodeString(String input, int maxLength) {
		byte[] tabByte = input.getBytes(StandardCharsets.ISO_8859_1);
		int[] tabInt = new int[Math.min(tabByte.length, maxLength)];

		for (int i = 0; i < tabInt.length; i++) {
			tabInt[i] = tabByte[i] & 0xFF;
		}

		return tabInt;
	}

	/**
	 * Add the 12 bits information data and concatenate the bytes to it
	 * 
	 * @param inputBytes
	 *            the data byte sequence
	 * @return The input bytes with an header giving the type and size of the data
	 */
	public static int[] addInformations(int[] inputBytes) {
		int[] tabBytes = new int[inputBytes.length + 2];
		int inputLength = inputBytes.length & 0xFF;

		tabBytes[0] = (0b0100 << 4) + (inputLength >> 4);
		tabBytes[1] = ((inputLength - ((inputLength >> 4) << 4)) << 4) + (inputBytes[0] >> 4);

		for (int i = 1; i < tabBytes.length - 2; i++) {
			tabBytes[i+1] = ((inputBytes[i-1] - ((inputBytes[i-1] >> 4) << 4)) << 4) + (inputBytes[i] >> 4);
		}

		// TODO eventuellement optimiser
		tabBytes[tabBytes.length - 1] = (inputBytes[inputBytes.length - 1] - ((inputBytes[inputBytes.length - 1] >> 4) << 4)) << 4;

		return tabBytes;
	}

	/**
	 * Add padding bytes to the data until the size of the given array matches the
	 * finalLength
	 * 
	 * @param encodedData
	 *            the initial sequence of bytes
	 * @param finalLength
	 *            the minimum length of the returned array
	 * @return an array of length max(finalLength,encodedData.length) padded with
	 *         bytes 236,17
	 */
	public static int[] fillSequence(int[] encodedData, int finalLength) {
		if (finalLength <= encodedData.length) {
			return encodedData;
		} else {
			int[] output = new int[finalLength];
			for (int i = 0; i < finalLength; i++) {
				if (i < encodedData.length) {
					output[i] = encodedData[i];
				} else {
					if ((i - encodedData.length) % 2 == 0)
						output[i] = 0b11101100;
					else
						output[i] = 0b00010001;	
				}
			}
			
			return output;
		}
	}

	/**
	 * Add the error correction to the encodedData
	 * 
	 * @param encodedData
	 *            The byte array representing the data encoded
	 * @param eccLength
	 *            the version of the QR code
	 * @return the original data concatenated with the error correction
	 */
	public static int[] addErrorCorrection(int[] encodedData, int eccLength) {
		int[] errorEncoding = ErrorCorrectionEncoding.encode(encodedData, eccLength);
		int[] outputData = new int[encodedData.length + errorEncoding.length];

		for (int i = 0; i < outputData.length; i++) {
			if (i < encodedData.length) {
				outputData[i] = encodedData[i];
			} else {
				outputData[i] = errorEncoding[i - encodedData.length];
			}
		}

		return outputData;
	}

	/**
	 * Encode the byte array into a binary array represented with boolean using the
	 * most significant bit first.
	 * 
	 * @param data
	 *            an array of bytes
	 * @return a boolean array representing the data in binary
	 */
	public static boolean[] bytesToBinaryArray(int[] data) {
		// TODO Implementer
		return null;
	}

}
