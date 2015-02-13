package com.allen.george.decompression;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import com.allen.george.compression.GRGCompressionAlgorithm;
import com.allen.george.huffman.Node;
import com.allen.george.utils.CompressionAlgorithmType;
import com.allen.george.utils.CompressionCycleSettings;
import com.allen.george.utils.FileUtil;
import com.allen.george.utils.Utils;


//Class used to run a decompression algorithm
//The decompress method RUNS the selected algorithm
public class ImageDecompression {
	
	private byte[] bytes; //the bytes of the image
	private String imageData;//the encoded data
	private Mat image; //the original image
	private Node[] roots; //the node array of roots
	private Size originalSize; //the size of the original image
	
	private GRGDecompressionAlgorithm decompressionAlgorithm; //the decompression algorithm
	
	//Constructor
	//Takes in the file path to the encoded image, the compression algorithm and the size of the original image
	public ImageDecompression(String filePath, GRGCompressionAlgorithm compressionAlgorithm, Size originalSize){
		this.image = compressionAlgorithm.getOriginal(); //set the original image		
		bytes = FileUtil.readBytes(filePath); //read the bytes
		imageData = Utils.decode(bytes); //decode the bytes
		this.roots = compressionAlgorithm.getRoots(); //set the roots
		this.originalSize = originalSize; //set the original size
	}
	
	public void decompress(){			
		if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_FULL_BLOCKS_DCT){	 //if we are using GRGF	
			decompressionAlgorithm = new GRGFullBlockDecompressAlgo(imageData, image, roots, originalSize); //init the GRGF algorithm
			decompressionAlgorithm.deCompress();//decompress the data
		} else if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_HALF_BLOCKS_DCT){//if we are using GRGH	
			decompressionAlgorithm = new GRGHalfCBlockDecompressAlgo(imageData, image, roots,originalSize );//init the GRGH algorithm
			decompressionAlgorithm.deCompress();//decompress the data
		} else if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_NO_BLOCKS_DWT){//if we are using GRGN
			decompressionAlgorithm = new GRGNoBlockDecompressAlgo(imageData, image, roots, originalSize);//init the GRGN algorithm
			decompressionAlgorithm.deCompress();//decompress the data
		}else if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_COLOUR){//if we are using GRGC
			decompressionAlgorithm = new GRGFullBlockDCTColourDecompressAlgo(imageData, image, roots, originalSize);//init the GRGC algorithm
			decompressionAlgorithm.deCompress();//decompress the data
		}			
	}

	
	
}
