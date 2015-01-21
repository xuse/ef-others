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
package jef.net.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jef.common.log.LogUtil;
import jef.jre5support.Headers;
import jef.net.AuthenticationException;
import jef.net.MessageClient;
import jef.net.mail.Mail.MailId;
import jef.net.mail.Mail.MailSummary;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.io.ReaderInputStream;

/**
 * POP3简介： 在POP3协议中有三种状态，认可状态，处理状态，和更新状态。
 * 当客户机与服务器建立联系时，一旦客户机提供了自己身份并成功确认，即由认可状态转入处理状态，
 * 在完成相应的操作后客户机发出quit命令，则进入更新状态，更新之后最后重返认可状态。 POP3基本命令集： <li>USER　username 登录时使用
 * </li> <li>PASS　password 登录时使用</li> <li>STAT　 请求服务器发回关于邮箱的统计资料，如邮件总数和总字节数</li>
 * <li>LIST 返回邮件数量和每个邮件的大小</li> <li>LIST [Msg#] 处理 返回邮件数量和每个邮件的大小</li> <li>RETR
 * [Msg#] 处理 返回由参数标识的邮件的全部文本</li> <li>LAST 是查看被取走的邮件的最高序数。</li> <li>DELE [Msg#]
 * 服务器将由参数标识的邮件标记为删除，由quit命令执行</li> <li>RSET 服务器将重置所有标记为删除的邮件，用于撤消DELE命令</li>
 * <li>NOOP 服务器返回一个肯定的响应，防止SOCKET连接中断</li> <li>QUIT　更新</li> <li>TOP n m //返回行数，
 * n表示消息编号，m表示返回正文的行数，可以设置为0，则只返回邮件头.</li> <li>UIDL [Msg#] 处理
 * 返回邮件的唯一标识符，POP3会话的每个标识符都将是唯一的</li> <li>APOP NameDigest 认可 Digest是MD5消息摘要</li>
 * <p>
 * 用法举例 <code><pre>
 * Pop3Client pop3 = new Pop3Client("pop.163mail.com", "my_account","password");
 * int n = pop3.getTotal();// 获得待收邮件数
 * MailSummary[] mails = pop3.getSummarys();// 得到全部邮件标题大小等信息
 * for (MailSummary s : mails) {
 * 	System.out.println("Mail:" + s.getSubject()+" from "+s.getFrom()+"   size="+s.getId().getSize());
 *   if(s.getFrom().contains("张三")){
 * 		// pop3.saveMail(s.getId().getNum(), new File("c:/"+s.getSubject()+".eml"));//保存成文件
 * 		Mail mail=pop3.fetch(s.getId().getNum());//直接收取
 * 		for(Part p:mail.getParts()){
 * 			if(p instanceof FilePart){
 * 				System.out.println("附件："+((FilePart)p).getFilename());
 * 			}else if(p instanceof HtmlPart){
 * 				System.out.println("HTML："+((HtmlPart)p).getContent());
 * 			}else if(p instanceof NormalPart){
 * 				System.out.println("Text："+((NormalPart)p).getContent());
 * 			}
 * 		}
 * 	
 * 	}
 * </pre></code>
 */
public class Pop3Client extends MessageClient {

	public static final String STATUS_OK = "+OK";

	public Pop3Client(String server, String user, String password) {
		super(server, user, password);
	}

	/**
	 * 连接服务器
	 * 
	 * @throws UnknownHostException
	 * @throws AuthenticationException
	 */
	public synchronized void open() throws UnknownHostException,
			AuthenticationException {
		if (client != null)
			return;
		try {
			super.initConnectObjs();
			if (user != null && password != null) {
				login();
			}
		} catch (IOException e) {
			LogUtil.exception(e);
		}
	}

	/**
	 * 得到待收邮件的数量
	 * 
	 * @return
	 */
	public int getTotal() {
		tryOpen();
		sendLine("STAT");
		String reply[] = getResponse();
		if (STATUS_OK.equals(reply[0])) {
			return StringUtils.toInt(reply[1], 0);
		} else {
			throw new RuntimeException(StringUtils.join(reply));
		}
	}

	/**
	 * 列出所有邮件的大小
	 * 
	 * @return
	 * @throws IOException
	 */
	public MailId[] list() throws IOException {
		tryOpen();
		sendLine("LIST");
		this.assertResponse();
		List<MailId> list = new ArrayList<MailId>();
		while (true) {
			String str = sockin.readLine();
			if (str.equals("."))
				break;
			String[] args = str.split(" ");
			MailId mid = new MailId();
			mid.setNum(Integer.valueOf(args[0]));
			mid.setSize(StringUtils.toInt(args[1], -1));
			list.add(mid);
		}
		return list.toArray(new MailId[0]);
	}

