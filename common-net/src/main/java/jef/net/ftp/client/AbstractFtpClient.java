package jef.net.ftp.client;

import java.io.IOException;

public abstract class AbstractFtpClient {
	protected abstract FtpEntry[] getEntries() throws IOException;
}
