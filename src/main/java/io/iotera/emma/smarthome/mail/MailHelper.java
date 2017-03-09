package io.iotera.emma.smarthome.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

public class MailHelper {

    @Autowired
    JavaMailSender sender;

    @Autowired
    Environment env;

    public void send(String email, String subject, String text) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = sender.createMimeMessage();
        message.setFrom(new InternetAddress(env.getProperty("mail.sender.address"),
                env.getProperty("mail.sender.name")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject(subject);
        message.setText(text);
        sender.send(message);
    }

}
