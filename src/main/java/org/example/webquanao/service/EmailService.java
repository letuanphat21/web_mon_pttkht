package org.example.webquanao.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.webquanao.utils.EmailProperties;

import java.util.Properties;

public class EmailService {
    public void sendEmail(String to, String subject, String content) {

        // load config mỗi lần dùng (tránh lỗi static init)
        String host = EmailProperties.get("mail.host");
        String port = EmailProperties.get("mail.port");
        String username = EmailProperties.get("mail.username");
        String password = EmailProperties.get("mail.password");

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", EmailProperties.get("mail.auth"));
        props.put("mail.smtp.starttls.enable", EmailProperties.get("mail.starttls.enable"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);

            // HTML + UTF-8
            message.setContent(content, "text/html; charset=UTF-8");

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Send email failed", e);
        }
    }
}
