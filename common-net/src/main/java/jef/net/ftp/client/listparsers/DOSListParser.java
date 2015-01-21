package jef.net.ftp.client.listparsers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jef.common.JefException;
import jef.net.ftp.client.FtpEntry;
import jef.net.ftp.client.FTPListParser;

/**
 * This parser can handle the MSDOS-style LIST responses.
 * 
 * @author Carlo Pelliccia
 */
public class DOSListParser implements FTPListParser {

	private static final Pattern PATTERN = Pattern
			.compile("^(\\d{2})-(\\d{2})-(\\d{2})\\s+(\\d{2}):(\\d{2})(AM|PM)\\s+"
					+ "(<DIR>|\\d+)\\s+([^\\\\/*?\"<>|]+)$");

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yy hh:mm a");

	public FtpEntry[] parse(String[] lines) throws JefException {
		int size = lines.length;
		FtpEntry[] ret = new FtpEntry[size];
		for (int i = 0; i < size; i++) {
			Matcher m = PATTERN.matcher(lines[i]);
			if (m.matches()) {
				String month = m.group(1);
				String day = m.group(2);
				String year = m.group(3);
				String hour = m.group(4);
				String minute = m.group(5);
				String ampm = m.group(6);
				String dirOrSize = m.group(7);
				String name = m.group(8);
				ret[i] = new FtpEntry();
				ret[i].setName(name);
				if (dirOrSize.equalsIgnoreCase("<DIR>")) {
					ret[i].setType(FtpEntry.TYPE_DIRECTORY);
					ret[i].setSize(0);
				} else {
					long fileSize;
					try {
						fileSize = Long.parseLong(dirOrSize);
					} catch (Throwable t) {
						throw new JefException();
					}
					ret[i].setType(FtpEntry.TYPE_FILE);
					ret[i].setSize(fileSize);
				}
				String mdString = month + "/" + day + "/" + year + " " + hour
						+ ":" + minute + " " + ampm;
				Date md;
				try {
					md = DATE_FORMAT.parse(mdString);
				} catch (ParseException e) {
					throw new JefException();
				}
				ret[i].setModifiedDate(md);
			} else {
				throw new JefException();
			}
		}
		return ret;
	}

}
