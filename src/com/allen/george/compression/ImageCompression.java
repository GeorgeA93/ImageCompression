package com.allen.george.compression;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.allen.george.utils.CompressionAlgorithmType;
import com.allen.george.utils.CompressionCycleSettings;

//Class used to run a compression algorithm
//The compress method RUNS the selected algorithm
public class ImageCompression {

	private Mat original; //the input image
	
	private GRGCompressionAlgorithm compressionAlgorithm; //the compression algorithm to use
	
	//Constructor
	//Takes in the file path to the original image
	public ImageCompression(String filePath) {
		
		if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_COLOUR){	//if we are using a colour image algorithm		
			original = Highgui.imread(filePath, Highgui.CV_LOAD_IMAGE_COLOR); //load the input image into a 3 channel matrix
		} else { //if we are using a greyscale image algorithm
			original = Highgui.imread(filePath, Highgui.CV_LOAD_IMAGE_GRAYSCALE); //load the input image into a 1 channel matrix
		}		
	}


	//Method compress
	//Compresses the input image depending on the chosen algorithm type
	public void compress() {
		
		if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_FULL_BLOCKS_DCT){	//if we are using GRGF	
			compressionAlgorithm = new GRGFullBlockDCTAlgo(original); //init the GRGF algorithm
			compressionAlgorithm.compress(); //compress the image
		} else if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_HALF_BLOCKS_DCT){ //if we are using GRGH
			compressionAlgorithm = new GRGHalfBlockDCTAlgo(original); //init the GRGH algorithm
			compressionAlgorithm.compress();//compress the image
		} else if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_NO_BLOCKS_DWT){ //if we are using GRGN
			compressionAlgorithm = new GRGNoBlockDWTAlgo(original); //init the GRGN algorithm
			compressionAlgorithm.compress(); //compress the image
		} else if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_COLOUR){ //if we are using GRGC
			compressionAlgorithm = new GRGFullBlockDCTColourAlgo(original); //init the GRGC algorithm
			compressionAlgorithm.compress(); //compress the image
		}		
		
	}	

	//Method to get the compression algorithm that was used to compress the image
	public GRGCompressionAlgorithm getCompressionAlgorithm(){
		return this.compressionAlgorithm; //returns the compression algorithm
	}
	

}
