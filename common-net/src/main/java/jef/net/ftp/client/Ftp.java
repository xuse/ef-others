package jef.net.ftp.client;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 操作FTP的抽象接口
 * 
 * <p>重要，兼容性问题备注：
 * <blockquote>
 * 原先的{@link Ftp}是非抽象类，可以直接构造，现在将其改为{@link FtpImpl}。用法和以前完全一样。
 * 将Ftp类改为接口是为了增加SFTP的实现。现在Ftp有两种实现，分别是{@link FtpImpl}和{@link SFtpImpl}。
 * </blockquote>
 * 
 * @author Jiyi
 *
 */
public interface Ftp extends Closeable{

	/**
	 * 连接ftp服务器
	 * 
	 * @param home
	 *            登录后的主目录
	 * @return
	 */
	boolean connect(String home);

	/**
	 * 连接FTP
	 * 
	 * @return
	 */
	boolean connect();

	/**
	 * 断开与ftp服务器连接
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * 返回当前FTP路径
	 * 
	 * @return
	 * @throws IOException
	 */
	String pwd() throws IOException;

	/**
	 * ftp上传 如果服务器段已存在名为filename的文件夹，该文件夹中与要上传的文件夹中同名的文件将被替换
	 * 
	 * @param filename
	 *            要上传的文件（或文件夹）名
	 * @return
	 * @throws IOException
	 */
	void upload(File... files) throws IOException;

	/**
	 * ftp上传 如果服务器段已存在名为newName的文件夹，该文件夹中与要上传的文件夹中同名的文件将被替换
	 * 
	 * @param file
	 *            要上传的文件（或文件夹）
	 * @param newName
	 *            服务器段要生成的文件（或文件夹）名
	 * @return
	 */
	boolean doUpload(File file, String newName) throws IOException;

	/**
	 * upload 上传文件
	 * 
	 * @param file
	 *            要上传的文件名
	 * @param newname
	 *            上传后的新文件名
	 * @return -1 文件不存在 >=0 成功上传，返回文件的大小
	 * @throws IOException
	 */
	long uploadFile(File file, String newname) throws IOException;

	/**
	 * 从ftp下载文件到本地
	 * 
	 * @param filename
	 *            服务器上的文件名
	 * @param newfilename
	 *            本地生成的文件名
	 * @return
	 */
	long download(String filename, File outfile);

	/**
	 * 进入到指定的目录下
	 * 
	 * @param path
	 * @throws IOException
	 */
	boolean cd(String path) throws IOException;

	/**
	 * 取得相对于当前连接目录的某个目录下所有文件列表
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	List<String> dir(String path) throws IOException;

	/**
	 * 取得相对于当前连接目录的某个目录下所有文件列表
	 * 
	 * @param path
	 * @param extnames
	 * @return
	 * @throws IOException
	 */
	List<String> dir(String path, final String... extnames) throws IOException;

	/**
	 * 列出当前目录下的文件项目
	 * 
	 * @return
	 * @throws IOException
	 */
	FtpEntry[] list() throws IOException;

	/**
	 * 将List返回结果不做解析，以String形式返回
	 * 
	 * @return
	 * @throws IOException
	 */
	String listAsString() throws IOException;

	/**
	 * 列出指定路径下的文件
	 * 
	 * @param path
	 * @return 包括所有数据的一长串String
	 * @throws IOException
	 */
	String list(String path) throws IOException;

	/**
	 * 删除文件
	 * 
	 * @param path
	 * @throws IOException
	 */

	void delete(String path) throws IOException;

	/**
	 * 删除目录
	 * 
	 * @param path
	 * @throws IOException
	 */
	void rd(String path) throws IOException;

	/**
	 * 执行指定的FTP命令
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	String execute(String cmd) throws IOException;

	/**
	 * 切换到二进制模式
	 * 
	 * @throws IOException
	 */
	void binary() throws IOException;

	/**
	 * 切换到acsii模式
	 * 
	 * @throws IOException
	 */
	void ascii() throws IOException;

	/**
	 * 在当前目录下创建文件夹
	 * 
	 * @param dir
	 * @return
	 */
	boolean createDir(String dir);

	/**
	 * 返回上一级目录
	 * 
	 * @throws IOException
	 */
	void cdUp() throws IOException;

	/**
	 * 获取当前的目录编码
	 * 
	 * @return
	 */
	String getDefaultEncoding();

	/**
	 * 设置编码，会影响列出文件目录功能
	 * 
	 * @param defaultEncoding
	 */
	void setDefaultEncoding(String defaultEncoding);

	/**
	 * 当前目录下的文件改名
	 * 
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	void rename(String from, String to) throws IOException;

	/**
	 * 返回服务器是否连接
	 * @return
	 */
	boolean isConnected();
	
	/**
	 * 设置调试模式
	 * 需要在连接后设置才会生效
	 * @param debug
	 */
	void setDebug(boolean debug);

}
