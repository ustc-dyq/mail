package com.personal.mail;

import java.security.Security;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class JavaMail {

	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private String mailHost;
	private String name;
	private String passwd;
	private Properties props;
	private Session session;
	
	public JavaMail(String mailHost,String name,String passwd) {
		this.mailHost = mailHost;
		this.name = name;
		this.passwd = passwd;
	}
	
	public void init(boolean isSSL) {
		props = System.getProperties();
		props.setProperty("mail.smtp.host", mailHost);
		props.setProperty("mail.smtp.auth", "true");
		if(isSSL) {
			props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
	        props.setProperty("mail.smtp.socketFactory.fallback", "false");
	        props.setProperty("mail.smtp.port", "465");
	        props.setProperty("mail.smtp.socketFactory.port", "465");
		}
		session = Session.getDefaultInstance(props, new Authenticator() {
            //身份认证
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(name, passwd);
            }
        });
	}
	
	
	public void sendMail(String from,String to,String subject,String text) throws Exception {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
	    message.setRecipients(Message.RecipientType.TO, to);
	    message.setSubject(subject);
	    message.setText(text);
	    Transport.send(message);
	}
	
	public void sendMail(String from,String to,String subject, MimeMultipart mm) throws Exception {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
	    message.setRecipients(Message.RecipientType.TO, to);
	    message.setSubject(subject);
	    message.setContent(mm);

        // 12. 设置发件时间
        message.setSentDate(new Date());
	    Transport.send(message);
	}
	
	public void sendMultiMessageDemo(String from,String to,String subject) throws Exception {
		// 1. 创建邮件对象
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(from, "我的测试邮件_发件人昵称", "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to, "我的测试邮件_收件人昵称", "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject(subject);

        /*
         * 下面是邮件内容的创建:
         */

        // 5. 创建图片“节点”
        MimeBodyPart image = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource("FairyTail.jpg")); // 读取本地文件
        image.setDataHandler(dh);                   // 将图片数据添加到“节点”
        image.setContentID("image_fairy_tail");     // 为“节点”设置一个唯一编号（在文本“节点”将引用该ID）

        // 6. 创建文本“节点”
        MimeBodyPart text = new MimeBodyPart();
        //    这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
        text.setContent("这是一张图片<br/><img src='cid:image_fairy_tail'/>", "text/html;charset=UTF-8");

        // 7. （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
        MimeMultipart mm_text_image = new MimeMultipart();
        mm_text_image.addBodyPart(text);
        mm_text_image.addBodyPart(image);
        mm_text_image.setSubType("related");    // 关联关系

        // 8. 将 文本+图片 的混合“节点”封装成一个普通“节点”
        //    最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
        //    上面的 mm_text_image 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
        MimeBodyPart text_image = new MimeBodyPart();
        text_image.setContent(mm_text_image);

        // 9. 创建附件“节点”
        MimeBodyPart attachment = new MimeBodyPart();
        DataHandler dh2 = new DataHandler(new FileDataSource("妖精的尾巴目录.doc"));  // 读取本地文件
        attachment.setDataHandler(dh2);                                             // 将附件数据添加到“节点”
        attachment.setFileName(MimeUtility.encodeText(dh2.getName()));              // 设置附件的文件名（需要编码）

        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text_image);
        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
        mm.setSubType("mixed");         // 混合关系

        // 11. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
        message.setContent(mm);

        // 12. 设置发件时间
        message.setSentDate(new Date());

        // 13. 保存上面的所有设置
        message.saveChanges();
        Transport.send(message);

	}
	
	
	public static void test() throws Exception {
		JavaMail mail = new JavaMail("smtp.163.com","13270806206@163.com","1991725dyq");
		mail.init(false);
		mail.sendMail("13270806206@163.com", "1124276334@qq.com", "javamail", "hello world");
	}
	
	public static void testSSL() throws Exception {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		JavaMail mail = new JavaMail("smtp.qq.com","1124276334@qq.com","fchahkuxjjonihji");
		mail.init(true);
		mail.sendMail("1124276334@qq.com", "13270806206@163.com", "javamail", "hello world");
	}
	
	public static void main(String[] args) throws Exception {
		
	}

}
