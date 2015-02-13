package com.allen.george.utils;

//Class that holds the compression cycle settings
public class CompressionCycleSettings {
	
	public static String IMAGE_TO_COMPRESS_PATH; //the image to compress
	public static String GRG_OUTPUT_PATH; //the grg output path
	public static String COMPRESSED_IMAGE_OUTPUT_PATH;	 //the new image output path
	public static CompressionAlgorithmType algorithmType; //the algorithm type
	
	//Method to print the settings
	public static void printSettings(){
		System.out.println("Compression Cycle Settings");
		System.out.println("-----------------------------------------------------------------");
		System.out.println("Image to compress: " + IMAGE_TO_COMPRESS_PATH);
		System.out.println("Path of GRG file: " + GRG_OUTPUT_PATH);
		System.out.println("Path of compressed output: " + COMPRESSED_IMAGE_OUTPUT_PATH);
		System.out.println("Algorithm Type: " + algorithmType.toString());
	}
}
