package jef.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jef.common.log.LogUtil;
import jef.http.server.UploadFile;
import jef.tools.Assert;
import jef.tools.DateUtils;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;

/**
 * 一个上传文件管理器的最简单实现
 * 
 * 上传文件管理器可以将UploadFile存储起来，供下次使用。
 * 同时为了防止文件长期存储后不用，允许设置过期时间
 * @author Administrator
 *
 */
public class UploadFileManager {
	private File folder;
	private RemoveExpireFileThread scanExpireFile;
	private static final int TYPE_UPLOADFILE=1;
	private static final int TYPE_OBJECT=2;
	
	/**
	 * 默认构造，文件过期时间一天
	 */
	public UploadFileManager(){
		this(DateUtils.MILLISECONDS_IN_DAY);
	}
	/**
	 * 初始化构造
	 * @param expireTime 文件过期时间，到期后自动删除。如果设置为0则文件永不过期
	 */
	public UploadFileManager(int expireTime){
		String path=System.getProperty("file.manager.path");
		if(StringUtils.isEmpty(path)){
			folder=new File(System.getProperty("java.io.tmpdir"),"FileManager");
		}else{
			folder=new File(path);
		}
		if(!folder.exists()){
			folder.mkdirs();
		}
		LogUtil.info("The SimpleFileManager initlized folder:" + folder.getPath());
		if(expireTime>0){
			scanExpireFile=new RemoveExpireFileThread(folder,expireTime);
			scanExpireFile.setDaemon(true);
			scanExpireFile.start();	
		}
	}
	
	/**
	 * 获取文件
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public UploadFile load(String id) throws IOException{
		Assert.notNull(id);
		File file=new File(folder,id);
		if(!file.isFile())return null;
		InputStream in=IOUtils.getInputStream(file);
		try{
			DataInputStream din=new DataInputStream(in);
			int type=din.readByte();
			Assert.equals(type, TYPE_UPLOADFILE);
			return loadUploadFile(din,in);
		}finally{
			IOUtils.closeQuietly(in);
		}
	}
	
	/*
	 * 
	 */
	private UploadFile loadUploadFile(DataInputStream din,InputStream in) throws IOException {
		String localPath=din.readUTF();
		@SuppressWarnings("unused")
		long created=din.readLong();
		File tmpFile=IOUtils.saveAsTempFile(in);
		UploadFile result=new UploadFile(tmpFile,localPath,null);
		return result;
	}
	
	/**
	 * 根据指定的id从本地存储中加载对象
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public Object loadObject(String id) throws IOException{
		Assert.notNull(id);
		File file=new File(folder,id);
		if(!file.isFile())return null;
		InputStream in=IOUtils.getInputStream(file);
		try{
			DataInputStream din=new DataInputStream(in);
			int type=din.readByte();
			if(type==TYPE_UPLOADFILE){
				return loadUploadFile(din,in);
			}
			Assert.equals(type, TYPE_OBJECT);
			@SuppressWarnings("unused")
			long created=din.readLong();
			Object o=IOUtils.loadObject(in);
			return o;
		}finally{
			IOUtils.closeQuietly(in);
		}
		
	}
	
	/**
	 * 删除文件
	 * @param id
	 * @return
	 */
	public boolean delete(String id){
		Assert.notNull(id);
		File file=new File(folder,id);
		if(!file.isFile())return false;
		return file.delete();
	}
	
