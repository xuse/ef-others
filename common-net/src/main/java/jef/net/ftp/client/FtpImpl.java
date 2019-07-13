/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.net.ftp.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jef.common.JefException;
import jef.common.log.LogUtil;
import jef.inner.sun.net.ftp.FtpProtocolException;
import jef.net.ftp.FtpConnectInfo;
import jef.net.ftp.client.listparsers.DOSListParser;
import jef.net.ftp.client.listparsers.EPLFListParser;
import jef.net.ftp.client.listparsers.MLSDListParser;
import jef.net.ftp.client.listparsers.NetWareListParser;
import jef.net.ftp.client.listparsers.UnixListParser;
import jef.tools.ArrayUtils;
import jef.tools.Assert;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

/**
 * FtpClient的实现。
 * @author Jiyi
 *
 */
public class FtpImpl extends AbstractFtpClient implements Ftp{
	//基本连接信息
	private String host;
	private String username;
	private String password;
	private int port = 21;
	private boolean debug;
	
	//当前目录信息
	private ClientStatus state;
	
	//文件目录编码等信息
	protected String defaultEncoding=Charset.defaultCharset().name();
	protected FTPListParser parser;
	FtpClient ftpClient = null;
	
	/**
	 * 构造并连接
	 * @param info
	 */
	public FtpImpl(FtpConnectInfo info){
		this(info.getHost(),info.getPort(),info.getUser(),info.getPassword());
		this.connect(info.getHomeDir());
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;
		if(ftpClient!=null)
			ftpClient.setDebug(debug);
	}
	
	/**
	 * 构造
	 * @param serverIP
	 * @param username
	 * @param password
	 */
	public FtpImpl(String serverIP, String username, String password) {
		this.host = serverIP;
		this.username = username;
		this.password = password;
	}

