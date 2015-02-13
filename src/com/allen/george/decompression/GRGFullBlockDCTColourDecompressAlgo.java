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

//Implememtation of the GRGC algorithm
//Extends the base class GRGDecompressionAlgorithm
//and implements the deCompress method
public class GRGFullBlockDCTColourDecompressAlgo extends GRGDecompressionAlgorithm{
	
	private String imageData;//string to store the image data
	private Mat image; //Mat to store the original image
	private Node[] roots; //Node array of root nodes
	private Size originalSize; //the size of the original
	
	//Constructor
	//Takes in the input image, the image data, the original size and an array of root nodes
	public GRGFullBlockDCTColourDecompressAlgo(String imageData, Mat image, Node[] roots, Size originalSize){
		this.imageData = imageData; //set the image data
		this.image = image; //set the image
		this.roots = roots; //set the roots
		this.originalSize = originalSize; //set the size
	}	
	
	//Method to decompress a string of image data into a new matrix
	public void deCompress(){
		String[] blockStrings = imageData.split(";"); //split the image data string into an array of strings
		int index = 0; //init a counter
		List<Mat> blocks = new ArrayList<Mat>(); //init a list of mat
		HuffmanDecoder decoder = new HuffmanDecoder();	//create the decoder
		for(String s : blockStrings){//loop through the strings
			decoder.setEncoded(s); //dset the data to decode
			decoder.setRoot(roots[index]); //set the root
			decoder.setDecoded(new StringBuilder());//set the string builder
			String decoded = decoder.decode();//decode the data
			blocks.add(createMat(decoded));		//create a matrix out of the data
			index ++; //increment the index
		}		
		
		List<Mat> bBlocks = new ArrayList<Mat>(); //create an array to store the BLUE channel blocks
		List<Mat> gBlocks = new ArrayList<Mat>(); //create an array to store the GREEN channel blocks
		List<Mat> rBlocks = new ArrayList<Mat>();	//create and array to store the RED channel blocks
		
		int size1 = blocks.size() / 3; //find the size of each block
		int size2 = size1 * 2; //G Blocks
		int size3 = size1 * 3; //R BLOCkS
		int i = 0;
		for(i = 0; i < size1; i ++){ //loop through the large block array
			bBlocks.add(blocks.get(i)); //add the block to the BLUE channel array
		}
		for( ; i < size2; i ++){ //loop through the large block array
			gBlocks.add(blocks.get(i));	//add the block to the GREEN channel array	
		}
		for( ; i < size3; i ++){ //loop through the large block array
			rBlocks.add(blocks.get(i));	//add the block to the RED channel array		
		}					
		
		//Dequantize all the blocks
		bBlocks = deQuantize(bBlocks);
		gBlocks = deQuantize(gBlocks);
		rBlocks = deQuantize(rBlocks);
		
		//Inverse the transfrom for all the blocks
		bBlocks = deTransform(bBlocks);	
		gBlocks = deTransform(gBlocks);	
		rBlocks = deTransform(rBlocks);	
		
		//De process all the blocks
		bBlocks = deProcess(bBlocks);
		gBlocks = deProcess(gBlocks);
		rBlocks = deProcess(rBlocks);
		
		//Create matrices to store each colour channel
		Mat rChannel = new Mat(image.size(), CvType.CV_32FC1);
		Mat bChannel = new Mat(image.size(), CvType.CV_32FC1);
		Mat gChannel = new Mat(image.size(), CvType.CV_32FC1);		
				
		bChannel = Utils.reconstruct(bBlocks, bChannel); //but the BLUE blocks into the BLUE channel
		rChannel = Utils.reconstruct(rBlocks, rChannel);//but the RED blocks into the RED channel
		gChannel = Utils.reconstruct(gBlocks, gChannel);	//but the GREEN blocks into the GREEN channel
		
		
		Mat finalMat = new Mat(originalSize, image.type()); //create the final decompressed matrix
		for(int y = 0; y < originalSize.height; y ++){ //loop through the rows of the image
			for(int x = 0; x < originalSize.width; x ++){ //loop though the cols of the image
				double[] data = new double[] { bChannel.get(y, x)[0], gChannel.get(y, x)[0], rChannel.get(y, x)[0] };// get the BGR data				
				finalMat.put(y, x, data); //put the BGR data into the final matrix
			}
		}
				
		Highgui.imwrite(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg", finalMat); //write the final matrix to the disk
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
						//values[i] = values[i] + 127; //minus 127
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
