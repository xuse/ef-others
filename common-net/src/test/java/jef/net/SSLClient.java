package jef.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

public class SSLClient {
	static int port = 99;

	public static void main(String args[]) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			Socket s = factory.createSocket("localhost", port);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "GB2312"));
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			out.println("证书启用成功!");
			System.out.println(in.readLine());
			out.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
