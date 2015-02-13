package com.allen.george.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.allen.george.utils.CompressionAlgorithmType;
import com.allen.george.utils.CompressionCycleSettings;

//Class to house the graphical user interface to set the compression cycle settings
//Allows the user to pick images to compress/decompress, where to save the results and what algorithm to use
public class CompressionCycleGUI extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	private String imageToCompressTemp; //temporary String for image to compres path
	private String grgOutputTemp;//temporary String for grg output path
	private String compressedOutputTemp;//temporary String for result output path
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Save")) { //EXIT BUTTON
			setSettings();
		} else if (e.getActionCommand().equals("Browse Image To Compress")) { // HELP BUTTON
			getChosenPath("Select the image to compress", 0);
		} else if (e.getActionCommand().equals("Browse GRG output")) { //SPATIAL DOMAIN BUTTON
			getChosenPath("Select the output directory for the GRG file", 1);
		}else if (e.getActionCommand().equals("Browse Compress Output")) { //ABOUT BUTTON
			getChosenPath("Select the output directory for the compressed file", 2);
		}
		
	}
	
	private void getChosenPath(String title, int sender){
		JFileChooser chooser = new JFileChooser(); //the JFileChooser
		chooser.setCurrentDirectory(new java.io.File(".")); //Set to look in the project root directory
		chooser.setDialogTitle(title);		//Set title of the File Chooser
		chooser.setAcceptAllFileFilterUsed(false); //dont accept all file filters
		if(sender != 0){
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //Switch to directories only
		}
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {		//When the user clicks the load/open button	
			if(sender == 0){
				imageToCompressTemp = chooser.getSelectedFile().getAbsolutePath(); //get the path
			} else if(sender == 1){
				grgOutputTemp = chooser.getCurrentDirectory().getAbsolutePath(); //get the path
			} else if(sender == 2){
				compressedOutputTemp = chooser.getCurrentDirectory().getAbsolutePath(); //get the path
			}
		} 
	}
	
	//Method to set the compression cycle settings
	private void setSettings(){
		CompressionCycleSettings.IMAGE_TO_COMPRESS_PATH = imageToCompressTemp; //set the image to compress
		CompressionCycleSettings.GRG_OUTPUT_PATH = grgOutputTemp; //set the grg outout
		CompressionCycleSettings.COMPRESSED_IMAGE_OUTPUT_PATH = compressedOutputTemp; //set the decompressed output
		//SELECT THE ALGORITHM
		if(grgFullBlockDCTButton.isSelected()){ 
			CompressionCycleSettings.algorithmType = CompressionAlgorithmType.GRG_FULL_BLOCKS_DCT; //use GRGF
		} else if(grgHalfBlockDCTButton.isSelected()){
			CompressionCycleSettings.algorithmType = CompressionAlgorithmType.GRG_HALF_BLOCKS_DCT; //use GRGH
		} else if(grgNoBlockDWTButton.isSelected()){ 
			CompressionCycleSettings.algorithmType = CompressionAlgorithmType.GRG_NO_BLOCKS_DWT; //use GRGN
		} else if(grgColourButton.isSelected()){
			CompressionCycleSettings.algorithmType = CompressionAlgorithmType.GRG_COLOUR; //use GRGC
		}
		
		this.setVisible(false); //close the form
		
		CompressionCycleSettings.printSettings();//print the settings to the console
	}
	
	//Constructor 
	//Sets up the GUI
	public CompressionCycleGUI(){
		setTitle("Please Select The Compression Settings"); //set the title
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		//set the default exit operation
	
		setBounds(100, 100, 600, 400); //set the bounds of the gui
		
		doneButton.addActionListener(this); //add the actionlistener to the button
		
		//Buttons
		imageToCompressButton.setActionCommand("Browse Image To Compress");
		grgOutputButton.setActionCommand("Browse GRG output");
		compressedResultOutputButton.setActionCommand("Browse Compress Output");		
		imageToCompressButton.addActionListener(this);
		grgOutputButton.addActionListener(this);
		compressedResultOutputButton.addActionListener(this);
		
		//Radio buttons
		algorithmsGroup.add(grgFullBlockDCTButton);
		algorithmsGroup.add(grgHalfBlockDCTButton);
		algorithmsGroup.add(grgNoBlockDWTButton);
		algorithmsGroup.add(grgColourButton);
		grgFullBlockDCTButton.addActionListener(this);
		grgHalfBlockDCTButton.addActionListener(this);
		grgNoBlockDWTButton.addActionListener(this);
		grgColourButton.addActionListener(this);
		
		//Main Panel
		this.setContentPane(mainPanel);
		
		//Add the labels to the panel and buttons
		mainPanel.add(imageToCompressLabel);
		mainPanel.add(imageToCompressButton);
		mainPanel.add(grgOutputLabel);
		mainPanel.add(grgOutputButton);
		
		mainPanel.add(compressedResultOutputLabel);	
		mainPanel.add(compressedResultOutputButton);		
		
		//add the radio buttons to the panel
		mainPanel.add(grgFullBlockDCTButton);
		mainPanel.add(grgHalfBlockDCTButton);
		mainPanel.add(grgNoBlockDWTButton);
		mainPanel.add(grgColourButton);
		mainPanel.add(doneButton);
	}

	private JPanel mainPanel = new JPanel(new GridLayout(6, 2)); //the panel with a grid layout
	
	private JButton doneButton = new JButton("Save"); //done button 
	
	//Labels
	private JLabel imageToCompressLabel = new JLabel("Select and image to compress");
	private JLabel grgOutputLabel = new JLabel("Select where to store the compressed output '.grg' file");
	private JLabel compressedResultOutputLabel = new JLabel("Select where to store the compressed output '.jpg' file");

	//Buttons
	private JButton imageToCompressButton = new JButton("Browse");
	private JButton grgOutputButton = new JButton("Browse");
	private JButton compressedResultOutputButton = new JButton("Browse");
	
	//Radio buttons
	private JRadioButton grgFullBlockDCTButton = new JRadioButton("GRG FULL BLOCKS DCT");
	private JRadioButton grgHalfBlockDCTButton = new JRadioButton("GRG HALF BLOCKS DCT");
	private JRadioButton grgNoBlockDWTButton = new JRadioButton("GRG NO BLOCKS DWT");
	private JRadioButton grgColourButton = new JRadioButton("GRG FULL BLOCKS DCT COLOUR");
	private ButtonGroup algorithmsGroup = new ButtonGroup();
}
