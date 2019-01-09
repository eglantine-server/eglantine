package com.eglantine.mail;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailTLS {

	final static String username = "eglantine.server@gmail.com";
	final static String password = "banyuls2018";

	@SuppressWarnings("serial")
	static class MyProperties extends Properties {
		MyProperties(String[][] arrayProps) {
			for (int i = 0; i < arrayProps.length;i++) {
				this.put(new String(arrayProps[i][0]), new String(arrayProps[i][1]));
			}
		}
	}

	static final String[][] gmailTLSServer = new String[][] {
		{"mail.smtp.auth", "true"},
		{"mail.smtp.starttls.enable", "true"},
		{"mail.smtp.host", "smtp.gmail.com"},
		{"mail.smtp.port", "587"}
	};

	public static void sendMail(String[][] server,String[] auth,String replyto,String to,String subject,String text) {

		Properties props = new MyProperties(server);

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(auth[0], auth[1]);
			}
		});

		try {

			Message message = new MimeMessage(session);
			//			message.setFrom(new InternetAddress("eglantine.server@gmail.com"));
//		message.setFrom(new InternetAddress("christophe.drion@gmail.com"));
			message.setReplyTo(new Address[] {new InternetAddress(replyto)});
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(text);
			Transport.send(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sendEglantineMail(String replyto,String to,String subject,String text) {
		
		sendMail(gmailTLSServer,new String[] {username,password},replyto,to,subject,text);
		
	}
	public static void main(String[] args) {
		sendMail(gmailTLSServer,new String[] {username,password},"christophe.drion@gmail.com","christophe.drion@gmail.com","Hey Friend","ca va mon pote ?");
	}
}