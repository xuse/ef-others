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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import jef.common.log.LogUtil;
import jef.net.AuthenticationException;
import jef.net.MessageClient;
import jef.tools.Exceptions;
import jef.tools.StringUtils;
import jef.tools.support.JefBase64;

/**
 * 实现SMTP协议的客户端
 * @author Administrator
 *
 */
public class SmtpClient extends MessageClient {
	private boolean debugMode=false;
	

	/**
	 * 连接服务器
	 * @throws UnknownHostException
	 * @throws AuthenticationException
	 */
	public synchronized void open() throws UnknownHostException,AuthenticationException{
		if (client != null)
			return;
		try {
			super.initConnectObjs();
			sendLine("HELO Jef_Mail_Client");
			processLine();
			if (user != null && password != null) {
				login();
			}
		} catch (IOException e) {
			throw new RuntimeException("Host:" +server +" connect error.",e);
		}
	}
	
	protected void login() throws IOException, AuthenticationException {
		sendLine("AUTH LOGIN");
		processLine();
		sendLine(JefBase64.encode(user.getBytes("ISO-8859-1")));
		processLine();
		sendLine(JefBase64.encode(password.getBytes("ISO-8859-1")));
		String str = processLine();
		if (!str.startsWith("235 ")) {
			throw new AuthenticationException(str);
		}
	}

	/**
	 * 构造
	 * @param server 服务器主机地址或IP
 	 * @param user   登录用户名
	 * @param password 登录密码
	 */
	public SmtpClient(String server, String user, String password) {
		super(server, user, password);
	}
	/**
	 * 描述一个邮件地址
	 * @author Administrator
	 *
	 */
	public static class Address{
		private String addr;
		private String name;
		public Address(String addr,String name){
			this.addr=addr;
			this.name=name;
		}
		public Address(String addr){
			this.addr=addr;
		}
		public String getAddr() {
			return addr;
		}
		public void setAddr(String addr) {
			this.addr = addr;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String toString(){
			if(name==null)return addr;
			return name+"<"+addr+">";
		}
	}

	
	

	/**
	 * 发送复杂格式邮件Message
	 * @param sendTo  发往
	 * @param from    来自
	 * @param message 邮件消息
	 */
	public synchronized boolean sendMail(String[] sendTo, Address from, MailMessage message) {
		checkSend(sendTo);
		tryOpen();
		
		sendLine("MAIL FROM: <" + from.getAddr() + ">");
		assert250(processLine());
		for(String add: sendTo){
			sendLine("RCPT TO: <" + add+">");
			assert250(processLine());
		}
		sendLine("DATA");
		processLine();
		message.writeTo(sockout,from,sendTo);
		// 写尾
		sendLine(".");
		try {
			assert250(processLine());
			return true;
		} catch (Exception e) {
			Exceptions.log(e);
		}finally{
			sendLine("RSET");
			processLine();
		}
		return false;
	}

	private void assert250(String response) {
		if(!response.startsWith("250 ")){
			throw new IllegalStateException("process Failure! The Mail Server says:" + response);
		}
	}

	private void checkSend(String[] sendTo) {
		if(sendTo==null || sendTo.length==0){
			throw new IllegalArgumentException("The list of send to must not be empty!");
		}
	}