	/**
	 * 存储文件
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String save(UploadFile file) throws IOException{
		String uuid=StringUtils.generateGuid();
		ByteArrayOutputStream header=new ByteArrayOutputStream();
		DataOutputStream out=new DataOutputStream(header);
		try{
			out.writeByte(TYPE_UPLOADFILE);
			out.writeUTF(file.getPartName());
			out.writeLong(System.currentTimeMillis());//保存时间
			IOUtils.saveAsFile(new File(folder,uuid), new ByteArrayInputStream(header.toByteArray()),IOUtils.getInputStream(file.getTmpFile()));	
		}finally{
			out.close();
		}
		return uuid;
	}
	
	/**
	 * 将实现了Serializable接口的对象保存到本地
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public String saveObject(Object obj) throws IOException{
		String uuid=StringUtils.generateGuid();
		ByteArrayOutputStream header=new ByteArrayOutputStream();
		DataOutputStream out=new DataOutputStream(header);
		out.writeByte(TYPE_OBJECT);
		out.writeLong(System.currentTimeMillis());//保存时间
		
		File output=new File(folder,uuid);
		OutputStream writer=IOUtils.getOutputStream(output);
		writer.write(header.toByteArray());
		try{
			IOUtils.saveObject((Serializable)obj,writer);	
		}finally{
			out.close();
		}
		return uuid;
	}
	
	
	//单元测试
	public static void main(String... args) throws IOException {
		UploadFileManager manager=new UploadFileManager(5000);
		File source1=new File("d:/test.jar");
		File source2=new File("d:/interface-0.1.4-SNAPSHOT-impl.jar");
		UploadFile file1=new UploadFile(source1, "d:/fackpath/test.jar", "file");
		UploadFile file2=new UploadFile(source2, "d:/fackpath/test11.jar", "file");
		String id1=manager.save(file1);
		String id2=manager.save(file2);
		Assert.notNull(id1);
		Assert.notNull(id2);
		
		UploadFile f1=manager.load(id1);
		UploadFile f2=manager.load(id2);
		Assert.notNull(f1);
		Assert.notNull(f2);
		System.out.println(f1.getFileName());
		System.out.println(f2.getFileName());
		
		File loaded1=new File("c:/1.zip");
		File loaded2=new File("c:/2.zip");
		IOUtils.copyFile(f1.getTmpFile(), loaded1);
		IOUtils.copyFile(f2.getTmpFile(), loaded2);
		//比较源文件和从Manager中取出的文件内容是否一致
		Assert.equals(StringUtils.getCRC(IOUtils.getInputStream(source1)), StringUtils.getCRC(IOUtils.getInputStream(loaded1)));
		Assert.equals(StringUtils.getCRC(IOUtils.getInputStream(source2)), StringUtils.getCRC(IOUtils.getInputStream(loaded2)));
		
		//等待6秒后去读取，文件已经过期被删除。
		ThreadUtils.doSleep(10000);
		f1=manager.load(id1);
		f2=manager.load(id2);
		Assert.isNull(f1);
		Assert.isNull(f2);
		
		System.out.println("文件操作案例通过。");
		
		Map<String,String> aa=new HashMap<String,String>();
		aa.put("asa", "sdfdg44");
		aa.put("asa2", "sdfdg44");
		aa.put("asa4", "sdfdg44");
		aa.put("asa5", "sdfdg44");
		String oid=manager.saveObject(aa);
		Map<String,String> bb=(Map<String, String>) manager.loadObject(oid);
		Assert.equals(aa.size(), bb.size());
		System.out.println("可序列化对象操作案例通过。");
	}
	
	final static class RemoveExpireFileThread extends Thread{
		private int expireTime;
		private File folder;
		private int interval;
		public RemoveExpireFileThread(File folder,int interval2) {
			this.expireTime=interval2;
			this.folder=folder;
			this.interval=expireTime/10;
			if(interval<5000)interval=5000;//扫描最小间隔时间不能短于5秒
		}
		@Override
		public void run() {
			LogUtil.info("The RemoveExpireFileThread started." + this.toString());
			ThreadUtils.doSleep(interval);	
			while(true){
				long range=System.currentTimeMillis()-expireTime;
				//根据文件的上次修改时间删除过期文件
				for(File f:IOUtils.listFiles(folder)){
					try{
						if(f.exists() && f.lastModified()<range){
							LogUtil.info("The file " + f.getAbsolutePath() +" delete for it's expired.");
							boolean flag=f.delete();
							if(!flag){
								LogUtil.info("File delete failure:"+ f.getAbsolutePath());
							}
						}
					}catch(Exception e){
						Exceptions.log(e);
					}
					
				}
				ThreadUtils.doSleep(interval);				
			}
		}
	}
}
