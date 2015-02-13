package com.allen.george.compression;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import com.allen.george.huffman.Node;

//Base Class for ALL compression algorithms
//To create a new class extend this algorithm and implement the "Compress" method
public abstract class GRGCompressionAlgorithm {
	
	protected Mat original; //the input image
	protected Node[] roots; //the roots for huffman decoding
	public Size originalSize; //the size of the original image

	public abstract void compress(); //abstract compress method, to be extended
	
	//Get the huffman roots
	public Node[] getRoots(){
		return this.roots;
	}
	
	//Get the original image
	public Mat getOriginal(){
		return this.original;
	}
	
}
