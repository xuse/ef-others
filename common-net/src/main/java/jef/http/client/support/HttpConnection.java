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
package jef.http.client.support;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import jef.common.wrapper.Holder;
import jef.http.client.BlockSession;
import jef.http.client.ConnectOptions;
import jef.http.client.ConnectSession;
import jef.http.client.HostManager;
import jef.http.client.HostProfile;
import jef.tools.StringUtils;

public abstract class HttpConnection  implements Closeable{
	
	public abstract HostProfile getHostProfile();
	public abstract boolean canRetry();
	public abstract boolean isConnected();
	public abstract URL getURL();
	public abstract ConnectOptions getOption();
	public abstract void addRetry();
	public abstract boolean reconnect(long curPos, long endPos, BlockSession session);
	public abstract boolean supportBreakPoint();
	public abstract long getTransferPos();
	public abstract void checkRange(int id, long curPos, long endPos, ConnectSession session);
	public abstract String getFilenameFromServer() throws IOException;
	
	public static final HttpConnection createConnection(URL url, ConnectOptions options, boolean doMethodNow) throws IOException {
		HostProfile profile=HostManager.getHost(url.getHost());
		URLConnection con = HttpConnectionImpl.generateUrlCon(url, options,profile);
		HttpConnection myCon = new HttpConnectionImpl(con, false, options,profile);
		if (doMethodNow) {
			myCon.connect();
		}
		return myCon;
	}
	
	public abstract HttpStatus connect() throws IOException ;
	public abstract void setRequestRangeAndConnect(long curPos, long endPos, BlockSession session) throws IOException;
	public abstract int getContentLength();
	public abstract String getContentType();
	public abstract String getHeaderField(String string);
	public abstract Map<String, List<String>> getHeaderFields();
	public abstract InputStream getDownloadStream() throws IOException;
	/**
	 * 获取文件长度，只请求文件的第一个字节，从而在最小的数据量传输情况下获取文件长度
	 */
	public static final int getLengthOnly(URL url, ConnectOptions options,Holder<HttpConnection> reuseConn) throws IOException {
		boolean bak = options.isKeepSessionOnServer();
		options.setKeepSessionOnServer(false);
		HttpConnection con = HttpConnection.createConnection(url, options, false);
		if(con.supportBreakPoint()){
			con.setRequestRangeAndConnect(0, 1, null);
			String range = con.getHeaderField("Content-Range");
			if (range == null)
				return -1;
			String length = StringUtils.substringAfterLast(range, "/");
			con.close();
			options.setKeepSessionOnServer(bak);
			return StringUtils.toInt(length, -1);
		}else{
			con.connect();
			if(reuseConn!=null){
				reuseConn.set(con);	
			}else{
				con.close();
			}
			return con.getContentLength();
		}
	}
}
