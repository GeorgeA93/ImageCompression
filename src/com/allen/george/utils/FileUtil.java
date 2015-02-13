package com.allen.george.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;


//Class to read and write bytes to files using Apache Commons IOUTILS
public class FileUtil {
	
	//Method to write bytes to a file
	//Takes in the file path and the bytes to write
	public static void writeBytes(String filePath, byte[] dataBytes) {
		try {
			IOUtils.write(dataBytes, new FileOutputStream(new File(filePath)));
		} catch (Exception e) {			
			e.printStackTrace();
		} 
	}
	
	//Method to read bytes from a file.
	//Takes in a file path
	//Returns the file contents as a string.
	public static byte[] readBytes(String filePath) {
		try {
			return IOUtils.toByteArray(new FileInputStream(new File(filePath)));
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return null;
	}
	
}
