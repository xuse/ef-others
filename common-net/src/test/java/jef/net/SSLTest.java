package jef.net;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class SSLTest {

	private static SSLServerSocket ss;

	public static void initSSLServerSocket(int port) throws Exception {
		KeyStore ks = KeyStore.getInstance("JKS"); // 创建服务器证书密钥库
		KeyStore sks = KeyStore.getInstance("PKCS12"); // 创建根证书密钥库
		ks.load(new FileInputStream("c:/key.cert"), "123456".toCharArray());

		// sks.load(new FileInputStream(caCert), caCertKeyStorePass);

		// 创建管理JKS密钥库的X.509密钥管理器
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, "123456".toCharArray());
		System.out.println("[信息] 初始化服务器证书密钥管理器成功");
		// skmf.init(sks);
		System.out.println("[信息] 初始化根证书密钥管理器成功");
		SSLContext sslContext = SSLContext.getInstance("SSLv3");

		sslContext.init(kmf.getKeyManagers(), null, null);
		System.out.println("[信息] 初始化SSL上下文成功");
		ss = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(port);
		ss.setNeedClientAuth(true);
	}

	public static void main(String[] args) {
		int port = 99;
		try {
			initSSLServerSocket(port);
			System.out.println("服务器在端口 [" + port + "] 等待连接...");
			while (true) {
				SSLSocket socket = (SSLSocket) ss.accept();
				new CreateThread(socket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class CreateThread extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket s;

		public CreateThread(Socket socket) {
			try {
				s = socket;
				in = new BufferedReader(new InputStreamReader(s.getInputStream(), "GB2312"));
				out = new PrintWriter(s.getOutputStream(), true);
				start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public void run() {
			try {
				String msg = in.readLine();
				System.out.println("接收到: " + msg);
				out.write("服务器接收到的信息是: " + msg);
				out.flush();
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
