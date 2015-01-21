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

/**
 * 安装SSL证书，需要对JRE目录有写权限 
 * @author Administrator
 *
 */
public class InstallCert {
	private char[] passphrase="changeit".toCharArray();
	private String host;
	private int    port;
	private File  jreSecurity;
	
	public static void main(String[] args) throws Exception {
		InstallCert i=new InstallCert("",443);
		i.install();
	}

	public InstallCert(String host){
		this(host,443);
	}
	public InstallCert(String host,int port){
		this.host=host;
		if(StringUtils.isEmpty(host)){
			throw new IllegalArgumentException("You must assign a host!");
		}
		if(port<1)port=443;
		this.port=port;
		jreSecurity=new File(System.getProperty("java.home"),"lib/security");
	}
	
	public void install() throws IOException, GeneralSecurityException{
		File storeFile=getStoreFile();
		KeyStore ks=loadKeyStore(storeFile);

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();
		//开始连接新的SSL站点
		LogUtil.debug("Opening connection to " + host + ":" + port + "...");
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);
		try {
			LogUtil.debug("Starting SSL handshake...");
			socket.startHandshake();
			socket.close();
			LogUtil.debug("No errors, certificate is already trusted");
			return;
		} catch (SSLException e) {
			LogUtil.debug("SSL not trusted."+e.getMessage());
		}
		X509Certificate[] chain = tm.chain;
		if (chain == null || chain.length==0) {
			throw new GeneralSecurityException("Could not obtain server certificate chain");
		}
		LogUtil.debug("Server sent " + chain.length + " certificate(s):");
//		printCerts(tm.chain);
		
		X509Certificate cert = chain[0];
		String alias = host + "-" + (1);
		ks.setCertificateEntry(alias, cert);
		OutputStream out = new FileOutputStream(storeFile);
		try{
			ks.store(out, passphrase);	
		}finally{
			IOUtils.closeQuietly(out);
		}
		LogUtil.info("Added certificate to keystore "+storeFile.getAbsolutePath()+" using alias '" + alias + "'");
	}

	void printCerts(X509Certificate[] chain) {
		try{
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
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
	}

	private File getStoreFile() {
		String path=System.getProperty("javax.net.ssl.trustStore");
		if(StringUtils.isNotEmpty(path)){
			return new File(path);
		}
		return new File(jreSecurity,"jssecacerts");
	}

	private KeyStore loadKeyStore(File storeFile) throws IOException,  GeneralSecurityException{
		if(!storeFile.isFile()){
			storeFile=new File(jreSecurity, "cacerts");
		}
		//加载旧密钥
		InputStream in = new FileInputStream(storeFile);
		try{
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(in, passphrase);
			return ks;				
		}finally{
			IOUtils.closeQuietly(in);
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

}
