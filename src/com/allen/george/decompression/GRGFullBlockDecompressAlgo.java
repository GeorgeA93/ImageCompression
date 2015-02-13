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

//Implememtation of the GRGF algorithm
//Extends the base class GRGDecompressionAlgorithm
//and implements the deCompress method
public class GRGFullBlockDecompressAlgo extends GRGDecompressionAlgorithm{
	
	private String imageData;//the image data to be decoded
	private Mat image; // the original image
	private Node[] roots; //the node array of root nodes
	private Size originalSize; //the size of the original
	
	//Constructor
	//Takes in the image data, the original image, the array of root nodes and the size of the original
	public GRGFullBlockDecompressAlgo(String imageData, Mat image, Node[] roots, Size originalSize){
		this.imageData = imageData; //set the image data
		this.image = image; //set the image
		this.roots = roots; //set the roots
		this.originalSize = originalSize; //set the size
	}	
	
	//Method to decompress the encoded data into a new matrix
	public void deCompress(){
		String[] blockStrings = imageData.split(";"); //split the image data into a string array
		int index = 0; //init a counter
		List<Mat> blocks = new ArrayList<Mat>(); //init the blocks
		HuffmanDecoder decoder = new HuffmanDecoder();//create the decoder
		for(String s : blockStrings){ //loop through the strings in the string array
			decoder.setEncoded(s); //set the encoded data
			decoder.setRoot(roots[index]); //set the root
			decoder.setDecoded(new StringBuilder()); //set the string builder
			String decoded = decoder.decode();//decode the string
			blocks.add(createMat(decoded)); //create a matrix from the string
			index ++; //increment the counter
		}		
		
		//dequantize the blocks
		blocks = deQuantize(blocks);
		//detransform the blocks
		blocks = deTransform(blocks);	
		//de process the blocks
		blocks = deProcess(blocks);	
		
		this.image = Utils.reconstruct(blocks, image); //create a matrix out of all the blocks
		
		Mat finalM = new Mat(originalSize, image.type()); //init the final image
		
		for(int y = 0; y < finalM.rows(); y ++){ //loop throught the rows of the matrix
			for(int x  = 0; x < finalM.cols(); x ++){ //loop through the cols of the matrix
				double[] data = new double[] { image.get(y, x)[0] }; //get the image data
				
				finalM.put(y, x, data); //put the data into the final image
			}
		}
				
		Highgui.imwrite(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg", finalM); //write the image to memory
	}
	
	//Method to create a matrix from a string
	//Takes in a string d
	//returns the matrix representation of the string
	private Mat createMat(String d){
		Mat m = new Mat(new Size(8, 8), CvType.CV_32F); //init the resultant matrix
		
		String[] dd = d.split(" "); //split the string at every space into a string array
		double[][] id = new double[8][8]; //create a double array to store the Integer value of the string
		int count = 0; //init a counter
		for(int i = 0; i < 8; i ++){ //from 0 to 8
			for(int j = 0; j < 8; j ++){ //from 0 to 8
				if(count == dd.length) break; //check we havent gone out of bounds			
				id[i][j] = Double.valueOf(dd[count]); //add the Integer value to the id array
				count ++; //increment the counter
			}
		}
		
		for(int y = 0; y < 8; y ++){ ///from 0 to 8
			for(int x = 0; x < 8; x ++){//from 0 to 8
				double[] values = new double[] { id[y][x] }; //get the values				
				m.put(y, x, values); //put the values into the resultant matrix
			}
		}
		
		return m; //return the resultant matrix
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
