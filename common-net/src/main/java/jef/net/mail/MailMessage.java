package jef.net.mail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jef.common.MimeTypes;
import jef.http.client.support.FilePart;
import jef.http.client.support.NormalPart;
import jef.http.client.support.Part;
import jef.net.mail.SmtpClient.Address;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.support.JefBase64;

/**
 * 描述一个MIME邮件消息
 */
public class MailMessage {
	private static final String BASE64 = "Content-Transfer-Encoding: base64";
	private static final String BOUNDARY = "------------------7d71b526e00e4";
	
	//文件部头：文件附件多以二进制，用base64编码
	private static final String HEAD_FILE_ATTACHMENT   = "--------------------7d71b526e00e4\r\n" + 
			"Content-Disposition: attachment; filename=\"%s\"\r\n" + 
			BASE64 + "\r\nContent-Type: application/octet-stream\r\n\r\n"; // 每个文件部分的开头
	//HTML附件：一般为图片什么的
	private static final String HEAD_HTML_RESOURCE   = "--------------------7d71b526e00e4\r\n" + 
			"Content-Type: %s\r\n" +			
			BASE64 + "\r\nContent-Location: %s\r\n\r\n"; // 每个文件部分的开头
	//普通数据。需要2个参数。mime-type 转换编码
	private static final String HEAD_NORMAL = "--------------------7d71b526e00e4\r\n" +
			"Content-Type: %s\r\n\t;charset=\"utf-8\"\r\nContent-Transfer-Encoding: %s"+
			"\r\n\r\n";// 每个普通数据段开头

	private static final String REQUEST_TAIL= "--------------------7d71b526e00e4--\r\n";// 结尾

	
	protected List<Part> parts = new ArrayList<Part>();
	private String subject;
	/**
	 * 在邮件消息中添加一个部分 
	 * @param part
	 */
	public void addPart(Part part) {
		parts.add(part);
	}
	/**
	 * 获得目前邮件消息的各个部分
	 * @return
	 */
	public List<Part> getParts() {
		return parts;
	}
	/**
	 * 返回消息的长度
	 * @return
	 */
	public long getLength() {
		long n = 0;
		for (Part p : parts) {
			n += p.getLength();
		}
		return n;
	}

	/**
	 * 构造一个邮件消息
	 * @param subject 邮件标题
	 */
	public MailMessage(String subject) {
		this.subject = subject;
	}
	/**
	 * 返回标题
	 * @return
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * 设置标题
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * 将邮件内容写入到指定的输出流
	 * @param writer 输出流
	 * @param from   发件人信息
	 * @param sendTo 收件人信息
	 */
	public void writeTo(PrintWriter writer,Address from,String[] sendTo){
		// 写头
		writer.println("Subject: " + MIMEUtil.encodeRfc2047(this.subject, "UTF-8"));
		if(from!=null){
			String userShow=from.getName()==null?"":MIMEUtil.encodeRfc2047(from.getName(),"UTF-8");
			writer.println("From: " +  userShow+"<"+from.getAddr()+">");
		}
		if(sendTo!=null){
			String sendToStr = "<" + StringUtils.join(sendTo, ">,\n <") + ">";	
			writer.println("To: " + sendToStr);
		}
		writer.println("Cc: ");
		writer.println("Mime-Version: 1.0");
		writer.println(String.format("Content-Type: multipart/mixed;\n\tboundary=\"%s\"", BOUNDARY));
		writer.println(BASE64);
		writer.println("");
		writer.println("This is a multi-part message in MIME format.");
		writer.println("");
		writer.println(BOUNDARY);
		writer.println(String.format("Content-Type: multipart/related;\r\n\tboundary=\"%s\"", BOUNDARY));
		writer.println("");
		writer.println(BOUNDARY);
		writer.println(String.format("Content-Type: multipart/alternative;\r\n\tboundary=\"%s\"", BOUNDARY));
		writer.println("");
		// 写体
		try {
			sendMessage(writer);
			writer.flush();
		} catch (IOException e) {
			Exceptions.log(e);
		}
	}
	

	private void sendMessage(PrintWriter sockout) throws IOException {
		for (Part part : parts) {
			if (part instanceof FilePart) {
				FilePart fPart = (FilePart) part;
				if(fPart.getName()==null){
					sockout.print(String.format(HEAD_FILE_ATTACHMENT, MIMEUtil.encodeRfc2047(fPart.getFilename(),"UTF-8")));
				}else{
					String mime=MimeTypes.getByFileName(fPart.getFilename());
					sockout.print(String.format(HEAD_HTML_RESOURCE, fPart.getName(),mime));
				}
				byte[] data = IOUtils.toByteArray(fPart.getInputStream());
				JefBase64.encodeAndPOutput(data,sockout);
				
			} else {
				NormalPart np = (NormalPart) part;
				if(!MimeTypes.isValid(np.getName())){
					np.setName("text/plain");
				}
				if(np.getTransferEncode()==null){
					if("text/html".equals(np.getName())){
						np.setTransferEncode("quoted-printable");
					}else{
						np.setTransferEncode("base64");
					}
				}
				sockout.print(String.format(HEAD_NORMAL,np.getName(),np.getTransferEncode()));
				if("base64".equals(np.getTransferEncode())){
					JefBase64.encodeAndPOutput(np.getContent().getBytes("UTF-8"),sockout);	
				}else{
					MIMEUtil.encodeQPAndoutput(np.getContent(), "UTF-8", sockout);	
				}
			}
			sockout.println("");
		}
		sockout.print(REQUEST_TAIL);
	}
}

