package com.allen.george.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.allen.george.compression.ImageCompression;
import com.allen.george.decompression.ImageDecompression;
import com.allen.george.utils.CompressionAlgorithmType;
import com.allen.george.utils.CompressionCycleSettings;
import com.allen.george.utils.Utils;

//Class that implements the Graphical User interface for the program
//Allows the user to load images to be compressed and the running of the compresion cycle
public class GUI extends JFrame implements ActionListener, Runnable{
	
private static final long serialVersionUID = 1L;		
	
	//ACTION PERFORMED
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Exit")) { //EXIT BUTTON
			System.exit(0);			
		} else if (e.getActionCommand().equals("Help")) { // HELP BUTTON
			showHelp();
		} else if (e.getActionCommand().equals("Set Compression Cycle Settings")) { //SET COMPRESSION CYCLE SETTINGS
			compressionCycle();
		}else if (e.getActionCommand().equals("Run Compression Cycle")) {  //RUN THE CYCLE
			start();
		}else if (e.getActionCommand().equals("About")) { //ABOUT BUTTON
			showAbout();
		} else if (e.getActionCommand().equals("Clear All")) { //CLEAR ALL BUTTON
			clearAll();
		}
	}
	
	//start the compression cycle thread
	private void start(){
		if(CompressionCycleSettings.algorithmType != null){ //if we have selected an algorithm
			compressionThread = new Thread(this); //init the thread
			compressionThread.start(); //start the thread
		} else {
			String errorText = "Please set the cycle settings"; //THE TEXT
			JOptionPane.showMessageDialog(this, errorText, "Error", JOptionPane.ERROR_MESSAGE); //Showing the text
		}
	}
	

	//Compression cycle thread run method
	//Runs the compression and decompression cycle on the input image in a sperate thread
	public void run(){
		long start = System.nanoTime();  //get the start time of the thread
		
		imageCompressor = new ImageCompression(CompressionCycleSettings.IMAGE_TO_COMPRESS_PATH); //create the image compressor	
		imageCompressor.compress(); //compress the image
				
		imageDecompressor = new ImageDecompression(CompressionCycleSettings.GRG_OUTPUT_PATH + "/compressed.grg", imageCompressor.getCompressionAlgorithm(), imageCompressor.getCompressionAlgorithm().originalSize); //create the image decompressor
		imageDecompressor.decompress(); //decompress the image
		
		Mat original; //mat to hold the original
		Mat compressed; //mat to hold the compressed
		if(CompressionCycleSettings.algorithmType == CompressionAlgorithmType.GRG_COLOUR){	//If we used a colour image		
			original = Highgui.imread(CompressionCycleSettings.IMAGE_TO_COMPRESS_PATH, Highgui.CV_LOAD_IMAGE_COLOR); //read the original
			compressed = Highgui.imread(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg", Highgui.CV_LOAD_IMAGE_COLOR); //read the compressed 
		} else { //else we used a greyscale image
			original = Highgui.imread(CompressionCycleSettings.IMAGE_TO_COMPRESS_PATH, Highgui.CV_LOAD_IMAGE_GRAYSCALE);  //read the original
			compressed = Highgui.imread(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);//read the compressed 
		}		
		originalImageLabel.setIcon(Utils.getImageIconForJLabel(original)); //set the original label icon
		compressedImageLabel.setIcon(Utils.getImageIconForJLabel(compressed)); //set the compressed label icon
		originalImageLabel.setText(""); //remove the text
		compressedImageLabel.setText(""); //remove the text
		
		
		//calc compression and saving rates
		File oFile = new File(CompressionCycleSettings.IMAGE_TO_COMPRESS_PATH);
		File cFile = new File(CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH + "/result.jpg");
		File gFile = new File(CompressionCycleSettings.GRG_OUTPUT_PATH + "/compressed.grg");
		
		float oFileLength = oFile.length(); //get the file length of the original
		float cFileLength = cFile.length(); //get the file length of the compressed
		float gFileLength = gFile.length(); //get the file length of the GRG
		
		//ORIGINAL TO COMPRESS SAVINGS/COMPRESSION RATIO
		float ocCompressionRatio = Utils.calcCompressionRatio(oFileLength, cFileLength); 
		float ocSavingsRatio = Utils.calcSpaceSavings(oFileLength, cFileLength);
		
		//ORIGINAL TO GRG SAVINGS/COMPRESSION RATIO
		float ogCompressionRatio = Utils.calcCompressionRatio(oFileLength, gFileLength);
		float ogSavingsRatio = Utils.calcSpaceSavings(oFileLength, gFileLength);
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;		//calculate the time taken since the start of the thread
		
		//PRINT THE COMPRESSION INFO INTO A MESSAGE DIALOG
		String compressionInfo ="Compression Ratio between orignal and GRG file: " + ogCompressionRatio +"\n" +
								"Savings Ratio between orignal and GRG file: " + ogSavingsRatio * 100 + "%" +"\n" +"\n" +
								"Compression Ratio between original and new JPG file: " + ocCompressionRatio +"\n" +	
								"Savings Ratio between original and new JPG file: " + ocSavingsRatio * 100 + "%" + "\n" +"\n" +
								"Computational Time: " + elapsedTimeInSec;
		JOptionPane.showMessageDialog(this, compressionInfo, "Comparing Compression Rates and Saving Rates", JOptionPane.INFORMATION_MESSAGE); //Showing the text
		
		
		
	}
	
	//Method to show the compression cycle gui
	private void compressionCycle(){
		if(compressionCycleGui != null) return;//if the gui is not null return	
		
		compressionCycleGui = new CompressionCycleGUI(); //create the gui
		compressionCycleGui.setVisible(true); //set it visible
		compressionCycleGui.setResizable(false); //dont allow resizing
		
		compressionCycleGui = null; //make it null
	}
	
	//Method to clear all the settings and the labels
	private void clearAll(){
		if(imageCompressor == null) return;
		originalImageLabel.setIcon(null);		
		originalImageLabel.setText("Load an Image");
		compressedImageLabel.setIcon(null);		
		compressedImageLabel.setText("The result of the cycle");
		imageCompressor = null;
		imageDecompressor = null;
	}
	
		
		//Show the help text in a JOptionPane
		private void showHelp(){
			String helpText = "To compress an image please press 'Compression' then 'Compression Cycle'.\nTo Exit the program press 'File' then 'Exit'"; //THE HELP TEXT
			JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE); //Showing the text in an JOptionPane
		}
		
		//Show the about text in a JOptionPane
		private void showAbout(){
			String aboutText = "Image Compresser v1\n\nCreated by George Allen\n"; //THE ABOUT TEXT
			JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE); //Showing the text in an JOptionPane
		}
		
		//Construcor
		//Creates the GUI and adds all the elements
		public GUI(){
			setTitle("Image Compresser v1"); //set the title
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //set exit operation
			
			setBounds(100, 100, 1600, 900); //set the size
			
			//ADD THE MENUS AND CONTROLS
			setJMenuBar(menuBar);
			menuBar.add(fileMenu);			
			exitMenuItem.addActionListener(this);			
			fileMenu.add(exitMenuItem);
			menuBar.add(compressMenu);
			compressionCycleMenuItem.addActionListener(this);	
			runCompressionCycle.addActionListener(this);
			clearMenuItem.addActionListener(this);			
			compressMenu.add(compressionCycleMenuItem);		
			compressMenu.add(runCompressionCycle);		
			compressMenu.add(clearMenuItem);			
			menuBar.add(helpMenu);
			helpMenuItem.addActionListener(this);
			aboutMenuItem.addActionListener(this);
			helpMenu.add(helpMenuItem);
			helpMenu.add(aboutMenuItem);
			
			
			//SET UP THE PANELS AND LABELES	
			Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
			mainPanel.setBorder(padding);
			setContentPane(mainPanel);		
			originalImageLabel.setHorizontalAlignment(JLabel.CENTER);
			compressedImageLabel.setHorizontalAlignment(JLabel.CENTER);
			mainPanel.add(originalImageLabel);
			mainPanel.add(compressedImageLabel);	
		}
		
		//MENUS
		private JMenuBar menuBar = new JMenuBar();
		private JMenu fileMenu = new JMenu("File");
		private JMenu compressMenu = new JMenu("Compression");
		private JMenu helpMenu = new JMenu("Help");		
		private JMenuItem exitMenuItem = new JMenuItem("Exit");
		private JMenuItem compressionCycleMenuItem = new JMenuItem("Set Compression Cycle Settings");
		private JMenuItem runCompressionCycle = new JMenuItem("Run Compression Cycle");
		private JMenuItem clearMenuItem = new JMenuItem("Clear All");
		private JMenuItem helpMenuItem = new JMenuItem("Help");
		private JMenuItem aboutMenuItem = new JMenuItem("About");
		
		//CONTAINERS
		private JPanel mainPanel = new JPanel(new GridLayout(1, 1));
		
		//BUTTONS AND LABELS
		private JLabel originalImageLabel = new JLabel("Load an Image");
		private JLabel compressedImageLabel = new JLabel("The result of the cycle");	
		
		//IMAGE COMPRESSION CYCLEGUI
		private CompressionCycleGUI compressionCycleGui;
		
		//IMAGE COMPRESSION CYCLE OBJECTS AND THREADS
		private ImageCompression imageCompressor;
		private ImageDecompression imageDecompressor;
		private Thread compressionThread;

}
