package com.allen.george.decompression;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

import com.allen.george.huffman.HuffmanDecoder;
import com.allen.george.huffman.Node;
import com.allen.george.utils.CompressionCycleSettings;
import com.allen.george.utils.Utils;


//Implememtation of the GRGN algorithm
//Extends the base class GRGDecompressionAlgorithm
//and implements the deCompress method
public class GRGNoBlockDecompressAlgo extends GRGDecompressionAlgorithm {

	private String imageData;//the image data to be decoded
	private Mat image; // the original image
	private Node[] roots; //the node array of root nodes
	private Size originalSize; //the size of the original
	
	//Constructor
	//Takes in the image data, the original image, the array of root nodes and the size of the original
	public GRGNoBlockDecompressAlgo(String imageData, Mat image, Node[] roots, Size originalSize){
		this.imageData = imageData; //set the image data
		this.image = image; //set the image
		this.roots = roots; //set the roots
		this.originalSize = originalSize; //set the size
	}	

	//Method to decompress the encoded data into a new matrix
	public void deCompress() {
		HuffmanDecoder decoder = new HuffmanDecoder(); //create the huffmand decoder
		decoder.setEncoded(imageData); //set the data to be decoded
		decoder.setRoot(roots[0]); //set the root
		String decoded = decoder.decode(); //decode the data
		Mat m = createMat(decoded); //create a matrix from the decoded data

		//de transform the matrix
		m = deTransform(m); 
		//de process the matrix
		m = deProcess(m);
		
		this.image = m.clone(); //copy the matrix to the image
 
		Mat finalM = new Mat(originalSize, image.type()); //create a final matrix
		
		for(int y = 0; y < finalM.rows(); y ++){ //loop through the final matrix rows
			for(int x  = 0; x < finalM.cols(); x ++){ //loop through the final matrix cols
				double[] data = new double[] { image.get(y, x)[0] }; //get the data
				
				finalM.put(y, x, data); //put the data into the final matrix
			}
		}
				
		Highgui.imwrite(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg", finalM); //write the final image to the disk
	}

	//Method to create a matrix from a string
	//Takes in a string d
	//returns the matrix representation of the string
	private Mat createMat(String d){
		Mat m = new Mat(image.size(), CvType.CV_32F); //init the resulant matirx
		
		String[] dd = d.split(" ");	 //split the string at every space into a string array
		double[][] id = new double[image.rows()][image.cols()];	 //create a double array to store the integer value of the string
		int count = 0; //init a counter
		for(int i = 0; i < image.rows(); i ++){ //loop trhough the image rows
			for(int j = 0; j < image.cols(); j ++){ //loop through the image cols
				if(count == dd.length) break; //check we havent gone out of bounds						
				id[i][j] = Double.valueOf(dd[count]); //add the interget value to the id array
				count ++; //increment the counter
			}
		}
		
		for(int y = 0; y < image.rows(); y ++){ //loop through the image rows
			for(int x = 0; x < image.cols(); x ++){ //loop through the image cols
				double[] values = new double[] { id[y][x] }; //get the data
				m.put(y, x, values); //put the data into the matrix
			}
		}
		
		return m; //return the matrix
	}

	//Method to deProcess a matrix
	//Takes in the matrix
	//Returns the new matrix, res
	public Mat deProcess(Mat m) {
		Mat res = new Mat(m.size(), m.type()); //init the resultant matrix
		for (int y = 0; y < m.rows(); y++) { //loop through the rows of the matrix
			for (int x = 0; x < m.cols(); x++) { //loop through the cols of the matrix
				double[] values = m.get(y, x); //get the data
				for (int i = 0; i < values.length; i++) { //loop through the data
					values[i] = values[i] + 127; //Add 127 to each value
				}
				res.put(y, x, values); //put the data into the resultant matrix
			}
		}
		return res; //return the new matrix
	}

	//Method to deTransfrom a matrix
	//Takes in a matrix, m
	//Returns a new matrix, res
	public Mat deTransform(Mat m) {
		Mat res = new Mat(m.size(), m.type()); //init the resultant matrix
		InverseHaarWaveletTransform(m, res, 1, 2, 40); //perform the inverse DWT 
		return res; //return the result
	}
	
	//Method to perform a INVERSE DWT on a matrix
	//Takes in a matrix source, a matrix desitnation and the number of iterations
	//Based on code from: http://stackoverflow.com/questions/20071854/wavelet-transform-in-opencv
	private void InverseHaarWaveletTransform(Mat src, Mat dst, int iterations, int shrinkageType, float shrinkageVal){
		float c, dh, dv, dd;
		int width = src.cols();
		int height = src.rows();
		
		for(int k = iterations; k > 0; k --){
			for(int y = 0; y < (height >> k); y ++){
				for(int x = 0; x < (width >> k); x ++){
					c = (float)src.get(y, x)[0];
					dh = (float)src.get(y, x + (width >> k))[0];
					dv = (float)src.get(y + (height >> k), x)[0];
					dd = (float)src.get(y + (height >> k), x + (width >> k))[0];
					
					switch (shrinkageType){
						case 0: //HARD
							dh = hardShrink(dh, shrinkageVal);
							dv = hardShrink(dv, shrinkageVal);
							dd = hardShrink(dd, shrinkageVal);							
							break;						
						case 1: //SOFT
							dh = softShrink(dh, shrinkageVal);
							dv = softShrink(dv, shrinkageVal);
							dd = softShrink(dd, shrinkageVal);	
							break;							
						case 2: //GARROT
							dh = garrotShrink(dh, shrinkageVal);
							dv = garrotShrink(dv, shrinkageVal);
							dd = garrotShrink(dd, shrinkageVal);	
							break;
					}
					
					dst.put(y * 2,  x * 2, new double[] { 0.5 * (c + dh + dv + dd) });
					dst.put(y * 2, x * 2 + 1, new double[] { 0.5 * ( c- dh + dv - dd) });
					dst.put(y * 2 + 1, x * 2, new double[] { 0.5 * (c + dh - dv - dd) });
					dst.put(y * 2 + 1, x * 2 + 1, new double[] { 0.5 * (c - dh - dv + dd) });
				}
			}
			Mat C = src.adjustROI(0, 0, width >> (k - 1), height >> (k -1));
			Mat D = dst.adjustROI(0, 0, width >> (k - 1), height >> (k -1));
			D.copyTo(C);
		}
	}
	
	private float signum(float x){
		float res = 0;
		if(x == 0){
			res = 0;
		} 
		if(x > 0){
			res = 1;
		}
		if(x < 0){
			res = -1;
		}
		return res;
	}
	
	private float softShrink(float d, float t){
		float res;
		if(Math.abs(d) > t){
			res = signum(d) * (Math.abs(d) - t);
		} else {
			res = 0;
		}		
		return res;
	}
	
	private float hardShrink(float d, float t){
		float res;		
		if(Math.abs(d) > t){
			res = d;
		} else {
			res = 0;
		}		
		return res;
	}
	
	private float garrotShrink(float d, float t){
		float res;		
		if(Math.abs(d) > t){
			res = d - ((t * t) / d);
		} else{
			res = 0;
		}		
		return res;
	}

}
