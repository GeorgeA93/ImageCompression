package com.allen.george.compression;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.allen.george.huffman.HuffmanEncoder;
import com.allen.george.huffman.Node;
import com.allen.george.utils.CompressionCycleSettings;
import com.allen.george.utils.FileUtil;
import com.allen.george.utils.Utils;

//Implememtation of the GRGC algorithm (COLOUR IMAGES)
//Extends the base class GRGCompressionAlgorithm
//and implements the compress method
public class GRGFullBlockDCTColourAlgo extends GRGCompressionAlgorithm {

	private Mat rChannel; // matrix to store the RED channel
	private Mat bChannel; // matirx to store the BLUE channel
	private Mat gChannel; // matirx to store the GREEN channel

	// Constructor
	// Takes in an input matrix (the original)
	public GRGFullBlockDCTColourAlgo(Mat original) {
		this.original = original;// set the original

		this.originalSize = original.size();// set the original size

		int colBorder = 8 - (original.cols() % 8); // find how much padding is
													// needed for the columns
		int rowBorder = 8 - (original.rows() % 8); // find how much padding is
													// needed for the rows
		Size sizeWithBorders = new Size(original.cols() + colBorder, original.rows() + rowBorder);// create
																									// a
																									// new
																									// size
																									// with
																									// the
																									// padding
																									// added
																									// to
																									// it

		final Mat borderdImage = Mat.zeros(sizeWithBorders, CvType.CV_32FC3);// create
																				// a
																				// matrix
																				// with
																				// the
																				// new
																				// padded
																				// size
																				// applied
		for (int y = 0; y < original.rows(); y++) {// loop through the rows of
													// the image
			for (int x = 0; x < original.cols(); x++) {// loop through the cols
														// of the image
				borderdImage.put(y, x, new double[] { original.get(y, x)[0], original.get(y, x)[1], original.get(y, x)[2] });// put
																																// the
																																// pixel
																																// data
																																// into
																																// the
																																// borderd
																																// image
			}
		}

		this.original = new Mat(borderdImage.size(), borderdImage.type());// create
																			// the
																			// original
																			// with
																			// the
																			// new
																			// bordered
																			// size

		for (int y = 0; y < original.rows(); y++) { // loop through the rows of
													// the image
			for (int x = 0; x < original.cols(); x++) { // loop throught the
														// cols of the image
				double[] data = new double[] { borderdImage.get(y, x)[0], borderdImage.get(y, x)[1], borderdImage.get(y, x)[2] };
				this.original.put(y, x, data);// put the bordered pixel data
												// into the original image
			}
		}

		// Init the three colour channel matrices
		rChannel = new Mat(this.original.size(), CvType.CV_8UC1);
		bChannel = new Mat(this.original.size(), CvType.CV_8UC1);
		gChannel = new Mat(this.original.size(), CvType.CV_8UC1);
		initChannels(this.original);
	}

	// Method to initiliase the three channel matrices
	// Takes in the input image m
	public void initChannels(Mat m) {
		for (int y = 0; y < m.rows(); y++) { // loop through the rows of the
												// matrix
			for (int x = 0; x < m.cols(); x++) { // loop through the cols of the
													// matrix
				double[] b = new double[] { m.get(y, x)[0] }; // get the BLUE
																// data
				double[] g = new double[] { m.get(y, x)[1] }; // get the GREEN
																// data
				double[] r = new double[] { m.get(y, x)[2] }; // get the RED
																// data
				bChannel.put(y, x, b); // put the BLUE data in the blue channel
				gChannel.put(y, x, g); // put the GREEN data in the green
										// channel
				rChannel.put(y, x, r); // put the RED data in the red channel
			}
		}

	}

	// Method to compress an input image
	// Runs the four stages of the algorithm:
	// Preprocess
	// Transform
	// Quantization
	// Encoding
	public void compress() {

		// preprocess all channels into blocks
		List<Mat> bBlocks = preprocess(bChannel);
		List<Mat> gBlocks = preprocess(gChannel);
		List<Mat> rBlocks = preprocess(rChannel);

		// transform all blocks
		bBlocks = transform(bBlocks);
		gBlocks = transform(gBlocks);
		rBlocks = transform(rBlocks);

		// quant all blocks
		bBlocks = quanitzation(bBlocks);
		gBlocks = quanitzation(gBlocks);
		rBlocks = quanitzation(rBlocks);

		// encode all blocks
		encode(bBlocks, gBlocks, rBlocks);
	}

	// Method to preprocess the input image
	// First splits it into blocks
	// Then takes 127 from each pixel value
	private List<Mat> preprocess(Mat matToProcess) {
		List<Mat> res = new ArrayList<Mat>();// init a list of matrices to store
												// the blocks

		for (int y = 0; y < matToProcess.rows(); y += 8) { // loop through the
															// original rows 8
															// points at a time
			for (int x = 0; x < matToProcess.cols(); x += 8) { // loop through
																// the original
																// cols 8 points
																// at a time
				Rect rect = new Rect(x, y, 8, 8);// create a new rect that has
													// an x position of x, y
													// position of y and a width
													// and height of 8
				res.add(new Mat(matToProcess, rect));// add the rect to a MAT
														// then to the list of
														// blocks
			}
		}

		for (Mat mat : res) { // loop through each matrix in the block list
			for (int y = 0; y < mat.rows(); y++) {// loop through the matrixs
													// rows
				for (int x = 0; x < mat.cols(); x++) {// loop through the
														// matrixs cols
					double[] values = mat.get(y, x);// get the pixel value at
													// x,y
					for (int i = 0; i < values.length; i++) {// for every value
																// at x,y
																// (should only
																// be one)
						//values[i] = values[i] - 127;// minus 127 from the value
					}
					mat.put(y, x, values);// put the values back at x, y
				}
			}
		}
		return res;// return the list of matrices
	}

