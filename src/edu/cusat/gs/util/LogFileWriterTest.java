package edu.cusat.gs.util;


import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Test;
public class LogFileWriterTest {
	@Test
	public void testLogFileWriter(){
		// Test strings
		final String s1 = "Write this into file";
		final String s2 = "What happens, ";
		final String s3 = "When we split a line?";
		final String s4 = "This should be the 3rd line";
		final String s5 = "When there's a newline in the string\nThere's only 1 date";
		
		// The log directory
		final File dir = new File("./data/logs");
		
		// Clean directory of all files (not directories)
		for (File f : dir.listFiles()) { if(!f.isDirectory()) f.delete(); }
		
		// Create the LFW to test
		new LogFileWriter();
		
		// Send a message to System.out, which should be redirected to LFW
		System.out.println(s1);
		System.out.print(s2);
		System.out.println(s3);
		System.out.println(s4);
		System.out.println(s5);
				
		// find all files with the extension .log
		String[] fnames = dir.list(new FilenameFilter() { 
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".log"); 
			}
		});
		
		BufferedReader in = null;
		try {
			// Assume it's the only .log file there
			in = new BufferedReader(new FileReader(new File(dir, fnames[0]))); 
			String read = in.readLine();
			assertEquals(s1,read.substring(read.lastIndexOf(":")+2, read.length()));
			read = in.readLine();
			assertEquals(s2+s3,read.substring(read.lastIndexOf(":")+2, read.length()));
			read = in.readLine();
			assertEquals(s4, read.substring(read.lastIndexOf(":")+2, read.length()));
			read = in.readLine();
			assertEquals(s5.substring(0, s5.indexOf("\n")), read.substring(read.lastIndexOf(":")+2, read.length()));
			read = in.readLine();
			assertEquals(s5.substring(s5.indexOf("\n")+1), read);
		} catch(IOException e){
			System.out.println("There was a problem:" + e);
		} finally { //safely close the BufferedReader after use
			try { in.close(); } catch (IOException e) { }
		}
	}
}
