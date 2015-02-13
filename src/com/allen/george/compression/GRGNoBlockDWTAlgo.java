package com.allen.george.compression;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import com.allen.george.huffman.HuffmanEncoder;
import com.allen.george.huffman.Node;
import com.allen.george.utils.CompressionCycleSettings;
import com.allen.george.utils.FileUtil;
import com.allen.george.utils.Utils;

//Implememtation of the GRGN algorithm
//Extends the base class GRGCompressionAlgorithm
//and implements the compress method
public class GRGNoBlockDWTAlgo extends GRGCompressionAlgorithm{

	//Constructor
	//Takes in an input matrix (the original)
	public GRGNoBlockDWTAlgo(Mat original){
		this.original = original; //set the original
		
		this.originalSize = original.size();// set the original size
		
		int colBorder = 8 - (original.cols() % 8); //find how much padding is needed for the columns
		int rowBorder = 8 - (original.rows() % 8); //find how much padding is needed for the rows
		Size sizeWithBorders = new Size(original.cols() + colBorder, original.rows() + rowBorder); //create a new size with the padding added to it
		
		
		final Mat borderdImage = Mat.zeros(sizeWithBorders, CvType.CV_32FC1);//create a matrix with the new padded size applied
		for (int y = 0; y < original.rows(); y++){ //loop through the rows of the image
			for (int x = 0; x < original.cols(); x++){ //loop through the cols of the image
				borderdImage.put(y, x, new double[] { original.get(y, x)[0] });  //put the pixel data into the borderd image
			}
		}

		this.original = new Mat(borderdImage.size(), borderdImage.type()); //create the original with the new bordered size
		
		for (int y = 0; y < original.rows(); y ++) { //loop through the rows of the image
			for (int x = 0; x < original.cols(); x ++) { //loop throught the cols of the image
				double[] data = new double[] { borderdImage.get(y, x)[0]};	 
				this.original.put(y, x, data); //put the bordered pixel data into the original image
			}
		}	
	}                                                                                                                                                            
	
	//Method to compress an input image
	//Runs the three stages of the algorithm:
	//Preprocess
	//Transform	
	//Encoding
	public void compress(){		
		Mat mat = preprocess(original); //preprocess the original into a new matrix
		mat = transform(mat);	//transform the new matrix
		encode(mat); //encode the new matrix
	}
	
	
	//Method to preprocess the input image	
	// takes 127 from each pixel value
	private Mat preprocess(Mat m){
		Mat res = new Mat(m.size(), m.type()); //create a new matrix to store the result
		for (int y = 0; y < m.rows(); y++) { //loop through the rows of the matrix
			for (int x = 0; x < m.cols(); x++) { //loop through the cols of the matrix
				double[] values = m.get(y, x); //create a double array of the matrices values
				for (int i = 0; i < values.length; i++) { //loop through each value
					values[i] = values[i] - 127; //minus 127 from each value
				}
				res.put(y, x, values); //put the values in the result matrix
			}
		}
		
		return res; // return the result
	}
	
	//Method to transform a matrix using DWT
	//Takes in a the matrix
	//Returns the matrix with DWT applied to it
	private Mat transform(Mat m){
		Mat res = new Mat(m.size(), m.type()); //create a new matrix to store the result
		HaarWaveletTransform(m, res, 1); //apply the DWT to the matrix and store the result
		return res;	 //return the result
	}
	
	
	//Method to get the pixel data of a matrix as a string
	//Takes in a matrix
	//returns the matrix data as a string
	private String getData(Mat mat){
		StringBuilder builder = new StringBuilder(); //create a new string builder
		
		for(int y = 0; y < mat.rows(); y ++){ //loop through the matrix rows
			for(int x = 0; x < mat.cols(); x ++){ //loop through the matrix cols
				builder.append(String.valueOf((int)(mat.get(y, x)[0])) + " "); //add the data to the string builder
			}			
		}
			
		return builder.toString(); //return the string builders string
	}
	
	//Method to encode a a matrix using huffman coding
	//Takes in the matrix
	//encodes the matrix
	//converts the encoded form to bytes
	//saves the bytes to a file
	private void encode(Mat m){
		HuffmanEncoder encoder = new HuffmanEncoder();		//create a new Huffman Encoder	
		StringBuilder builder = new StringBuilder();//create a new string builder
		roots = new Node[1];//create a new string builder
		if(m.channels() == 1){//ensure that the matrix only has one channel	
			System.out.println("Encoding block: 1");//print out the current block we are encoding
			String imageData = getData(m);//get the data of the matrix as a string		
			encoder.setInput(imageData);//set the encoders data
			encoder.encode();			//encode the data
			roots[0] = encoder.getRoot();//set the root node of this block to the root array
			builder.append(encoder.getEncoded());//add the encoded data to the string builder
			builder.append(";");	//add an endline character to end the block
		} else {
			System.out.println("Colour not yet implemented");//if there are more than one channels we cannot encode
		}
		
		
		byte[] bytes = Utils.encode(builder.toString());// convert the encoded string to bytes
		
		FileUtil.writeBytes(CompressionCycleSettings.GRG_OUTPUT_PATH + "/compressed.grg", bytes);//save the bytes to a file
	}
	
	//Method to perform a DWT on a matrix
	//Takes in a matrix source, a matric desitnation and the number of iterations
	//Based on code from: http://stackoverflow.com/questions/20071854/wavelet-transform-in-opencv
	private void HaarWaveletTransform(Mat src, Mat dst, int iterations){
		float c, dh, dv, dd;
		int width = src.cols();
		int height = src.rows();
		
		for(int k = 0; k < iterations; k ++){
			for(int y = 0; y < (height >> (k + 1)); y ++){
				for(int x = 0; x < (width >> (k + 1)); x ++){
					c = (float)((src.get(2 * y, 2 * x)[0] +  src.get(2 * y,  2 * x + 1)[0] + src.get(2 * y + 1,  2 * x)[0] + src.get(2 * y + 1,  2 * x + 1)[0]) * 0.5);
					dst.put(y, x, new double[] { c });
					
					dh = (float)((src.get(2 * y, 2 * x)[0] +  src.get(2 * y + 1,  2 * x)[0] - src.get(2 * y,  2 * x + 1)[0] - src.get(2 * y + 1,  2 * x + 1)[0]) * 0.5);
					dst.put(y, x + (width >> (k + 1)), new double[] { dh });
					
					dv = (float)((src.get(2 * y, 2 * x)[0] +  src.get(2 * y ,  2 * x + 1)[0] - src.get(2 * y + 1,  2 * x )[0] - src.get(2 * y + 1,  2 * x + 1)[0]) * 0.5);
					dst.put(y + (height >> (k + 1)), x , new double[] { dv });
					
					dd = (float)((src.get(2 * y, 2 * x)[0] -  src.get(2 * y ,  2 * x + 1)[0] - src.get(2 * y + 1,  2 * x )[0] + src.get(2 * y + 1,  2 * x + 1)[0]) * 0.5);
					dst.put(y + (height >> (k + 1)), x + (width >> (k + 1)) , new double[] { dd });
				}
			}
			dst.copyTo(src);
		}
	}
	
	
	
}
