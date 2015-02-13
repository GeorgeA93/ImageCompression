package com.allen.george.decompression;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

import com.allen.george.huffman.HuffmanDecoder;
import com.allen.george.huffman.Node;
import com.allen.george.utils.CompressionCycleSettings;
import com.allen.george.utils.Utils;

//Implememtation of the GRGH algorithm
//Extends the base class GRGDecompressionAlgorithm
//and implements the deCompress method
public class GRGHalfCBlockDecompressAlgo extends GRGDecompressionAlgorithm {
	
	private String imageData;//the image data to be decoded
	private Mat image; // the original image
	private Node[] roots; //the node array of root nodes
	private Size originalSize; //the size of the original
	
	//Constructor
	//Takes in the image data, the original image, the array of root nodes and the size of the original
	public GRGHalfCBlockDecompressAlgo(String imageData, Mat image, Node[] roots, Size originalSize){
		this.imageData = imageData; //set the image data
		this.image = image; //set the image
		this.roots = roots; // set the roots
		this.originalSize = originalSize; //set the image size
	}	
	
	//Method to decompress the encoded data into a new matrix
	public void deCompress(){
		HuffmanDecoder decoder = new HuffmanDecoder();	//create the huffman decoder	
		decoder.setEncoded(imageData); //set the encoded data
		decoder.setRoot(roots[0]); //set the root
		String decoded = decoder.decode();//decode the data			
		Mat m = createMat(decoded);	 //create a mat from the decoded string
		List<Mat> blocks = createBlocks(m); //create blocks from the mat
		
		//dequantize all the blocks
		blocks = deQuantize(blocks);
		//de transform all the blocks
		blocks = deTransform(blocks);
		//de process all the blocks
		blocks = deProcess(blocks);
		
		this.image = Utils.reconstruct(blocks, image); //reconstruct the blocks into one matrix
		
		Mat finalM = new Mat(originalSize, image.type()); //create a final matrix
		
		for(int y = 0; y < finalM.rows(); y ++){ //loop through the rows of the matrix
			for(int x  = 0; x < finalM.cols(); x ++){ //loop throught the cols of the matrix
				double[] data = new double[] { image.get(y, x)[0] }; //get the data
				
				finalM.put(y, x, data); //put the data into the final matrix
			}
		}
				
		Highgui.imwrite(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg", finalM); //save the image to the disk	
		
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
	
	//Method to create split a matrix into a list of matrices
	//Takes in a matrix, m
	//returns a new list of matrices, res
	private List<Mat> createBlocks(Mat m){
		List<Mat> res = new ArrayList<Mat>(); //init the new list
		for (int y = 0; y < m.rows(); y += 8) { //loop through the matrix rows 8 points at a time
			for (int x = 0; x < m.cols(); x += 8) { //loop through the matrix cols 8 points at a time
				Rect rect = new Rect(x, y, 8, 8); //create a new rectangle
				res.add(new Mat(m, rect)); //put the rectangle into a matrix and then into the list
			}
		}		
		return res; //return the list
	}
	
	
	//Method to deProcess a list of matrices
		//Takes in a list of matrices, blocks
		//Returns the new list of matrices, res
		public List<Mat> deProcess(List<Mat> blocks){
			List<Mat> res = new ArrayList<Mat>(); //the new list of matrices
			
			for(Mat mat : blocks){ //for each matrix in the list
				for (int y = 0; y < mat.rows(); y++) { //loop through the rows of the matrix
					for (int x = 0; x < mat.cols(); x++) { //loop through the cols of the matrix
						double[] values = mat.get(y, x); //get the pixel values
						for (int i = 0; i < values.length; i++) { //for each value
							values[i] = values[i] + 127; //minus 127
						}
						mat.put(y, x, values); //put the new values into the matrix
					}
				}
				res.add(mat); //add the matrix to the list
			}
			
			return res; //return the new list of matrices
		}
		
		//Method to deTransfrom a list of matrices
		//Takes in a list of matrices, blocks
		//Returns a new list of matrices, res
		public List<Mat> deTransform(List<Mat> blocks){
			List<Mat> res = new ArrayList<Mat>(); //the new list of matrices
			for (Mat mat : blocks) { //for each matrix in the list
				mat.convertTo(mat, CvType.CV_32F); //convert the matrix so it can use the DCT
				Core.idct(mat, mat); //apply the inverse DCT to the matrix
				res.add(mat); //add the matrix to the new list
			}
			return res; //return the new list of matrices
		}
		
		//Method to deQuantize a list of matrices
		//Takes in a list of matrices, blocks
		//Returns a new list of matrices, res
		public List<Mat> deQuantize(List<Mat> blocks){		
			List<Mat> res = new ArrayList<Mat>(); //the new list of matrices
			Mat q = Utils.createQuantMat();		//the Quant matrix
			for (Mat mat : blocks) { //for each matrix in the list
				res.add(Utils.multiply(mat, q)); //times the matrix by the qaunt matrix and add it to the list
			}
			return res;	 //return the new list
		}
}
