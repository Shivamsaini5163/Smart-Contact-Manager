package com.smartcontactmanager.app.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {
    public boolean sendEmail(String to,String subject,String body){
        // Gmail SMTP settings
        boolean status = false;
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "shivamhacker5163@gmail.com";
        String password = "rqtr wmks dqev rlci";

        // Set properties
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Authenticate
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true);
        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
//            message.setText(body);
            message.setContent( body,"text/html");
            // Send
            Transport.send(message);
            System.out.println("✅ Email sent successfully!");
            status = true;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return status;
    }
}
