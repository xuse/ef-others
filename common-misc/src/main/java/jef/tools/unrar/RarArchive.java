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
package jef.tools.unrar;

import java.io.File;

import jef.tools.Assert;
import jef.tools.IOUtils;
import jef.ui.swing.Swing;

/**
 * surpport native method corresponding to the method provided by unrar.dll.
 * when you use this class you should call the static method
 * {@link #init(String)} first and make sure the unrar.dll is in the place where
 * JVM can find
 * 
 * @author careprad email:careprad@gmail.com
 */
public final class RarArchive {
	static {
		File root = new File(System.getProperty("user.dir"));
		File f = IOUtils.findFile(root, "jniunrar.dll",false);
		if (f != null) {
			System.out.println("Loading lib:"+f.getAbsolutePath());
			System.load(f.getAbsolutePath());
		}else{
			System.loadLibrary("jniunrar");	
		}
	}
	/**
	 * Move to the next file in the archive. If the archive is solid and
	 * RAR_OM_EXTRACT mode was set when the archive was opened, the current file
	 * will be processed - the operation will be performed slower than a simple
	 * seek.
	 */
	public final static short OPER_SKIP = 0;
	/**
	 * Test the current file and move to the next file in the archive. If the
	 * archive was opened with RAR_OM_LIST mode, the operation is equal to
	 * RAR_SKIP.
	 */
	public final static short OPER_TEST = 1;
	/**
	 * Extract the current file and move to the next file. If the archive was
	 * opened with RAR_OM_LIST mode, the operation is equal to RAR_SKIP.
	 */
	public final static short OPER_EXTRACT = 2;
	/**
	 * 分卷提供器
	 */
	private VolumeChangeCallBack volchgcall = DEFAULT_VOLUMNCHANGE;
	/**
	 * 口令生成提供器
	 */
	private NeedPasswordCallBack needPwdcall = DEFAULT_PASS_CALLBACK;
	/**
	 * 内容跟踪
	 */
	private DataProcessCallback dataProcessCallback;
	/**
	 * 通过readHeader得到的当前的Header
	 */
	private RarFileHeader curHeader;
	/**
	 * the handle pointer of the archive file,for translate output data
	 */
	private long arcHandle;
	private RarFileStatus status;

	public RarArchive(File file) {
		this(file, RarOpenMode.RAR_OM_EXTRACT);
	}

	public RarArchive(File arcName, RarOpenMode openMode) {
		if (!arcName.exists())
			throw new IllegalArgumentException("File " + arcName.getAbsolutePath() + " does not exist.");
		this.status = new RarFileStatus(arcName.getAbsolutePath(), openMode);
	}

	public RarArchive(String arcName, RarOpenMode openMode) {
		this.status = new RarFileStatus(arcName, openMode);
	}

	/**
	 * 读取当前的文件头。
	 * 
	 * @return RarFileHeader 返回类型，可能是以下三种情况之一 null: 读取头过程中出错，数据错误
	 *         RarFileHeader.EOF: 文件到结尾，没有新的文件了 RarFileHeader 正常读取
	 * @throws
	 */
	public RarFileHeader readFileHeader() {
		int flag = readHeader();
		if (flag == ErrorCode.SUCCESS)
			return curHeader;
		if (flag == ErrorCode.ERAR_END_ARCHIVE)
			return RarFileHeader.EOF;
		return null;
	}

	public int openArchive() {
		try {
			return open();
		} catch (UnRarException e) {
			return ErrorCode.ERAR_UNKNOWN_FORMAT;
		}
	}

