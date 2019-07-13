package jef.net.ftp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import jef.common.log.LogUtil;
import jef.net.ftp.FtpConnectInfo;
import jef.tools.Assert;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.reflect.BeanUtils;

/**
 * SFTP的实现
 * @author Administrator
 *
 */
public class SFtpImpl extends jef.net.ftp.client.AbstractFtpClient implements Ftp{
	private Session session;
	private ChannelSftp ftp;
	//基本连接信息
	private String host;
	private String username;
	private String password;
	private String defaultEncoding;
	private int port = 22;
	private boolean debug=false;
	//当前目录信息
	private ClientStatus state;
	/**
	 * 构造并连接
	 * @param info
	 */
	public SFtpImpl(FtpConnectInfo info){
		this(info.getHost(),info.getPort(),info.getUser(),info.getPassword());
		this.connect(info.getHomeDir());
	}
	
	/**
	 * 构造
	 * @param serverIP
	 * @param username
	 * @param password
	 */
	public SFtpImpl(String serverIP, String username, String password) {
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
	public SFtpImpl(String serverIP, int port, String username, String password) {
		this.host = serverIP;
		this.username = username;
		this.password = password;
		this.port = port;
	}
	
	
	public boolean connect(String home) {
		JSch jsch = new JSch();
		try{
			jsch.getSession(username, host, port);
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			session.setConfig(sshConfig);
			session.connect();
			debug("Session connected.");
			ftp = (ChannelSftp)session.openChannel("sftp");
			ftp.connect();
			if(defaultEncoding!=null){
				int version=ftp.getServerVersion();
				if(version>=3 && version<=5){
					BeanUtils.setFieldValue(ftp, "fEncoding", defaultEncoding);
					BeanUtils.setFieldValue(ftp, "fEncoding_is_utf8", "UTF-8".equals(defaultEncoding));	
				}else{
					ftp.setFilenameEncoding(defaultEncoding);	
				}
				if(debug){
					LogUtil.show("setting FilenameEncoding to "+defaultEncoding);
				}
			}
			//System.out.println(ftp.getServerVersion());
			debug("SFTP client Connected to " + host);
			if(home!=null){
				if(debug){
					LogUtil.show("CD "+home);
				}
				ftp.cd(home);
			}
			this.state=new ClientStatus(this, ftp.pwd());
			return true;	
		}catch(Exception e){
			Exceptions.log(e);
			return false;
		}
	}
	
	/*
	 * 当目录改变后刷新
	 */
	private void refreshDir() throws SftpException  {
		String current=state.getPath();
		String newpath=ftp.pwd();
		if(debug){
			LogUtil.show("PWD = "+newpath);
		}
		if(current.equals(newpath)){
			return;
		}
		state=new ClientStatus(this, newpath);
	}

	private void debug(String string) {
		if(debug){
			LogUtil.show(string);
		}
	}

	public boolean connect() {
		return connect(null);
	}

	public void close() throws IOException {
		ftp.disconnect();
		session.disconnect();
		if(debug){
			LogUtil.show("Sftp to "+this.host+":"+this.port+" disconnected.");
		}
	}

	public String pwd() throws IOException {
		try {
			String result= ftp.pwd();
			if(debug){
				LogUtil.show("PWD = "+result);
			}
			return result;
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	public void upload(File... files) throws IOException {
		ensureFtpOpen();
		for(File file: files){
			doUpload(file,null);
		}
	}

	private void ensureFtpOpen() {
		String message="The ftp has not connected!";
		Assert.notNull(ftp,message);
		Assert.isTrue(ftp.isConnected(),message);
	}

	public boolean doUpload(File file_in, String newName) throws IOException {
		if(ftp==null)throw new RuntimeException("The server is not connected!");
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

	public long uploadFile(File file, String newname) throws IOException {
		if(ftp==null)throw new RuntimeException("The server is not connected!");
		if (!file.exists() || file.isDirectory()){
			LogUtil.error("FTP:Local file "+ file + " is not exist.");
			return -1;
		}
		if(StringUtils.isEmpty(newname))newname=file.getName();
		InputStream in=new FileInputStream(file);
		try{
			if(debug){
				LogUtil.show("PUT "+file.getAbsolutePath()+" "+newname);
			}
			ftp.put(in, newname);
		} catch (SftpException e) {
			throw new IOException(e);
		}finally{
			IOUtils.closeQuietly(in);
		}
		return file.length();
	}

	public long download(String filename, File outfile) {
		if(ftp==null)throw new RuntimeException("The server is not connected!");
		long result = 0;
		InputStream is = null;
		FileOutputStream os = null;
		try {
			if(debug){
				LogUtil.show("GET "+filename+" "+outfile.getAbsolutePath());
			}
			is = ftp.get(filename);
			os = new FileOutputStream(outfile);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
				result = result + c;
			}
		} catch (SftpException e) {
			Exceptions.log(e);
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

	public boolean cd(String path) throws IOException {
		if(debug){
			LogUtil.show("CD "+path);
		}
		try {
			ftp.cd(path);
		} catch (SftpException e) {
			LogUtil.warn("Can not CD to "+path +", the path is not exist.");
			return false;
		}
		try {
			refreshDir();
			return true;
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	public List<String> dir(String path) throws IOException {
		try {
			if(debug){
				LogUtil.show("LIST "+path);
			}
			@SuppressWarnings("unchecked")
			Vector<LsEntry> c=ftp.ls(path);
			List<String> result=new ArrayList<String>();
			for(LsEntry e:c){
				result.add(e.getFilename());
			}
			return result;
		} catch (SftpException e) {
			throw new IOException(e);
		}	
	}

	public List<String> dir(String path, String... extnames) throws IOException {
		try {
			if(debug){
				LogUtil.show("LIST "+path);
			}
			@SuppressWarnings("unchecked")
			Vector<LsEntry> c=ftp.ls(path);
			List<String> result=new ArrayList<String>();
			for(LsEntry e:c){
				String fileName=e.getFilename();
				boolean flag=false;
				if(extnames.length>0){
					String ext=IOUtils.getExtName(fileName);
					for(String pattern:extnames){
						if(pattern.equals(ext)){
							flag=true;
							 break;
						}
					}
				}else{
					flag=true;
				}
				if(flag)result.add(fileName);
			}
			return result;
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	public FtpEntry[] list() throws IOException {
		return state.getFiles();
	}

	public String listAsString() throws IOException {
		return list(".");
	}

	public String list(String path) throws IOException {
		StringBuilder sb=new StringBuilder();
		Vector v;
		try {
			if(debug){
				LogUtil.show("LIST "+path);
			}
			v = ftp.ls(path);
		} catch (SftpException e1) {
			throw new IOException(e1);
		}
		for(Object o:v){
			LsEntry e2=(LsEntry)o;
			sb.append(e2.getLongname());
			sb.append('\n');
		}
		return sb.toString();
	}

	public void delete(String path) throws IOException {
		ensureFtpOpen();
		try {
			deleteInCurrentFolder(path);
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	/*
	 * 删除一个目录下的全部文件
	 * @param entry 文件夹的Entry;
	 * @throws IOException
	 */
	private void deleteFilesInDir(FtpEntry entry) throws  SftpException, IOException {
		Assert.isTrue(entry.getType()==FtpEntry.TYPE_DIRECTORY);
		cd(entry.getName());
		for(FtpEntry e: state.getFiles()){
			if(e.getType()==FtpEntry.TYPE_DIRECTORY){
				deleteFilesInDir(e);
				if(debug){
					LogUtil.show("RMD "+e.getName());
				}
				ftp.rmdir(e.getName());
			}else{
				if(debug){
					LogUtil.show("DELE "+e.getName());
				}
				ftp.rm(e.getName());
			}
		}
		this.cdUp();
	}
	
	private void deleteInCurrentFolder(String path) throws IOException, SftpException {
		FtpEntry entry=state.getEntry(path);
		if(entry==null){
			LogUtil.warn("dele file"+path+" failure, not found.");
			return;
		}
		if(entry.getType()==FtpEntry.TYPE_DIRECTORY){
			deleteFilesInDir(entry);
			if(debug){
				LogUtil.show("RMD "+entry.getName());
			}
			ftp.rmdir(entry.getName());
		}else{
			if(debug){
				LogUtil.show("DELE "+entry.getName());
			}
			ftp.rm(entry.getName());
		}
	}

	public void rd(String path) throws IOException {
		ensureFtpOpen();
		try {
			if(debug){
				LogUtil.show("RMD "+path);
			}
			ftp.rmdir(path);
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	public String execute(String cmd) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void binary() throws IOException {
		//do nothing;
	}

	public void ascii() throws IOException {
		//do nothing;
	}

	public boolean createDir(String dir) {
		try {
			if(debug){
				LogUtil.show("MKD "+dir);
			}
			ftp.mkdir(dir);
			return true;
		} catch (SftpException e) {
			Exceptions.log(e);
			return false;
		}
	}

	public void cdUp() throws IOException {
		String currentDir=state.getPath();
		if(currentDir.length()<=1)return;//已经在根目录下了
		int index=currentDir.lastIndexOf('/', currentDir.length()-1);
		String newDir=currentDir.substring(0,index+1);
		cd(newDir);
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding=defaultEncoding;
	}

	public void rename(String from, String to) throws IOException {
		try {
			if(debug){
				LogUtil.show("RNFR "+from+"\nRNTO "+to);
			}
			ftp.rename(from, to);
		} catch (SftpException e) {
			throw new IOException(e);
		}
	}

	protected FtpEntry[] getEntries() throws IOException {
		@SuppressWarnings("rawtypes")
		Vector v;
		try {
			if(debug){
				LogUtil.show("LIST .");
			}
			v = ftp.ls(".");
		} catch (SftpException e1) {
			throw new IOException(e1);
		}
		FtpEntry[] result=new FtpEntry[v.size()];
		int n=0;
		for(Object o:v){
			LsEntry e2=(LsEntry)o;
			FtpEntry e=new FtpEntry();
			SftpATTRS attr=e2.getAttrs();
			e.setName(e2.getFilename());
			e.setModifiedDate(new Date(attr.getMTime()*1000L));
			if(attr.isLink()){
				e.setType(FtpEntry.TYPE_LINK);
				
				try {
					String link = ftp.readlink(e2.getFilename());
					if(link!=null && link.length()>0){
						e.setLink(link);
					}
				} catch (SftpException e1) {
					Exceptions.log(e1);
				}
			}else if(attr.isDir()){
				e.setType(FtpEntry.TYPE_DIRECTORY);
			}else{
				e.setType(FtpEntry.TYPE_FILE);
			}
			e.setSize(attr.getSize());	
			result[n++]=e;
		}
		return result;
	}

	public boolean isConnected() {
		return ftp!=null && ftp.isConnected();
	}

	public void setDebug(boolean debug) {
		this.debug=debug;
	}
}
