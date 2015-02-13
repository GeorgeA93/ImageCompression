package com.allen.george.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

//Class full of helper methods that are used by the different algrithms
public class Utils {

	//The JPEG quant matrix stored as a double array
	public static double[][] quantMatArray = new double[][] { new double[] { 16, 11, 10, 16, 24, 40, 51, 61 }, new double[] { 12, 12, 14, 19, 26, 58, 60, 55 }, new double[] { 14, 13, 16, 24, 40, 57, 69, 56 }, new double[] { 14, 17, 22, 29, 51, 87, 80, 62 }, new double[] { 18, 22, 37, 56, 68, 109, 103, 77 }, new double[] { 24, 35, 55, 64, 81, 104, 113, 92 }, new double[] { 49, 64, 78, 87, 103, 121, 120, 101 }, new double[] { 72, 92, 95, 98, 112, 100, 103, 99 }

	};

	//Method to convert the quant double array to a matrix
	//Returns a matrix, quantMat
	public static Mat createQuantMat() {
		Mat quantMat = new Mat(new Size(8, 8), CvType.CV_32F); //create the matrix
		for (int y = 0; y < 8; y++) { //from 0 to 7
			for (int x = 0; x < 8; x++) { //from 0 to 7
				quantMat.put(y, x, quantMatArray[y][x]); //put the data into the matrix
			}
		}

		return quantMat; //return the matrix
	}

	//Method to divide two matrices by one another
	//Takes in two matrices, a and b
	//Returns a new matrix, res
	public static Mat divide(Mat a, Mat b) {
		Mat res = new Mat(a.size(), a.type()); //create the resultant matrix
		for (int y = 0; y < a.rows(); y++) { //loop through the rows of the matrix a
			for (int x = 0; x < a.cols(); x++) { //loop through the cols of the matrix a
				double[] result = new double[] { a.get(y, x)[0] / b.get(y, x)[0] }; //divide a by b
				res.put(y, x, result); //put the result into the matrix
			}
		}

		return res; //return the resultant matrix
	}

	//Method to multiply two matrices by one another
	//Takes in two matrices, a and b
	//Returns a new matrix, res
	public static Mat multiply(Mat a, Mat b) {
		Mat res = new Mat(a.size(), a.type()); //create the resultant matrix
		for (int y = 0; y < a.rows(); y++) {//loop through the rows of the matrix a
			for (int x = 0; x < a.cols(); x++) {//loop through the cols of the matrix a
				double[] result = new double[] { a.get(y, x)[0] * b.get(y, x)[0] };//times a by b
				res.put(y, x, result);//put the result into the matrix
			}
		}
		return res;//return the resultant matrix
	}

	//Method to reconstruct a list of matrices into one big matrix
	public static Mat reconstruct(List<Mat> blocks, Mat original) {
		
		//CONVERT THE LIST INTO A 2D ARRAY
		int a = original.rows() / 8;
		int b = original.cols() / 8;
		Mat[][] blockArray = new Mat[a][b];

		for(int i = 0; i < a; i ++){
			for(int j = 0; j < b; j ++){
				blockArray[i][j] = blocks.get(j % b + i * b);
			}
		}

		//CONVERT THE 2D ARRAY INTO ONE BIG MATRIX
		Mat result = new Mat(a * 8, b * 8, original.type());
		for (int y = 0; y < a; y++) {
			for (int x = 0; x < b; x++) {
				for (int m = 0; m < 8; m++) {
					for (int n = 0; n < 8; n++) {
						double[] values = blockArray[y][x].get(m, n);
						result.put(y * 8 + m, x * 8 + n, values);
					}
				}
			}
		}

		return result; //return the result
	}

	//Method to show an matrix in a JFrame
	public static void showResult(Mat img) {
		Imgproc.resize(img, img, new Size(640, 480)); //resize the image
		MatOfByte matOfByte = new MatOfByte(); 
		Highgui.imencode(".jpg", img, matOfByte); //convert to a byte array
		byte[] byteArray = matOfByte.toArray(); 
		BufferedImage bufImage = null; 
		try {
			InputStream in = new ByteArrayInputStream(byteArray); //read the byte array
			bufImage = ImageIO.read(in);
			JFrame frame = new JFrame();
			frame.getContentPane().add(new JLabel(new ImageIcon(bufImage))); //add it to a JFRAME
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Method to compress a string to a byte array using Javas GZIP library
	private static byte[] compressToByte(String data, String encoding) {
		byte[] bytes; //init the byte array
		try {
			bytes = data.getBytes(encoding); //get the bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream os = new GZIPOutputStream(baos);
			os.write(bytes, 0, bytes.length); //write the bytes
			os.close(); //close the stream
			byte[] result = baos.toByteArray(); //convert to byte array
			return result; //return the byte array
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//Method to uncompress a byte array into a string
	private static String unCompressString(byte[] data, String encoding) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data); //create an input stream
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //create an output stream
			GZIPInputStream is = new GZIPInputStream(bais); //create GZIP stream
			byte[] tmp = new byte[256]; //init a temp byte array
			while (true) {
				int r = is.read(tmp); //read from temp
				if (r < 0) {
					break;
				}
				buffer.write(tmp, 0, r); //write to the buffer
			}
			is.close(); //close the sream

			byte[] content = buffer.toByteArray();
			return new String(content, 0, content.length, encoding); //return the new string
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}		
	}
	
	//Wrapper method to encode data
	public static byte[] encode(String data){
		return compressToByte(data, "UTF-8");
	}
	//Wrapper method to decode data
	public static String decode(byte[] data) {
		return unCompressString(data, "UTF-8");
	}

	// Converts an image so it can be shown in a JLabel
	// Returns a new ImageIcon
	// Takes in the matrix image, for conversion
	// Code based on from the following example:
	// http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	public static ImageIcon getImageIconForJLabel(Mat image) {
		Imgproc.resize(image, image, new Size(640, 480));
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", image, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			return new ImageIcon(bufImage);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	//Method to calculate the compression ratio
	public static float calcCompressionRatio(float uncompressedSize, float compressedSize) {
		return uncompressedSize / compressedSize;
	}

	//Method to calculate the savings ratio
	public static float calcSpaceSavings(float uncompressedSize, float compressedSize) {
		return 1 - (calcCompressionRatio(compressedSize, uncompressedSize));
	}

}
