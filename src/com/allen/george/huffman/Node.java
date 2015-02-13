package com.allen.george.huffman;

//Class for a huffman node
//Each node can have a left and right child, a frequency and a value
public class Node implements Comparable<Node>{

	private Node left; //the left child
	private Node right; //the right child
	private int frequency; //the frequency
	private char val;//the value
	
	//Constructor
	//Creates a node from a value and a frequency
	public Node(int frequency, char val){
		this.frequency = frequency; //set the frequency
		this.val = val; //set the value
	}
	
	//Construcor
	//Creates a node from a left and right child
	public Node(Node left, Node right){
		this.left = left; //create the left child
		this.right = right; //create the right child
	}
	
	//Method to check wheter the node is a leaf or not
	public boolean isLeaf(){
		if (left == null && right == null){ //if the node has no children
			return true; //it is a leaf
		}
		return false; //else it is not a leaf
	}
	
	//Method to compare the frequencies of two nodes
	public int compareTo(Node other){
		return (this.frequency - other.getFrequency()); //compare the two frequencies		
	}
	
	//GETTERS
	public Node getLeft(){
		return left;
	}
	
	public Node getRight(){
		return right;
	}
	
	public char getVal(){
		return val;
	}
	
	public int getFrequency(){
		return frequency;
	}
	
}
