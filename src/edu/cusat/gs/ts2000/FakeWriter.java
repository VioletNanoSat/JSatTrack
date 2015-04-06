package edu.cusat.gs.ts2000;

import java.io.IOException;
import java.io.Writer;

public class FakeWriter extends Writer {

	private String lastcmd; 
	
	@Override
	public void close() throws IOException { }

	@Override
	public void flush() throws IOException { }

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		lastcmd = new String(cbuf, off, len);
	}
	
	public String getLastCmd(){ return lastcmd; }
	
}

