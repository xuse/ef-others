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

import java.util.Calendar;
import java.util.Date;

/**
 * the rar file entry header infomation
 * 
 * @author careprad
 * 
 */
public class RarFileHeader {
	public static final RarFileHeader EOF=new RarFileHeader();
	private String fileName; // 文件名
	private String comment; // 备注
	private short flags; // 标志
	private long bytesExtracted=0;
	
	
	public void addExtracted(int num){
		bytesExtracted+=num;
	}
	// Output parameter which contains file flags:
	// 0x01 File continued from previous volume
	// 0x02 File continued on next volume
	// 0x04 File encrypted with password
	// 0x08 File comment present
	// 0x10 Previous files data is used (solid flag)
	// Bits 7 6 5 (0xE0 mask)
	//
	// 0 0 0 (0x00) Dictionary size 64 KB
	// 0 0 1 (0x20) Dictionary size 128 KB
	// 0 1 0 (0x40) Dictionary size 256 KB
	// 0 1 1 (0x60) Dictionary size 512 KB
	// 1 0 0 (0x80) Dictionary size 1024 KB
	// 1 0 1 (0xA0) Dictionary size 2048 KB
	// 1 1 0 (0xA0) Dictionary size 4096 KB
	// 1 1 1 (0xE0) Directory record
	// Other bits are reserved.

	private long unpackedSize = 0; // 压缩前大小
	private long packedSize = 0; // 压缩后大小
	private Date fileTime; // 文件时间
	private int fileAttributes = 0; // 文件属性，具体含义不明
	private long fileCRC = 0; // 文件CRC码
	private int hostOS = 0; // 压缩操作系统 0 - MS DOS 1 - OS/2 2 - Win32 3 - Unix 4 -
							// Mac OS
	private short versionToUnpack = 0;// 解压所需版本号(*10)
	private short method = 0; // 压缩方式 0x30 - 存储 0x31 - 最快压缩 0x32 - 快速压缩 0x33 -
								// 标准压缩 0x34 - 较好压缩 0x35 - 最好压缩

	private RarFileHeader(){};
	private RarFileHeader(String argfileName, String argComment, long argpackedSize, long argunpackedSize, long argfileTime, long crc, short flags, short argfileAttributes, short hostOs, short unpVer, short method) {
		this.fileName = argfileName;
		this.packedSize = argpackedSize;
		this.unpackedSize = argunpackedSize;
		this.comment = argComment;
		this.fileTime = fromDosTime(argfileTime);
		this.fileAttributes = argfileAttributes;
		this.fileCRC = crc;
		this.flags = flags;
		this.hostOS = hostOs;
		this.versionToUnpack = unpVer;
		this.method = method;
	}

	/**
	 * the attributes of the entry file
	 */
	public int getFileAttributes() {
		return fileAttributes;
	}

	/**
	 * the name of the entry file
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * the packed size of the entry file
	 */
	public long getPackedSize() {
		return packedSize;
	}

	/**
	 * the unpacked size of the entry file
	 * @return the unpacked size of the entry file
	 */
	public long getUnpackedSize() {
		return unpackedSize;
	}

	/**
	 * the time of the entry file
	 */
	public Date getFileTime() {
		return fileTime;
	}

	/**
	 * the comment of the entry
	 * 
	 * @return string
	 */
	public String getComment() {
		return comment;
	}

	public int getFlags() {
		return flags;
	}

	public long getFileCRC() {
		return fileCRC;
	}

	/**
	 * is the entry file a directory
	 * 
	 * @return true or false
	 */
	public boolean isDirectory() {
		return (flags & 0xE0) == 0xE0; // flags中dir标志
	}

	public int getHostOS() {
		return hostOS;
	}

	public short getVersionToUnpack() {
		return versionToUnpack;
	}

	public short getMethod() {
		return method;
	}

	private static Date fromDosTime(long dostime) {
		int hiWord = (int) ((dostime & 0xFFFF0000) >>> 16);
		int loWord = (int) (dostime & 0xFFFF);

		Calendar date = Calendar.getInstance();
		int year = ((hiWord & 0xFE00) >>> 9) + 1980;
		int month = (hiWord & 0x01E0) >>> 5;
		int day = hiWord & 0x1F;
		int hour = (loWord & 0xF800) >>> 11;
		int minute = (loWord & 0x07E0) >>> 5;
		int second = (loWord & 0x1F) << 1;
		date.set(year, month - 1, day, hour, minute, second);
		return date.getTime();
	}
	public long getBytesExtracted() {
		return bytesExtracted;
	}
}
