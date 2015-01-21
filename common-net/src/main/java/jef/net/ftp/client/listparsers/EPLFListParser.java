package jef.net.ftp.client.listparsers;

import java.util.Date;
import java.util.StringTokenizer;

import jef.common.JefException;
import jef.net.ftp.client.FtpEntry;
import jef.net.ftp.client.FTPListParser;

/**
 * This parser can handle the EPLF format.
 * 
 * @author Carlo Pelliccia
 */
public class EPLFListParser implements FTPListParser {

	public FtpEntry[] parse(String[] lines) throws JefException {
		int size = lines.length;
		FtpEntry[] ret = null;
		for (int i = 0; i < size; i++) {
			String l = lines[i];
			// Validate the plus sign.
			if (l.charAt(0) != '+') {
				throw new JefException();
			}
			// Split the facts from the filename.
			int a = l.indexOf('\t');
			if (a == -1) {
				throw new JefException();
			}
			String facts = l.substring(1, a);
			String name = l.substring(a + 1, l.length());
			// Parse the facts.
			Date md = null;
			boolean dir = false;
			long fileSize = 0;
			StringTokenizer st = new StringTokenizer(facts, ",");
			while (st.hasMoreTokens()) {
				String f = st.nextToken();
				int s = f.length();
				if (s > 0) {
					if (s == 1) {
						if (f.equals("/")) {
							// This is a directory.
							dir = true;
						}
					} else {
						char c = f.charAt(0);
						String value = f.substring(1, s);
						if (c == 's') {
							// Size parameter.
							try {
								fileSize = Long.parseLong(value);
							} catch (Throwable t) {
								;
							}
						} else if (c == 'm') {
							// Modified date.
							try {
								long m = Long.parseLong(value);
								md = new Date(m * 1000);
							} catch (Throwable t) {
								;
							}
						}
					}
				}
			}
			// Create the related FTPFile object.
			if (ret == null) {
				ret = new FtpEntry[size];
			}
			ret[i] = new FtpEntry();
			ret[i].setName(name);
			ret[i].setModifiedDate(md);
			ret[i].setSize(fileSize);
			ret[i].setType(dir ? FtpEntry.TYPE_DIRECTORY : FtpEntry.TYPE_FILE);
		}
		return ret;
	}

	public static void main(String[] args) throws Throwable {
		String[] test = { "+i8388621.29609,m824255902,/,\tdev",
				"+i8388621.44468,m839956783,r,s10376,\tRFCEPLF" };
		EPLFListParser parser = new EPLFListParser();
		FtpEntry[] f = parser.parse(test);
		for (int i = 0; i < f.length; i++) {
			System.out.println(f[i]);
		}
	}
}
