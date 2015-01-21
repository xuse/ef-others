
package jef.net.ftp.client.listparsers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import jef.common.JefException;
import jef.net.ftp.client.FtpEntry;
import jef.net.ftp.client.FTPListParser;

/**
 * This parser can handle the standard MLST/MLSD responses (RFC 3659).
 * 
 * @author Carlo Pelliccia
 * @since 1.5
 */
public class MLSDListParser implements FTPListParser {

	/**
	 * Date format 1 for MLSD date facts (supports millis).
	 */
	private static final DateFormat MLSD_DATE_FORMAT_1 = new SimpleDateFormat("yyyyMMddhhmmss.SSS Z");

	/**
	 * Date format 2 for MLSD date facts (doesn't support millis).
	 */
	private static final DateFormat MLSD_DATE_FORMAT_2 = new SimpleDateFormat("yyyyMMddhhmmss Z");

	public FtpEntry[] parse(String[] lines) throws JefException {
		ArrayList<FtpEntry> list = new ArrayList<FtpEntry>();
		for (int i = 0; i < lines.length; i++) {
			FtpEntry file = parseLine(lines[i]);
			if (file != null) {
				list.add(file);
			}
		}
		int size = list.size();
		FtpEntry[] ret = new FtpEntry[size];
		for (int i = 0; i < size; i++) {
			ret[i] = (FtpEntry) list.get(i);
		}
		return ret;
	}

	/**
	 * Parses a line ad a MLSD response element.
	 * 
	 * @param line
	 *            The line.
	 * @return The file, or null if the line has to be ignored.
	 * @throws JefException
	 *             If the line is not a valid MLSD entry.
	 */
	private FtpEntry parseLine(String line) throws JefException {
		// Divides facts and name.
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(line, ";");
		while (st.hasMoreElements()) {
			String aux = st.nextToken().trim();
			if (aux.length() > 0) {
				list.add(aux);
			}
		}
		if (list.size() == 0) {
			throw new JefException();
		}
		// Extracts the file name.
		String name = (String) list.remove(list.size() - 1);
		// Parses the facts.
		Properties facts = new Properties();
		for (Iterator<String> i = list.iterator(); i.hasNext();) {
			String aux = (String) i.next();
			int sep = aux.indexOf('=');
			if (sep == -1) {
				throw new JefException();
			}
			String key = aux.substring(0, sep).trim();
			String value = aux.substring(sep + 1, aux.length()).trim();
			if (key.length() == 0 || value.length() == 0) {
				throw new JefException();
			}
			facts.setProperty(key, value);
		}
		// Type.
		int type;
		String typeString = facts.getProperty("type");
		if (typeString == null) {
			throw new JefException();
		} else if ("file".equalsIgnoreCase(typeString)) {
			type = FtpEntry.TYPE_FILE;
		} else if ("dir".equalsIgnoreCase(typeString)) {
			type = FtpEntry.TYPE_DIRECTORY;
		} else if ("cdir".equalsIgnoreCase(typeString)) {
			// Current directory. Skips...
			return null;
		} else if ("pdir".equalsIgnoreCase(typeString)) {
			// Parent directory. Skips...
			return null;
		} else {
			// Unknown... (link?)... Skips...
			return null;
		}
		// Last modification date.
		Date modifiedDate = null;
		String modifyString = facts.getProperty("modify");
		if (modifyString != null) {
			modifyString += " +0000";
			try {
				modifiedDate = MLSD_DATE_FORMAT_1.parse(modifyString);
			} catch (ParseException e1) {
				try {
					modifiedDate = MLSD_DATE_FORMAT_2.parse(modifyString);
				} catch (ParseException e2) {
					;
				}
			}
		}
		// Size.
		long size = 0;
		String sizeString = facts.getProperty("size");
		if (sizeString != null) {
			try {
				size = Long.parseLong(sizeString);
			} catch (NumberFormatException e) {
				;
			}
			if (size < 0) {
				size = 0;
			}
		}
		// Done!
		FtpEntry ret = new FtpEntry();
		ret.setType(type);
		ret.setModifiedDate(modifiedDate);
		ret.setSize(size);
		ret.setName(name);
		return ret;
	}

}
