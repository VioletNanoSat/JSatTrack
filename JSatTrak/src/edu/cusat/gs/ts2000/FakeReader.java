package edu.cusat.gs.ts2000;

import java.io.IOException;
import java.io.Reader;

public class FakeReader extends Reader {
	
	String response = "";
	
	@Override
	public void close() throws IOException { }
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if("".equals(response)) return -1; // End of stream
		
		// Find out how much will be copied
		int copyamt = Math.min(off+len, response.length());
		
		// Do the copy
		System.arraycopy(response.toCharArray(), 0, cbuf, off, copyamt);
		
		// Remove the copied characters
		response = response.substring(copyamt);
		
		return copyamt; // Return amount copied
	}
	
	public void setResponse(String resp){
		response = resp;
	}
	
}