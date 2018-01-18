package jef.http.client.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import jef.common.log.LogUtil;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从https站点下载并安装SSL证书，注意：需要对JRE目录有写权限
 * 
 * @author Administrator
 *
 */
public class InstallCert {
	private static Logger log = LoggerFactory.getLogger(InstallCert.class);
	private char[] passphrase = "changeit".toCharArray();
	private String host;
	private int port;
	private File jreSecurity;

	/**
	 * 构造，需要传入一个站点域名，默认使用443端口
	 * 
	 * @param host
	 *            加密的站点域名
	 */
	public InstallCert(String host) {
		this(host, 443);
	}

	/**
	 * 构造，需要传入一个站点域名和端口
	 * 
	 * @param host
	 *            加密的站点域名
	 * @param port
	 *            端口
	 */
	public InstallCert(String host, int port) {
		this.host = host;
		if (StringUtils.isEmpty(host)) {
			throw new IllegalArgumentException("You must assign a host!");
		}
		if (port < 1)
			port = 443;
		this.port = port;
		jreSecurity = new File(System.getProperty("java.home"), "lib/security");
		if (!jreSecurity.exists()) {
			log.error("请确认java安装路径并确认对目录有权限:{}", jreSecurity.getAbsolutePath());
		}
	}

	/**
	 * 握手并安装证书 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void install() throws IOException, GeneralSecurityException {
		File storeFile = getStoreFile();
		KeyStore ks = loadKeyStore(storeFile);

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();

		// 开始连接新的SSL站点
		log.debug("Opening connection to {}:{}...", host ,port);
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		try {
			socket.setSoTimeout(10000);
			log.debug("Starting SSL handshake...");
			socket.startHandshake();
			log.debug("No errors, certificate is already trusted");
			return;
		} catch (SSLException e) {
			log.error("SSL not trusted.",e);
		} finally {
			IOUtils.closeQuietly(socket);
		}

		X509Certificate[] chain = tm.chain;
		if (chain == null || chain.length == 0) {
			throw new GeneralSecurityException("Could not obtain server certificate chain");
		}
		LogUtil.debug("The server sent {} certificate(s):"+ chain.length);
		//取消注释后可以打印出证书
		//printCerts(tm.chain);

		X509Certificate cert = chain[0];
		String alias = host + "-" + 1;
		ks.setCertificateEntry(alias, cert);
		OutputStream out = new FileOutputStream(storeFile);
		try {
			ks.store(out, passphrase);
			LogUtil.info("Added certificate to keystore " + storeFile.getAbsolutePath() + " using alias '" + alias + "'");
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private File getStoreFile() {
		String path = System.getProperty("javax.net.ssl.trustStore");
		if (StringUtils.isNotEmpty(path)) {
			return new File(path);
		}
		return new File(jreSecurity, "jssecacerts");
	}

	private KeyStore loadKeyStore(File storeFile) throws IOException, GeneralSecurityException {
		if (!storeFile.isFile()) {
			storeFile = new File(jreSecurity, "cacerts");
		}
		// 加载旧密钥
		InputStream in = new FileInputStream(storeFile);
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(in, passphrase);
			return ks;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}


	/**
	 * 用于保存证书的TrustManager
	 * 
	 * @author jiyi
	 */
	private static class SavingTrustManager implements X509TrustManager {
		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}


	/**
	 * 工具方法：打印出证书内容
	 * 
	 * @param chain
	 */
	public static void printCerts(X509Certificate[] chain) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			for (int i = 0; i < chain.length; i++) {
				X509Certificate cert = chain[i];
				System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
				System.out.println("   Issuer  " + cert.getIssuerDN());
				sha1.update(cert.getEncoded());
				System.out.println("   sha1    " + toHexString(sha1.digest()));
				md5.update(cert.getEncoded());
				System.out.println("   md5     " + toHexString(md5.digest()));
				System.out.println();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
	}

	private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

	private static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes) {
			b &= 0xff;
			sb.append(HEXDIGITS[b >> 4]);
			sb.append(HEXDIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}
}