	/**
	 * 发送纯文本的简单邮件
	 * @param sendTo  发往
	 * @param from    来自
	 * @param subject 标题
	 * @param message 内容
	 */
	public synchronized boolean sendMail(String[] sendTo, Address from, String subject, String message) {
		checkSend(sendTo);
		tryOpen();
		String sendToStr = "<" + StringUtils.join(sendTo, ">,\n <") + ">";
		sendLine("MAIL FROM: <" + from.getAddr() + ">");
		assert250(processLine());
		for(String add: sendTo){
			sendLine("RCPT TO: <" + add+">");
			assert250(processLine());
		}
		sendLine("DATA");
		processLine();
		// 写头
		sendLine("Subject: " + MIMEUtil.encodeRfc2047(subject, "UTF-8"));
		String userShow=from.getName()==null?"":MIMEUtil.encodeRfc2047(from.getName(),"UTF-8");
		sendLine("From: " +  userShow+"<"+from.getAddr()+">");
		sendLine("To: " + sendToStr);
		sendLine("Cc: ");
		sendLine("Mime-Version: 1.0");
		sendLine(BASE64);
		sendLine("Content-Type: text/plain;charset=\"UTF-8\"\r\n");

		// 写体
		try {
			sendLine(JefBase64.encode(message.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Exceptions.log(e);
		}

		// 写尾
		sendLine(".");
		try {
			String response=processLine();
			if(!response.startsWith("250 ")){
				LogUtil.error("Send Failure! The Mail Server says:" + response);
			}else{
				return true;
			}
		} catch (Exception e) {
			Exceptions.log(e);
		}finally{
			sendLine("RSET");
			processLine();
		}
		return false;
	}

	
	private void tryOpen() {
		try {
			open();
		} catch (Exception e) {
			Exceptions.log(e);
			throw new RuntimeException(e.getCause());
		}	
	}
	
	
	protected int getDefaultPort() {
		return 25;
	}

	
	protected void processServerMsg(String str) {
			if (str.startsWith("334 ")) {
				String code = StringUtils.substringAfter(str, "334");
				LogUtil.show(new String(JefBase64.decodeFast(code)));
			}
	}
	private String processLine() {
		try {
			String str = sockin.readLine();
			if(isDebug())LogUtil.show(str);
			return str;
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	private static final String BASE64 = "Content-Transfer-Encoding: base64";


	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	protected boolean isDebug() {
		return debugMode;
	}
}
/*
 * /** SMTP的连接和收发过程： a.建立TCP连接。
 * b.客户端发送HELO命令以标识发件人自己的身份，然后客户端发送MAIL命令服务器端正希望以OK作为响应，表明准备接收。
 * c.客户端发送RCPT命令，以标识该电子邮件的计划接收人，可以有多个RCPT行 d.协商结束，发送邮件，用命令DATA发送
 * e.以.表示结束输入内容一起发送出去 f.结束此次发送，用QUIT命令退出。 SMTP的基本命令集： HELP 显示服务器支持的命令 常见 HELO
 * MAIL RCPT DATA RSET VRFY EXPN QUIT HELP <topic>".
 * 
 * HELO　向服务器标识用户身份 /EHLO MAIL　初始化邮件传输mail from: <xxx>
 * RCPT　标识单个的邮件接收人；常在MAIL命令后面可有多个rcpt to: <xxx>
 * DATA　在单个或多个RCPT命令后，表示所有的邮件接收人已标识，初始化数据传输，以.结束。 NOOP　　 无操作，服务器应响应OK
 * RSET　重置会话，当前传输被取消 QUIT　　　结束会话 VRFY EXPN
 * 
 * help 214-This is Sendmail version 8.9.3 214-Topics: 214- HELO EHLO MAIL RCPT
 * DATA 214- RSET NOOP QUIT HELP VRFY 214- EXPN VERB ETRN DSN 214-For more info
 * use "HELP <topic>". 214-To report bugs in the implementation send email to
 * 214- sendmail-bugs@sendmail.org. 214-For local information send email to
 * Postmaster at your site. 214 End of HELP info
 * 
 * help helo 214-HELO <hostname> 214- Introduce yourself. 214 End of HELP info
 * 
 * help ehlo 214-EHLO <hostname> 214- Introduce yourself, and request extended
 * SMTP mode. 214-Possible replies include: 214- SEND Send as mail [RFC821] 214-
 * SOML Send as mail or terminal [RFC821] 214- SAML Send as mail and terminal
 * [RFC821] 214- EXPN Expand the mailing list [RFC821] 214- HELP Supply helpful
 * information [RFC821] 214- TURN Turn the operation around [RFC821] 214-
 * 8BITMIME Use 8-bit data [RFC1652] 214- SIZE Message size declaration
 * [RFC1870] 214- VERB Verbose [Allman] 214- ONEX One message transaction only
 * [Allman] 214- CHUNKING Chunking [RFC1830] 214- BINARYMIME Binary MIME
 * [RFC1830] 214- PIPELINING Command Pipelining [RFC1854] 214- DSN Delivery
 * Status Notification [RFC1891] 214- ETRN Remote Message Queue Starting
 * [RFC1985] 214- XUSR Initial (user) submission [Allman] 214 End of HELP info
 * 
 * help mail 214-MAIL FROM: <sender> [ <parameters> ] 214- Specifies the sender.
 * Parameters are ESMTP extensions. 214- See "HELP DSN" for details. 214 End of
 * HELP info
 * 
 * help rcpt 214-RCPT TO: <recipient> [ <parameters> ] 214- Specifies the
 * recipient. Can be used any number of times. 214- Parameters are ESMTP
 * extensions. See "HELP DSN" for details. 214 End of HELP info
 * 
 * help data 214-DATA 214- Following text is collected as the message. 214- End
 * with a single dot. 214 End of HELP info
 * 
 * help rset 214-RSET 214- Resets the system. 214 End of HELP info
 * 
 * help noop help quit help help
 * 
 * help vrfy 214-VRFY <recipient> （注：这里recipient可以是你的username） 214- Verify an
 * address. If you want to see what it aliases 214- to, use EXPN instead. 214
 * End of HELP info
 * 
 * help expn 214-EXPN <recipient> 214- Expand an address. If the address
 * indicates a mailing 214- list, return the contents of that list. 214 End of
 * HELP info
 * 
 * help verb 214-VERB 214- Go into verbose mode. This sends 0xy responses that
 * are 214- not RFC821 standard (but should be) They are recognized 214- by
 * humans and other sendmail implementations. 214 End of HELP info
 * 
 * help etrn 214-ETRN [ <hostname> | @<domain> | #<queuename> ] 214- Run the
 * queue for the specified <hostname>, or 214- all hosts within a given
 * <domain>, or a specially-named 214- <queuename> (implementation-specific).
 * 214 End of HELP info
 * 
 * help dsn 214-MAIL FROM: <sender> [ RET={ FULL | HDRS} ] [ ENVID=<envid> ]
 * 214-RCPT TO: <recipient> [ NOTIFY={NEVER,SUCCESS,FAILURE,DELAY} ] 214- [
 * ORCPT=<recipient> ] 214- SMTP Delivery Status Notifications.
 * 214-Descriptions: 214- RET Return either the full message or only headers.
 * 214- ENVID Sender's "envelope identifier" for tracking. 214- NOTIFY When to
 * send a DSN. Multiple options are OK, comma- 214- delimited. NEVER must appear
 * by itself. 214- ORCPT Original recipient. 214 End of HELP info 0 0 0
 * (请您对文章做出评价)
 */