	/**
	 * 获取全部的摘要信息
	 * 
	 * @return
	 * @throws IOException
	 */
	public MailSummary[] getSummarys() throws IOException {
		tryOpen();
		int count = getTotal();
		MailSummary[] array = new MailSummary[count];
		for (int i = 0; i < count; i++) {
			array[i] = getSummary(i + 1);
		}
		return array;
	}

	/**
	 * 获取指定的摘要信息
	 * 
	 * @param i
	 * @return
	 * @throws IOException
	 */
	public MailSummary getSummary(int i) throws IOException {
		tryOpen();
		sendLine("LIST " + i);
		String reply[] = assertResponse();
		MailId mid = new MailId();
		mid.setNum(Integer.valueOf(reply[1]));
		mid.setSize(Integer.valueOf(reply[2]));
		// getResponse();
		sendLine("TOP " + i + " 0");
		assertResponse();
		Headers map = getMailHeaders(sockin,false);
		MailSummary s = new MailSummary();
		s.id = mid;
		s.setHeaders(map);
		return s;
	}

	/**
	 * 获取指定的邮件
	 * 
	 * @param id
	 *            ,从1 到 邮件总数。
	 * @return
	 * @throws IOException
	 */
	public Mail fetch(int i) throws IOException {
		tryOpen();
		sendLine("RETR " + i);
		String reply[] = assertResponse();
		MailId id = new MailId();
		id.setNum(i);
		if(reply.length>1){
			id.setSize(StringUtils.toInt(reply[1], -1));
		}
		Headers headers = getMailHeaders(sockin,true);
		Mail m = Mail.createMail(headers, new ReaderInputStream(getMailReader()));
		MailSummary s = new MailSummary();
		s.id = id;
		s.setHeaders(headers);
		m.setSummary(s);
		return m;
	}
	
	/**
	 * 获得一个Fetch的句柄，该句柄在获得后立刻处理，不能插入执行任何指令。
	 * 使用该句柄解析可以获得下载进度
	 * @param i
	 * @return
	 */
	public MailBufferedReader fetchEx(int i){
		tryOpen();
		sendLine("RETR " + i);
		String reply[] = assertResponse();
		int size=0;
		if(reply.length>1)
			StringUtils.toInt(reply[1], -1);//获得邮件大小
		MailBufferedReader reader=new MailBufferedReader(sockin,size);
		return reader;
	}


	/**
	 * 删除指定的邮件
	 * 
	 * @param i
	 *            从1 到邮件总数
	 */
	public void delete(int i) {
		tryOpen();
		sendLine("DELE " + i);
		assertResponse();
	}

	/**
	 * 重置删除标记
	 */
	public void resetDeleteFlag() {
		tryOpen();
		sendLine("RSET");
		assertResponse();
	}

	/**
	 * 将指定的邮件保存到文件
	 * 
	 * @param i
	 * @param file
	 * @throws IOException
	 */
	public File saveMail(int i, File file) throws IOException {
		tryOpen();
		sendLine("RETR " + i);
		assertResponse();
		if (file.exists()) {
			file = IOUtils.escapeExistFile(file);
		}
		Writer w=IOUtils.getWriter(file, null);
		String s=sockin.readLine();
		w.write(s);
		for(s=sockin.readLine();!".".equals(s);){
			w.write("\r\n");
			w.write(s);
			s=sockin.readLine();
		}
		IOUtils.closeQuietly(w);
		return file;
	}
	

	protected void login() throws IOException, AuthenticationException {
		tryOpen();
		sendLine("USER " + user);
		assertResponse();
		sendLine("PASS " + password);
		try {
			assertResponse();
		} catch (RuntimeException e) {
			this.close();
			throw new AuthenticationException(e.getMessage());
		}
	}

	protected int getDefaultPort() {
		return 110;
	}

	protected void processServerMsg(String str) {
		// do nothing
	}

	// 断言确定返回的状态是成功
	private String[] assertResponse() {
		String reply[] = getResponse();
		if (!STATUS_OK.equals(reply[0])) {
			throw new RuntimeException(StringUtils.join(reply, " "));
		}
		return reply;
	}

	public static class MailBufferedReader{
		private BufferedReader reader;
		private int size,current;
		private boolean EOF=false;
		MailBufferedReader(BufferedReader in,int size) {
			this.reader=in;
			this.size=size;
		}

		
		public void setTotalSize(int size) {
			this.size = size;
		}


