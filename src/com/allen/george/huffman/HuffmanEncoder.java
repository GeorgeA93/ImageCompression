package com.allen.george.huffman;

import java.util.PriorityQueue;

//Class to encode a string of data using huffman encoding
public class HuffmanEncoder {

	private Node root = null; //the root node
	private final static int MAX_VALUE = 256; //the max value of a char 
	private String[] lookUpTable = new String[MAX_VALUE]; //the string array of look up values
	private StringBuilder encoded = new StringBuilder(); //the string builder
	private String input; //the input string
	
	//default constructor
	public HuffmanEncoder(){
		
	}

	//Constructor
	//Takes in an input string to be encoded
	public HuffmanEncoder(String input) {
		this.input = input;
	}

	//Method to encode a string and create a lookup table
	public void encode() {
		int[] frequencies = new int[MAX_VALUE]; //init an array of frequencies

		for (int i = 0; i < input.length(); i++) { //for the length of the input string
			frequencies[input.charAt(i)]++; //increment the frequenciers of the char in the string
		}

		PriorityQueue<Node> pq = new PriorityQueue<Node>(); //create a priorty queue to store the nodes

		for (int i = 0; i < frequencies.length; i++) { //for all of the frequncies
			if (frequencies[i] > 0) { //if the frequency occurs more than 0 times
				pq.add(new Node(frequencies[i], (char) i)); //add a new node to the queue
			}
		}

		while (pq.size() > 1) { //while the size of the queue is greater than 1
			Node left = pq.remove(); //remove a node from the queue
			Node right = pq.remove(); //remove another node from the queue
			pq.add(new Node(left, right)); //add a new node to the queue with the two nodes as parents
		}

		root = pq.poll(); //polls the queue

		createLookupTable(root, ""); //create the loop up table
		
		 
		for (char c : input.toCharArray()) { //for all the characters in the input string
			encoded.append(lookUpTable[c]); //get the encoded value of the character
		}
	}

	//Method to create a lookup table for a node and a string
	private void createLookupTable(Node current, String code) {
		if ((current.isLeaf())) { //if the node is a leaf node
			lookUpTable[current.getVal()] = code; //add the code to the lookup table
		} else { //else it is not a leaf
			createLookupTable(current.getLeft(), code + '0'); //add a 0 to the table for the left child
			createLookupTable(current.getRight(), code + '1'); //add a 1 to the table for the right child
		}
	}

	//GETTERS AND SETTERS
	public String getEncoded() {
		return encoded.toString();
	}
	
	public void setEncoded(StringBuilder encoded){
		this.encoded = encoded;
	}

	public Node getRoot() {
		return root;
	}

	public void setInput(String input){
		this.input = input;
	}
	

}
