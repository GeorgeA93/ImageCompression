package com.allen.george.huffman;

//Class to decode a data into a string using huffman decoding
public class HuffmanDecoder {
	
	private String encoded; //the encoded data as a string
	private Node root; //the root node
	private StringBuilder decoded = new StringBuilder(); //string builder
	
	//default constructor
	public HuffmanDecoder(){
		
	}
	
	//constructor
	//Takes in the encoded string and the root node (key)
	public HuffmanDecoder(String encoded, Node root){
		this.encoded = encoded; //set the encoded data
		this.root = root; //set the root node
	}
	
	//Method to decode the encoded string using the root node
	//Returns a string which is decoded
	public String decode(){
		char[] encodedArray = encoded.toCharArray(); //split the encoded data into a char array
		Node n = root; //get the root node
		
		for(char c : encodedArray){ //loop through each character in the char array
			
			if (c == '0'){ //if the char is a zero
				n = n.getLeft(); //get the left child node of the root
				if (n.isLeaf()) { //if the node is a leaf
					decoded.append(n.getVal()); //add the value
					n = root; //set the root
				} 
				
			}else if(c == '1'){ //if the char is a 1
				n = n.getRight(); //get the right child node of the root
				if (n.isLeaf()) { //if the right child is a leaf
					decoded.append(n.getVal()); //add the value
					n = root;//set the root
				}
			}
		}
		
		return decoded.toString(); //return the decode string
	}
	
	//SETTERS
	public void setEncoded(String encoded){
		this.encoded = encoded;
	}
	
	public void setRoot(Node root){
		this.root = root;
	}
	
	public void setDecoded(StringBuilder decoded){
		this.decoded = decoded;
	}

}