	public int openArchive(DataProcessListener dpCallback) {
		if (dpCallback == null) {
			this.dataProcessCallback = new DataProcessListener(this);
		} else {
			this.dataProcessCallback = dpCallback;
		}
		try {
			return open();
		} catch (UnRarException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * open archive file corresponding to RAROpenArchiveEx method
	 */
	public native int open() throws UnRarException;

	/**
	 * read block header corresponding to RARReadHeaderEx method
	 * 
	 * @return a RarReadHeaderResult value
	 */
	private native int readHeader();

	/**
	 * set the archive's pssword if you don't know whether it has a password,you
	 * should set a NeedPasswordCallBack by use
	 * {@link #setNeedPwdcall(NeedPasswordCallBack)}.If it need a password it
	 * will invoke this callback corresponding to RARSetPassword method
	 * 
	 * @param password
	 */
	private native void setPassword(String password);

	/**
	 * 解压到某个路径下 corresponding to RARProcessFile method
	 * 
	 * @param processMode
	 *            a RarProcessFileMode value
	 * @param desPath
	 *            the destination path to extract
	 * @return a RarProcessFileResult value
	 */
	public int extractTo(String desPath) {
		if (this.getStatus().getOpenMode() != RarOpenMode.RAR_OM_EXTRACT) {
			throw new UnsupportedOperationException("The archive is open in LIST mode, can't extract.");
		}
		return processFile(RarArchive.OPER_EXTRACT, desPath, null);
	}

	/**
	 * 解压为指定的文件名
	 * 
	 * @Title: extractAs
	 * @param destName
	 * @return int 返回类型
	 * @throws
	 */
	public int extractAs(String destName) {
		Assert.notNull(destName);
		if (this.getStatus().getOpenMode() != RarOpenMode.RAR_OM_EXTRACT) {
			throw new UnsupportedOperationException("The archive is open in LIST mode, can't extract.");
		}
		return processFile(RarArchive.OPER_EXTRACT, null, destName);
	}

	/**
	 * 测试文件
	 * 
	 * @Title: testFile
	 * @return int 返回类型
	 * @throws
	 */
	public int testFile() {
		if (this.getStatus().getOpenMode() != RarOpenMode.RAR_OM_EXTRACT) {
			throw new UnsupportedOperationException("The archive is open in LIST mode, can't test.");
		}
		return processFile(RarArchive.OPER_TEST, null, null);
	}

	/**
	 * 掠过当前文件头，进入下一个头的位置
	 */
	public void skip() {
		processFile(RarArchive.OPER_SKIP, null, null);
	}

	private native int processFile(short processMode, String desPath, String destName);

	// 为兼容原因暂不支持
	private native int processFileW(short processMode, String desPath, String destName);

	/**
	 * close file corresponding to RARCloseArchive method
	 */
	public native void close();
	private static final NeedPasswordCallBack DEFAULT_PASS_CALLBACK = new NeedPasswordCallBack() {
		public NeedPwdCallbackResult invoke() {
			String pass = (String) Swing.msgbox("Please input file password:", Swing.INPUT);
			return (pass == null) ? NeedPwdCallbackResult.FALLBACK : new NeedPwdCallbackResult(pass);
		}
	};
	private static final VolumeChangeCallBack DEFAULT_VOLUMNCHANGE = new VolumeChangeCallBack() {
		public VolumnChangeCallbackResult invoke(int type, String data) {
			if (type == RAR_VOL_ASK) {
				String next = (String) Swing.msgbox("Please input(" + data + ")", Swing.INPUT);
				if (next == null)
					return VolumnChangeCallbackResult.FALLBACK;
				return new VolumnChangeCallbackResult(next);
			} else {
				return VolumnChangeCallbackResult.CONTINUE;
			}
		}
	};
	static class DataProcessListener implements DataProcessCallback {
		private RarArchive rar;

		DataProcessListener(RarArchive rar) {
			this.rar = rar;
		}

		public int invoke(int p1, int p2) {
			RarFileHeader header = rar.getCurHeader();
			header.addExtracted(p2);
			System.out.println(header.getFileName() + "  " + header.getBytesExtracted() * 100.0 / header.getUnpackedSize() + "%");
			return 0;
		}
	};

	public RarFileHeader getCurHeader() {
		return this.curHeader;
	}

	/**
	 * set the martipart volumn change event callback
	 * 
	 * @param volchgcall
	 */
	public void setVolchgcall(VolumeChangeCallBack volchgcall) {
		this.volchgcall = volchgcall;
	}

	/**
	 * set the need password event callback
	 * 
	 * @param needPwdcall
	 */
	public void setPasswordCallback(NeedPasswordCallBack needPwdcall) {
		this.needPwdcall = needPwdcall;
	}

	public void setPwd(final String pwd) {
		if (pwd == null)
			return;
		this.needPwdcall = new NeedPasswordCallBack() {
			public NeedPwdCallbackResult invoke() {
				return pwd == null ? NeedPwdCallbackResult.FALLBACK : new NeedPwdCallbackResult(pwd);
			}
		};
	}

	public RarFileStatus getStatus() {
		return this.status;
	}

	public DataProcessCallback getDataProcessCallback() {
		return dataProcessCallback;
	}

	public void setDataProcessCallback(DataProcessCallback dataProcessCallback) {
		this.dataProcessCallback = dataProcessCallback;
	}
}
