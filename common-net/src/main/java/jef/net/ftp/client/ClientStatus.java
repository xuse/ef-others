package jef.net.ftp.client;

import java.io.IOException;

/**
 * 描述一个目录的状态
 * @author Administrator
 *
 */
final class ClientStatus {
	public ClientStatus(jef.net.ftp.client.AbstractFtpClient parent,String path){
		this.path=path;
		this.ftp=parent;
	}
	
	/**
	 * 父类
	 */
	private jef.net.ftp.client.AbstractFtpClient ftp;
	/**
	 * 当前路径
	 */
	private String path;
	
	/**
	 * 当前文件
	 */
	private FtpEntry[] files;
	
	/**
	 * 总大小
	 */
	private long total;

	public String getPath() {
		if(path.endsWith("/"))return path.substring(0,path.length()-1);
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public FtpEntry[] getFiles() {
		if(files==null){
			try {
				initFiles();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return files;
	}
	
	private void initFiles() throws IOException{
		this.files=ftp.getEntries();
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public FtpEntry getEntry(String name) throws IOException {
		if(files==null){
			this.initFiles();
		}
		for(FtpEntry e: files){
			if(e.getName().equalsIgnoreCase(name)){
				return e;
			}
		}
		return null;
	}
}