	// Method to transform each block in the list of matrices
	// Takes in a List of Mat as parameters
	// Returns the new List of Mat which has had a DCT transform applied to it
	private List<Mat> transform(List<Mat> blocks) {
		List<Mat> res = new ArrayList<Mat>();// init a list of matrices to store
												// the blocks
		for (Mat mat : blocks) { // loop through each matrix in the block list
			mat.convertTo(mat, CvType.CV_32F);// conver the matrix so that DCT
												// can be applied
			Core.dct(mat, mat);// peform the DCT
			res.add(mat);// add the new matrix to the resultant list
		}
		return res; // return the list
	}

	// Method to quantize each block in the list of matrices
	// Takes in a List of Mat as parameters
	// Returns the new List of Mat which has had a quantization matrix applied
	// to it
	private List<Mat> quanitzation(List<Mat> blocks) {
		List<Mat> res = new ArrayList<Mat>();// init a list of matrices to store
												// the blocks

		Mat q = Utils.createQuantMat(); // Create a new mat to store the Quant
										// matrix

		for (Mat mat : blocks) { // loop through each matrix in the block list
			res.add(Utils.divide(mat, q)); // divide each matrix by the quant
											// matrix
		}

		return res; // return the new list
	}

	// Method to get the pixel data of a matrix as a string
	// Takes in a matrix
	// returns the matrix data as a string
	private String getData(Mat mat) {
		StringBuilder builder = new StringBuilder(); // create a new string
														// builder

		for (int y = 0; y < mat.rows(); y++) { // loop through the matrix rows
			for (int x = 0; x < mat.cols(); x++) { // loop through the matrix
													// cols
				builder.append(String.valueOf((int) (mat.get(y, x)[0])) + " "); // add
																				// the
																				// data
																				// to
																				// the
																				// string
																				// builder
			}
		}

		return builder.toString(); // return the string builders string
	}

	//Method to encode a list of matrices using huffman coding
	//Takes in the blocks
	//encodes each block
	//converts the encoded form to bytes
	//saves the bytes to a file
	private void encode(List<Mat> bBlocks, List<Mat> gBlocks, List<Mat> rBlocks) {
		HuffmanEncoder encoder = new HuffmanEncoder();//create a new Huffman Encoder	
		StringBuilder builder = new StringBuilder();//create a new string builder
		String data = "";//create a string to store the data
		int index = 0;//create an integer to use for indexing
		roots = new Node[bBlocks.size() + gBlocks.size() + rBlocks.size()]; //init the node array to store the huffman roots
		//encode the BLUE blocks
		for (Mat mat : bBlocks) {//loop through each matrix in the block list
			if (mat.channels() == 1) {//ensure that the matrix only has one channel		
				System.out.println("Encoding block: " + index + 1 + ". Out of: " + roots.length);//print out the current block we are encoding
				String imageData = getData(mat);//get the data of the matrix as a string		
				encoder.setInput(imageData);//set the encoders data
				encoder.encode();//encode the data
				roots[index] = encoder.getRoot();//set the root node of this block to the root array
				builder.append(encoder.getEncoded());//add the encoded data to the string builder
				builder.append(";");//add an endline character to end the block
				encoder.setEncoded(new StringBuilder()); //replace the string builder with a new one		
				index++;//increment the index
			}
		}
		//encode the GREEN blocks
		for (Mat mat : gBlocks) {//loop through each matrix in the block list
			if (mat.channels() == 1) {//ensure that the matrix only has one channel		
				System.out.println("Encoding block: " + index + 1 + ". Out of: " + roots.length);//print out the current block we are encoding
				String imageData = getData(mat);//get the data of the matrix as a string		
				encoder.setInput(imageData);//set the encoders data
				encoder.encode();//encode the data
				roots[index] = encoder.getRoot();//set the root node of this block to the root array
				builder.append(encoder.getEncoded());//add the encoded data to the string builder
				builder.append(";");//add an endline character to end the block
				encoder.setEncoded(new StringBuilder());//replace the string builder with a new one		
				index++;//increment the index
			}
		}
		//encode the RED blocks
		for (Mat mat : rBlocks) {//loop through each matrix in the block list
			if (mat.channels() == 1) {//ensure that the matrix only has one channel		
				System.out.println("Encoding block: " + index + 1 + ". Out of: " + roots.length);//print out the current block we are encoding
				String imageData = getData(mat);//get the data of the matrix as a string		
				encoder.setInput(imageData);//set the encoders data
				encoder.encode();//encode the data
				roots[index] = encoder.getRoot();//set the root node of this block to the root array
				builder.append(encoder.getEncoded());//add the encoded data to the string builder
				builder.append(";");//add an endline character to end the block
				encoder.setEncoded(new StringBuilder());//replace the string builder with a new one		
				index++;//increment the index
			}
		}

		byte[] bytes = Utils.encode(builder.toString());// convert the encoded string to bytes

		FileUtil.writeBytes(CompressionCycleSettings.GRG_OUTPUT_PATH + "/compressed.grg", bytes);//save the bytes to a file
	}

}
