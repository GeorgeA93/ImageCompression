package com.allen.george;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.allen.george.gui.GUI;


public class Program {
	
	//The entry point to the program
	public static void main(String[] args){
		//Load opencv
		System.loadLibrary("opencv_java2410");
		
		//invoke the gui thread
		SwingUtilities.invokeLater(new Runnable() {
			//thread run method
			public void run() {
				
				try {					
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //cross platform look and feel					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
				GUI g = new GUI(); //new gui
				g.setVisible(true); //make the gui appear on screen
				g.setResizable(false); //do not allow the resizing of the gui
			}
		});		
	}
	

}