		/**
		 * 得到邮件总长度
		 * @return
		 */
		public int getTotalSize() {
			return size;
		}

		/**
		 * 得到邮件当前长度
		 */
		public int getCurrent() {
			return current;
		}
		/**
		 * 得到百分比进度
		 * @return
		 */
		public String getPercent(){
			return StringUtils.toPercent(current, size);
		}
		
		
		/**
		 * 读一行
		 * @return
		 * @throws IOException
		 */
		public String readLine() throws IOException {
			if(EOF)return null;
			String line=reader.readLine();
			if(".".equals(line)){
				EOF=true;
			}
			current+=line.length();
			return line;
		}
	}
	
	// 获取邮件文本
	private Reader getMailReader() throws IOException {
		return new FilterReader(sockin) {
			private boolean CR = true;
			private boolean EOF = false;
			@Override
			public void close() throws IOException {
			}
			@Override
			public int read() throws IOException {
				if (EOF)
					return -1;
				int c = super.read();
				if (c == '\n' || c == '\r') {
					CR = true;
				} else if (CR && c == '.') {// 新行的第一个字符
					int nextChar = super.read();
					if (nextChar == '\r') {
						super.read();// 读取掉最后的\n。
						EOF = true;
						return -1;
					} else if (nextChar == '\n') {
						EOF = true;
						return -1;
					}
					CR = false;
				} else {
					CR = false;
				}
				return c;
			}

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				int i = off;
				for (; i < len; i++) {
					int c = read();
					if (c == -1) {
						if (i == off)
							return -1;
						break;

					}
					cbuf[i] = (char) c;
				}
				return i - off;

			}

		};
	}

	// 获取邮件头
	private static Headers getMailHeaders(BufferedReader in,boolean autoStop) throws IOException {
		Headers map = new Headers();
		String key = null;
		while (true) {
			String str = in.readLine();
			if (str.equals(".")) {
				break;
			} else if (str.equals("") && autoStop) {
				break;
			} else if (str.startsWith(" ") || str.startsWith("\t")
					|| str.indexOf(":") == -1) {
				if (key == null)
					continue;
				String lastValue = map.getFirst(key);
				lastValue = lastValue + str;
				map.put(key, new String[]{lastValue});
			} else {
				key = StringUtils.substringBefore(str, ":").toLowerCase();
				String value = StringUtils.substringAfter(str, ":").trim();
				map.add(key, value);
			}
		}
		return map;
	}

	// //测试代码
	// public static void main(String... string) throws IOException,
	// Base64DecodingException {
	// Pop3Client client = new Pop3Client("pop.asiainfo.com", "jiyi",
	// "abc_123");
	// Mail mail = client.fetch(1);
	// System.out.println(mail.getSummary().getSubject());
	// for (Part p : mail.getParts()) {
	// if (p instanceof NormalPart) {
	// LogUtil.show(((NormalPart) p).getContent());
	// } else if (p instanceof FilePart) {
	// FilePart part = (FilePart) p;
	// LogUtil.show(part.getFilename());
	// ((FilePart) p).copyTo(new File("c:/" + part.getFilename()));
	// }
	// }
	// client.close();
	// }

	private String[] getResponse() {
		try {
			String str = sockin.readLine();
			if(debugMode) System.out.println(str);
			return str.split(" ");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void tryOpen() {
		try {
			open();
		} catch (Exception e) {
			LogUtil.exception(e);
			throw new RuntimeException(e.getCause());
		}
	}

	private boolean debugMode = true;

	@Override
	protected boolean isDebug() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * 保存并删除所有邮件，采用先取所有邮件头，然后优先收取小邮件的策略，大邮件放在最后收取
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void saveAndDeleteAll(File file) throws IOException {
		if (!file.exists())
			file.mkdirs();
		System.out.println("Gettiing Summary...");
		MailSummary[] ss = getSummarys();
		Arrays.sort(ss, new Comparator<MailSummary>() {
			public int compare(MailSummary o1, MailSummary o2) {
				Integer s1 = o1.getId().getSize();
				Integer s2 = o2.getId().getSize();
				return s1.compareTo(s2);
			}
		});
		for (MailSummary summary : ss) {
			int id = summary.getId().getNum();
			int size = summary.getId().getSize();
			System.out
					.println("(" + id + "/" + ss.length + " "
							+ StringUtils.formatSize(size) + ")"
							+ summary.getSubject());
			this.saveMail(id, new File(file, id + ".eml"));
			this.delete(id);
		}
	}
}
