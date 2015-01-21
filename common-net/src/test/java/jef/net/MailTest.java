package jef.net;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import jef.http.client.support.FilePart;
import jef.http.client.support.HtmlPart;
import jef.http.client.support.NormalPart;
import jef.http.client.support.Part;
import jef.net.mail.Mail;
import jef.net.mail.Mail.MailId;
import jef.net.mail.Mail.MailSummary;
import jef.net.mail.Pop3Client;
import jef.net.mail.Pop3Client.MailBufferedReader;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

import org.junit.Test;

public class MailTest {

	@Test
	public void testpop3() throws IOException {
		Pop3Client pop3 = new Pop3Client("pop.163mail.com", "my_account",
				"password");
		int n = pop3.getTotal();// 获得待收邮件数
		MailSummary[] mails = pop3.getSummarys();// 得到全部邮件标题大小等信息
		for (MailSummary s : mails) {
			if (s.getFrom().contains("张三")) {
				// pop3.saveMail(s.getId().getNum(), new
				// File("c:/"+s.getSubject()+".eml"));//保存成文件
				Mail mail = pop3.fetch(s.getId().getNum());// 直接收取
				printMail(mail);
			}

		}

	}

	private void printMail(Mail mail) {
		MailSummary s=mail.getSummary();
		System.out.println("Mail:" + s.getSubject() + " from "
				+ s.getFrom() + "   size=" + s.getId().getSize());
		System.out.println("Parts:"+mail.getParts().length);
		for (Part p : mail.getParts()) {
			if (p instanceof FilePart) {
				System.out
						.println("附件：" + ((FilePart) p).getFilename());
			} else if (p instanceof HtmlPart) {
				System.out.println("HTML："
						+ ((HtmlPart) p).getContent());
			} else if (p instanceof NormalPart) {
				System.out.println("Text："
						+ ((NormalPart) p).getContent());
			}
		}
		
	}
	
	@Test
	public void tesFetchMail() throws IOException, AuthenticationException {
		Pop3Client pop3 = new Pop3Client("pop.hikvision.com", "jiyi@hikvision.com", "hik88075998");
		pop3.open();
		try{
			int n = pop3.getTotal();// 获得待收邮件数
			MailSummary[] mails = pop3.getSummarys();// 得到全部邮件标题大小等信息
			int count=0;
			for (MailSummary s : mails) {
				if(s.getFrom().contains("dbranscomb@voltdb.com")){
					System.out.println("Saving Mail:" + s.getSubject() + " from "	+ s.getFrom() + "   size=" + s.getId().getSize());
					Mail mail=pop3.fetch(s.getId().getNum());
					printMail(mail);
					count++;
				}
				
			}
			System.out.println("OK!");		
		}finally{
			pop3.close();
		}
	}
	

	@Test
	public void testSaveMail() throws IOException, AuthenticationException {
		Pop3Client pop3 = new Pop3Client("pop.hikvision.com", "jiyi@hikvision.com", "hik88075998");
		pop3.open();
		try{
			int n = pop3.getTotal();// 获得待收邮件数
			MailSummary[] mails = pop3.getSummarys();// 得到全部邮件标题大小等信息
			int count=0;
			for (MailSummary s : mails) {
				System.out.println("Saving Mail:" + s.getSubject() + " from "	+ s.getFrom() + "   size=" + s.getId().getSize());
				File file=new File("c:/",StringUtils.toFilename(s.getSubject(),"")+".eml");
				pop3.saveMail(s.getId().getNum(),file );
				count++;
				if(count>2)break;
			}
			System.out.println("OK!");		
		}finally{
			pop3.close();
		}
	}
	
	@Test
	public void testLocal() throws IOException{
		Mail mail=Mail.loadMail(new File("c:/Foxmail发出的测试邮件.eml"));
		printMail(mail);
	}
	
	@Test
	public void testProgress() throws AuthenticationException, IOException{
		Pop3Client pop3 = new Pop3Client("pop.hikvision.com", "jiyi@hikvision.com", "hik88075998");
		pop3.open();
		try{
			MailId[] ids=pop3.list();
			for (MailId s : ids) {
				if(s.getSize()>155000){
					MailBufferedReader mm=pop3.fetchEx(s.getNum());
					mm.setTotalSize(s.getSize());
					File file=new File("c:/test0"+s.getNum()+".eml");
					Writer w=IOUtils.getWriter(file, null);
					String str=mm.readLine();
					w.write(str);
					for(str=mm.readLine();!".".equals(str);){
						System.out.println(mm.getPercent());
						w.write("\r\n");
						w.write(str);
						str=mm.readLine();
					}
					IOUtils.closeQuietly(w);
					System.out.println(file+" saved.");
					
				}
			}
			System.out.println("OK!");		
		}finally{
			pop3.close();
		}
	}
}