	/**
	 * 构造
	 * @param serverIP
	 * @param port
	 * @param username
	 * @param password
	 */
	public FtpImpl(String serverIP, int port, String username, String password) {
		this.host = serverIP;
		this.username = username;
		this.password = password;
		this.port = port;
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#connect(java.lang.String)
	 */
	public boolean connect(String home) {
		ftpClient = new FtpClient();
		try {
			if (this.port > 0) {
				ftpClient.openServer(host, port);
			} else {
				ftpClient.openServer(host);
			}
			if(username==null){
				ftpClient.login("anonymous", "@123");
			}else{
				ftpClient.login(username, password);
			}
			LogUtil.show(ftpClient.welcomeMsg);
			
			if (home != null && home.length() > 0) {
				ftpClient.cd(home);// path是ftp服务下主目录的子目录
			}
			ftpClient.setDebug(debug);
			ftpClient.binary();
			this.state=new ClientStatus(this, ftpClient.pwd());
			System.out.println("Login to \"" + ftpClient.pwd() + "\" directory");
			return true;
		} catch (IOException e) {
			Exceptions.log(e);
			return false;
		}	
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#connect()
	 */
	public boolean connect(){
		return connect(null);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException{
		if(ftpClient==null)return;
		ftpClient.closeServer();
		System.out.println("FTP:"+ this.host+" disconnected!");
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#pwd()
	 */
	public String pwd() throws IOException{
		ensureFtpOpen();
		return ftpClient.pwd();
	}

	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#upload(java.io.File[])
	 */
	public void upload(File... files) throws IOException{
		ensureFtpOpen();
		for(File file: files){
			doUpload(file,null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#doUpload(java.io.File, java.lang.String)
	 */
	public boolean doUpload(File file_in, String newName) throws IOException{
		if(ftpClient==null)throw new RuntimeException("The server is not connected!");
		if (!file_in.exists()) {
			throw new IOException("The file[" + file_in.getName() + "]is error or not exist!s");
		}
		if (file_in.isDirectory()) {
			innerUpload(file_in, newName);
		} else { //普通文件
			uploadFile(file_in, newName);	
		}
		return true;
	}

	/*
	 * 真正用于上传的方法
	 * @param localFile 本地文件
	 * @param remoteFolder 指定的上传文件夹
	 * @param path 当前上传路径
	 */
	private void innerUpload(File file_in, String renameTo) throws IOException {
		if (!file_in.exists()) {
			throw new IOException("The file[" + file_in.getName() + "]is error or not exist!s");
		}
		if(StringUtils.isEmpty(renameTo)){
			renameTo=file_in.getName();
		}
		Assert.notContainsAnyChar(renameTo,'\\','/');
		FtpEntry entry=state.getEntry(renameTo);
		if (file_in.isDirectory()) {//是文件夹
			if(entry==null){
				createDir(renameTo);	
			}
			cd(renameTo);
			//开始上传文件夹内部的内容
			for (File sub:file_in.listFiles()) {
				innerUpload(sub, sub.getName());
			}
			cdUp();//传完出来
		}else{
			if(entry==null){
				uploadFile(file_in, renameTo);
			}else{
				this.delete(renameTo);
				uploadFile(file_in, renameTo);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#uploadFile(java.io.File, java.lang.String)
	 */
	public long uploadFile(File file, String newname) throws IOException {
		if(ftpClient==null)throw new RuntimeException("The server is not connected!");
		if (!file.exists() || file.isDirectory()){
			LogUtil.error("FTP:Local file "+ file + " is not exist.");
			return -1;
		}
		if(StringUtils.isEmpty(newname))newname=file.getName();
		IOUtils.copy(new FileInputStream(file), ftpClient.put(newname),true);
		return file.length();
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#download(java.lang.String, java.io.File)
	 */
	public long download(String filename, File outfile) {
		if(ftpClient==null)throw new RuntimeException("The server is not connected!");
		long result = 0;
		InputStream is = null;
		FileOutputStream os = null;
		try {
			is = ftpClient.get(filename);
			os = new FileOutputStream(outfile);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
				result = result + c;
			}
		} catch (IOException e) {
			Exceptions.log(e);
		} finally {
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				Exceptions.log(e);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#cd(java.lang.String)
	 */
	public boolean cd(String path) throws IOException{
		try{
			ftpClient.cd(path);	
		}catch(FtpProtocolException e){
			LogUtil.warn("Can not CD to "+path +", the path is not exist.");
			return false;
		}
		refreshDir();
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#dir(java.lang.String)
	 */
	public List<String> dir(String path) throws IOException {
		if(ftpClient==null)throw new RuntimeException("The server is not connected!");
		path=path.replace('\\', '/');
		if(!path.startsWith("/"))path=ftpClient.pwd()+"/"+path;
		List<String> list = new ArrayList<String>();
		BufferedReader dis=null;
		try {
			dis = new BufferedReader(new InputStreamReader(ftpClient.nameList(path)));
			String filename = "";
			while ((filename = dis.readLine()) != null) {
				list.add(filename);
			}
		} catch (IOException e) {
			Exceptions.log(e);
		}finally{
			IOUtils.closeQuietly(dis);
		}
		return list;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#dir(java.lang.String, java.lang.String[])
	 */
	public List<String> dir(String path, final String... extnames) throws IOException {
		List<String> list = dir(path);
		for(int i=list.size()-1; i>=0; i--){
			String fileName = list.get(i);
			if (!ArrayUtils.contains(extnames, IOUtils.getExtName(fileName))) {
				list.remove(fileName);
			}
		}
		return list;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#list()
	 */
	public FtpEntry[] list() throws IOException{
		return state.getFiles();
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#listAsString()
	 */
	public String listAsString() throws IOException {
		ensureFtpOpen();
		return IOUtils.asString(ftpClient.list());
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#list(java.lang.String)
	 */
	public String list(String path) throws IOException{
		ensureFtpOpen();
		return IOUtils.asString(ftpClient.nameList(path));
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#delete(java.lang.String)
	 */
	public void delete(String path) throws IOException{
		ensureFtpOpen();
		deleteInCurrentFolder(path);
	}
	
	/*
	 * 在当前路径下删除相对路径的文件和文件夹
	 * @param path
	 * @throws IOException
	 */
	private void deleteInCurrentFolder(String path) throws IOException {
		FtpEntry entry=state.getEntry(path);
		if(entry==null){
			LogUtil.warn("dele file"+path+" failure, not found.");
			return;
		}
		if(entry.getType()==FtpEntry.TYPE_DIRECTORY){
			deleteFilesInDir(entry);
			ftpClient.rd(entry.getName());
		}else{
			ftpClient.delete(entry.getName());
		}
		LogUtil.show(ftpClient.getResponse());
	}
	
	/*
	 * 删除一个目录下的全部文件
	 * @param entry 文件夹的Entry;
	 * @throws IOException
	 */
	private void deleteFilesInDir(FtpEntry entry) throws IOException {
		Assert.isTrue(entry.getType()==FtpEntry.TYPE_DIRECTORY);
		cd(entry.getName());
		for(FtpEntry e: state.getFiles()){
			if(e.getType()==FtpEntry.TYPE_DIRECTORY){
				deleteFilesInDir(e);
				ftpClient.rd(e.getName());
			}else{
				ftpClient.delete(e.getName());
			}
		}
		this.cdUp();
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#rd(java.lang.String)
	 */
	public void rd(String path) throws IOException{
		ensureFtpOpen();
		ftpClient.rd(path);
		LogUtil.show(ftpClient.getResponse());
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#execute(java.lang.String)
	 */
	public String execute(String cmd) throws IOException{
		return ftpClient.execute(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#binary()
	 */
	public void binary() throws IOException{
		ftpClient.binary();
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#ascii()
	 */
	public void ascii() throws IOException{
		ftpClient.ascii();
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#createDir(java.lang.String)
	 */
	public boolean createDir(String dir) {
		Assert.isNotEmpty(dir);
		ensureFtpOpen();
		String[] dirs=StringUtils.split(dir,"/");
		StringBuilder sb=new StringBuilder(state.getPath());
		try {
			ftpClient.ascii();
			for(String d:dirs){
				sb.append('/').append(d);
				ftpClient.execute("MKD " + sb.toString());
			}
			ftpClient.binary();
			return true;
		} catch (IOException e1) {
			Exceptions.log(e1);
			return false;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#cdUp()
	 */
	public void cdUp() throws IOException {
		ftpClient.cdUp();
		refreshDir();
	}
	
	/*
	 * 当目录改变后刷新
	 */
	private void refreshDir() throws IOException {
		String current=state.getPath();
		String newpath=ftpClient.pwd();
		if(current.equals(newpath)){
			return;
		}
		state=new ClientStatus(this, newpath);
	}

	/*
	 */
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#setDefaultEncoding(java.lang.String)
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/*
	 * (non-Javadoc)
	 * @see jef.net.ftp.client.IFtp#rename(java.lang.String, java.lang.String)
	 */
	public void rename(String from, String to) throws IOException{
		ftpClient.rename(from, to);
	}
	/*
	 * 检查连接
	 */
	protected void ensureFtpOpen() {
		String message="The ftp has not connected!";
		Assert.notNull(ftpClient,message);
		Assert.isTrue(ftpClient.serverIsOpen(),message);
	}
	
	private static ArrayList<FTPListParser>listParsers = new ArrayList<FTPListParser>();
	static{
		listParsers.add(new UnixListParser());
		listParsers.add(new DOSListParser());
		listParsers.add(new EPLFListParser());
		listParsers.add(new NetWareListParser());
		listParsers.add(new MLSDListParser());
	}

	protected FtpEntry[] getEntries() throws IOException {
		ensureFtpOpen();
		BufferedReader reader=IOUtils.getReader(ftpClient.list(), defaultEncoding);
		try{
			List<String> list=new ArrayList<String>();
			String line=null;
			while((line=reader.readLine())!=null){
				list.add(line);
			}
			String[] data=list.toArray(new String[list.size()]);
			FtpEntry[] ret=null;
			if (parser != null) {
				try {
					ret = parser.parse(data);
				} catch (JefException e) {
					parser = null;
				}
			}
			if (ret == null) {
				for (Iterator<FTPListParser> i = listParsers.iterator(); i.hasNext();) {
					FTPListParser aux = (FTPListParser) i.next();
					try {
						ret = aux.parse(data);
						parser = aux;
						break;
					} catch (JefException e) {
						continue;
					}
				}
			}
			return ret;
		}finally{
			reader.close();
		}
	}

	public boolean isConnected() {
		return ftpClient!=null && ftpClient.serverIsOpen();
	}

//FTP远程命令列表<br>
//USER    PORT    RETR    ALLO    DELE    SITE    XMKD    CDUP    FEAT<br>
//PASS    PASV    STOR    REST    CWD     STAT    RMD     XCUP    OPTS<br>
//ACCT    TYPE    APPE    RNFR    XCWD    HELP    XRMD    STOU    AUTH<br>
//REIN    STRU    SMNT    RNTO    LIST    NOOP    PWD     SIZE    PBSZ<br>
//QUIT    MODE    SYST    ABOR    NLST    MKD     XPWD    MDTM    PROT<br>
//     在服务器上执行命令,如果用sendServer来执行远程命令(不能执行本地FTP命令)的话，所有FTP命令都要加上 <br>

}
