package jef.net.ftp.client;

import java.util.Date;

import jef.tools.DateUtils;

/**
 * 描述一个FTP端的文件对象
 * 
 * @author Administrator
 * 
 */
public class FtpEntry {

	/**
	 * The value for the type "file".
	 */
	public static final int TYPE_FILE = 0;

	/**
	 * The value for the type "directory".
	 */
	public static final int TYPE_DIRECTORY = 1;

	/**
	 * The value for the type "link".
	 */
	public static final int TYPE_LINK = 2;

	/**
	 * The name of the file.
	 */
	private String name = null;

	/**
	 * The path of the linked file, if this one is a link.
	 */
	private String link = null;

	/**
	 * The last modified date of the file.
	 */
	private Date modifiedDate = null;

	/**
	 * The size of the file (bytes).
	 */
	private long size = -1;

	/**
	 * The type of the entry represented. It must be {@link FtpEntry#TYPE_FILE},
	 * {@link FtpEntry#TYPE_DIRECTORY} or {@link FtpEntry#TYPE_LINK}.
	 */
	private int type;

	/**
	 * Returns the last modified date of the file. Pay attention: it could be
	 * null if the information is not supplied by the server.
	 * 
	 * @return The last modified date of the file, or null if the information is
	 *         not supplied.
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * Sets the last modified date of the file.
	 * 
	 * @param modifiedDate
	 *            The last modified date of the file.
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * Returns the name of the file.
	 * 
	 * @return The name of the file.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the file.
	 * 
	 * @param name
	 *            The name of the file.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the type of the entry represented. It must be
	 * {@link FtpEntry#TYPE_FILE}, {@link FtpEntry#TYPE_DIRECTORY} or
	 * {@link FtpEntry#TYPE_LINK}.
	 * 
	 * @return The type of the entry represented. It must be
	 *         {@link FtpEntry#TYPE_FILE}, {@link FtpEntry#TYPE_DIRECTORY} or
	 *         {@link FtpEntry#TYPE_LINK}.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type of the entry represented. It can be
	 * {@link FtpEntry#TYPE_FILE}, {@link FtpEntry#TYPE_DIRECTORY} or
	 * {@link FtpEntry#TYPE_LINK}.
	 * 
	 * @param type
	 *            The type of the entry represented. It can be
	 *            {@link FtpEntry#TYPE_FILE}, {@link FtpEntry#TYPE_DIRECTORY} or
	 *            {@link FtpEntry#TYPE_LINK}.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Returns the size of the file (bytes). A negative value is returned if the
	 * information is not available.
	 * 
	 * @return The size of the file (bytes). A negative value is returned if the
	 *         information is not available.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Sets the size of the file (bytes).
	 * 
	 * @param size
	 *            The size of the file (bytes).
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * This method returns the path of the linked file, if this one is a link.
	 * If this is not a link, or if the information is not available, it returns
	 * null.
	 * 
	 * @return The path of the linked file, if this one is a link. If this is
	 *         not a link, or if the information is not available, it returns
	 *         null.
	 */
	public String getLink() {
		return link;
	}

	/**
	 * This method sets the path of the linked file, if this one is a link.
	 * 
	 * @param link
	 *            The path of the linked file, if this one is a link.
	 */
	public void setLink(String link) {
		this.link = link;
	}

	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(name);
		buffer.append("\t");
		if (type == TYPE_FILE) {
			buffer.append("FILE");
		} else if (type == TYPE_DIRECTORY) {
			buffer.append("DIR");
		} else if (type == TYPE_LINK) {
			buffer.append("LINK");
			buffer.append("link=");
			buffer.append(link);
		} else {
			buffer.append("UNKNOWN");
		}
		
		buffer.append("\t");
		buffer.append(size).append("\t");
		buffer.append(DateUtils.formatDateTime(modifiedDate));
		return buffer.toString();
	}
}
