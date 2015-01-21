package jef.net.socket.bio;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import jef.tools.security.IChannel;

public class LineSocketChannel implements IChannel{
	private Socket sc;
	private BufferedReader scIn;
	private OutputStream scOut;
	private String charset;

	public LineSocketChannel(Socket sc,String charset){
		this.sc=sc;
		this.charset=charset;
		try {
			this.scIn=new BufferedReader(new InputStreamReader(sc.getInputStream(),charset));
			this.scOut=new BufferedOutputStream(sc.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public byte[] read() throws IOException {
		String s=scIn.readLine();
		return s.getBytes(charset);
	}

	public void write(byte[] encode) throws IOException {
		scOut.write(encode);
		scOut.write('\n');
		scOut.flush();
	}

	public void close() throws IOException {
		sc.close();
	}

	public Socket getSocket() {
		return sc;
	}

}
