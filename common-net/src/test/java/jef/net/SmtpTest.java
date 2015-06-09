package jef.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import jef.http.client.support.FilePart;
import jef.http.client.support.HtmlPart;
import jef.http.client.support.NormalPart;
import jef.net.mail.MailMessage;
import jef.net.mail.SmtpClient;
import jef.net.mail.SmtpClient.Address;
import jef.tools.IOUtils;

import org.junit.Ignore;
import org.junit.Test;

public class SmtpTest {
	/**
	 * 发送普通的文本邮件
	 */
	@Test
	@Ignore
	public void test(){
		String user="testasiainfo@163.com";
		String pass="testtest";
		SmtpClient client=new SmtpClient("smtp.163.com",user,pass);
//		client.setDebugMode(true);
		
		String[] sendTo=new String[]{"jiyi@asiainfo-linkage.com","mr.jiyi@gmail.com"};
		Address from=new Address("testasiainfo@163.com","季怡");
		client.sendMail(sendTo, from, "测试邮件标题123","实打实敖德sd,萨此站起来了！");
		client.close();
	}
	
	/**
	 * 发送带HTML、附件等复杂内容的邮件
	 * @throws IOException
	 */
	@Test
	@Ignore
	public void testAttachment() throws IOException{
		String host="smtp.163.com";
		String user="testasiainfo@163.com";
		String pass="testtest";
		
		String[] sendTo=new String[]{"jiyi@hikvision.com","mr.jiyi@gmail.com"};
		Address from=new Address("jiyi@hikvision.com","季怡");
		
		MailMessage message=new MailMessage("这是一个带附件的HTML邮件");
		message.addPart(new NormalPart("text/plain","的说法经适房 纯文本的正文内容"));
		message.addPart(new HtmlPart("<html><body><H1>HTML的正文内容</H1></body></html>"));
		message.addPart(new FilePart(null, new File("E:/个人工作资料/分版本项目升级和代码移植.doc")));
		SmtpClient client=null;
		try{
			client=new SmtpClient(host,user,pass);
			//client.setDebugMode(true);
			client.sendMail(sendTo, from, message);	
		}finally{
			if(client!=null)client.close();	
		}
	}
	
	/**
	 * 生成邮件，但不发送而是保存到本地
	 * @throws IOException
	 */
	@Test
	public void testMessageSave() throws IOException{
		MailMessage message=new MailMessage("这是一个带附件的HTML邮件");
		//添加文本内容
		message.addPart(new NormalPart("text/plain","的说法经适房 纯文本的正文内容"));
		//添加HTML内容
		message.addPart(new HtmlPart("<html><body><H1>HTML的正文内容</H1></body></html>"));
		//添加附件
		message.addPart(new FilePart(null, new File("E:/个人工作资料/分版本项目升级和代码移植.doc")));
		
		PrintWriter pw=new PrintWriter(new FileOutputStream(new File("c:/testemail.eml")));
		
		String[] sendTo=new String[]{"jiyi@hikvision.com.cn","mr.jiyi@gmail.com"};
		Address from=new Address("jiyi@hikvision.com.cn","季怡");
		
		message.writeTo(pw, from, sendTo);
		IOUtils.closeQuietly(pw);
	}
	

	@Test
	public void mailTest() throws AuthenticationException, IOException {
//		Socket client = new Socket("mail.hikvision.com", 25);
		
		Socket  client= new Socket("hikml.hikvision.com.cn", 25);

//		SmtpClient client=new SmtpClient("10.1.7.150", "jiyi", "Abc111");
//		client.setDebugMode(true);
//		client.open();
//		client.close();
	}
}
