package jef.tools;

import java.io.File;
import java.io.IOException;

import jef.common.log.LogUtil;
import jef.tools.support.ArchiveSummary;
import jef.tools.unrar.ErrorCode;
import jef.tools.unrar.RarArchive;
import jef.tools.unrar.RarFileHeader;
import jef.tools.unrar.RarOpenMode;

public class ZipUtilsEx extends ZipUtils {
	static {
		try {
			ZipUtils.class.getClassLoader().loadClass("jef.tools.unrar.RarArchive");
		} catch (ClassNotFoundException e) {
			Exceptions.log(e);
		}
	}

	/**
	 * 压缩rar格式文件（暂不支持）
	 * 
	 * @param archive
	 * @param source
	 * @throws IOException
	 */
	public static void rar(String archive, String source) throws IOException {
		rar(new File(archive), new File(source));
	}

	/**
	 * 压缩rar格式文件(暂不支持)
	 * 
	 * @param archive
	 * @param source
	 * @throws IOException
	 */
	public static void rar(File archive, File... source) throws IOException {
		throw new UnsupportedOperationException("rar archive creation is supported!");
	}

	/**
	 * 解压rar文件
	 * 
	 * @param file
	 * @param pwd
	 * @return
	 * @throws IOException
	 */
	public static boolean unrar(String file, String dest, String pwd) throws IOException {
		File f = new File(file);
		return unrar(f, dest, pwd);
	}

	/**
	 * 解压rar文件
	 * 
	 * @param file
	 * @param pwd
	 * @return
	 * @throws IOException
	 */
	public static boolean unrar(File file, String dest, String pwd) throws IOException {
		return unrar(file, dest, null, true, pwd);
	}

	/**
	 * 解压rar文件
	 * 
	 * @param file
	 *            压缩包
	 * @param dest
	 *            解压路径
	 * @param cd
	 *            文件名监听器
	 * @param pwd
	 *            密码
	 * @return
	 * @throws IOException
	 */
	public static boolean unrar(File file, String dest, EntryProcessor cd, String... pwd) throws IOException {
		return unrar(file, dest, cd, true, pwd);
	}

	/**
	 * 注意RAR的解压为本地方法，需要确保unrar.dll 、jniunrar.dll在Jre目录或者系统目录下。
	 * 
	 * @param file
	 *            压缩文件
	 * @param despath
	 *            目标路径
	 * @param cd
	 *            字符解码器
	 * @param breakAtFail
	 *            一旦失败就退出
	 * @param pwd
	 *            密码,注意只能设置一个。
	 * @throws IOException
	 * @return 无错完成处理返回true,否则返回false
	 */
	public static boolean unrar(File file, String despath, EntryProcessor cd, boolean breakAtFail, String... pwd)
			throws IOException {
		boolean hasError = false;
		RarArchive rar = new RarArchive(file);
		if (pwd.length > 0)
			rar.setPwd(pwd[0]);
		int flag = rar.openArchive();
		try {
			if (flag != ErrorCode.SUCCESS) {
				throw new IOException(String.format("RAR file open fail!%1$s", flag));
			}
			while (true) {
				RarFileHeader header = rar.readFileHeader();
				if (header == RarFileHeader.EOF)
					break;
				if (header == null) {
					System.out.println("Bad bolck header!");
					break;
				}
				String entryName = header.getFileName();
				if (cd != null) {
					entryName = cd.getExtractName(entryName, header.getPackedSize(), header.getUnpackedSize());
				}
				if (entryName == null)
					continue;
				int r = rar.extractAs(despath + "\\" + entryName);
				if (r != ErrorCode.SUCCESS) {
					hasError = true;
					String errMsg = "Extract Failure at file:" + header.getFileName() + " (" + r + ")";
					if (breakAtFail) {
						throw new IOException(errMsg);
					} else {
						LogUtil.show(errMsg);
					}
				}
			}
		} finally {
			rar.close();
			rar = null;
		}

		return !hasError;
	}

	// 得到RAR摘要信息
	public static ArchiveSummary getRarArchiveSummary(File file, String pwd) throws IOException {
		// long start = System.currentTimeMillis();
		RarArchive rar = new RarArchive(file, RarOpenMode.RAR_OM_LIST);
		if (pwd != null)
			rar.setPwd(pwd);
		int flag = rar.openArchive();
		if (flag != ErrorCode.SUCCESS) {
			throw new IOException(String.format("RAR file open fail!%1$s", flag));
		}
		ArchiveSummary summary = new ArchiveSummary();
		while (true) {
			RarFileHeader header = rar.readFileHeader();
			if (header == RarFileHeader.EOF)
				break;
			if (header == null) {
				throw new IOException("Bad bolck header!");
			}
			summary.addItem(header.getFileName(), header.getPackedSize(), header.getUnpackedSize());
			rar.skip();
		}
		rar.close();
		rar = null;
		// System.out.println("GetRARFileSummaryTimeCost:" +
		// (System.currentTimeMillis() - start));
		return summary;
	}

	// 获取压缩文件摘要信息
	public static ArchiveSummary getArchiveSummary(File file, String pwd) throws IOException {
		String fname = file.getName().toLowerCase();
		if (fname.endsWith(".tar.gz")) {
			return getTarGzSummary(file);
		} else if (fname.endsWith("tar")) {
			return getTarSummary(file);
		} else if (fname.endsWith(".rar")) {
			return getRarArchiveSummary(file, pwd);
		} else {// 一律按照zip格式解压
			return getZipArchiveSummary(file);
		}
	}

	/**
	 * 解压文件，按照扩展名来区分解压方式
	 * 
	 * @param file
	 *            压缩包
	 * @param dest
	 *            解压路径
	 * @param cd
	 *            handler
	 * @param pwd
	 *            密码（仅对rar文件有效）
	 * @return
	 * @throws IOException
	 */
	public static boolean uncompress(File file, String dest, EntryProcessor cd, String... pwd) throws IOException {
		try {
			String fname = file.getName().toLowerCase();
			if (fname.endsWith(".tar.gz")) {
				return unTarGz(file, dest, cd);
			} else if (fname.endsWith(".tar")) {
				return untar(file, dest, cd);
			} else if (fname.endsWith(".rar")) {
				return unrar(file, dest, cd, pwd);
			} else {// 一律按照zip格式解压
				return unzip(file, dest, null, cd);
			}
		} catch (Exception e) {
			Exceptions.log(e);
			return false;
		}
	}
}
