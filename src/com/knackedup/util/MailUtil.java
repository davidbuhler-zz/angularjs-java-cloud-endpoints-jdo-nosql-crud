package com.knackedup.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.knackedup.constant.AdminProperties;

public class MailUtil
{
	@SuppressWarnings("unused")
	private static Logger		LOG			= Logger.getLogger(MailUtil.class.getName());
	private static final String	KNACKED_UP	= "KnackedUp";

	public static void doSimpleMail(String emailAddress, String subject, String message, boolean blindCopyAdmin) throws UnsupportedEncodingException, MessagingException
	{
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		// TODO: Add opt out option
		String htmlBody = "<html><head></head><body><div style='background-color:#ebebeb;padding-left:20px;padding-right:20px;padding-bottom:20px;padding-top:40px;'>" + MailUtil.getBody(message) + "</div></body></html>";
		Multipart mp = new MimeMultipart();
		MimeBodyPart htmlPart = new MimeBodyPart();
		try
		{
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(AdminProperties.EMAIL, MailUtil.KNACKED_UP));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
			if (blindCopyAdmin)
			{
				msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(AdminProperties.EMAIL));
			}
			msg.setSubject(subject);
			htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
			mp.addBodyPart(htmlPart);
			msg.setContent(mp);
			Transport.send(msg);
		} catch (AddressException ex)
		{
			throw new AddressException("AddressException : " + ex);
		} catch (MessagingException ex)
		{
			throw new MessagingException("MessagingException : " + ex);
		} catch (UnsupportedEncodingException ex)
		{
			throw new UnsupportedEncodingException("UnsupportedEncodingException : " + ex);
		}
	}

	private static String getBody(String message)
	{
		StringBuilder s = new StringBuilder();
		s.append("<div style='background-color:#ffffff;color:#333;font-size:11pt;margin-bottom:0px;margin-top:0px;padding:10px'>");
		s.append(message);
		s.append("<br/><br/>Best,<br/><b>David and the KnackedUp team</b><br/>");
		s.append("</div>");
		return s.toString();
	}
}
