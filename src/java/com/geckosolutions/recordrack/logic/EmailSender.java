/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

/**
 *
 * @author anthony1
 */
import java.util.*;  
import javax.mail.*;  
import javax.mail.internet.*; 
import javax.activation.*;  

  
public class EmailSender  
{  
    public void sendMail(String recipient,String content)
    {  
        String to = recipient;//change accordingly  
        String from = "rexjay8@gmail.com";//change accordingly  
        String host = "localhost";//or IP address  

       //Get the session object  
        Properties properties = System.getProperties();  
        properties.setProperty("mail.smtp.host", host);  
        Session session = Session.getDefaultInstance(properties);  

       //compose the message  
        try
        {  
           String text = "Hi there,\n  This is your Record Rack verification code: "+content;
           MimeMessage message = new MimeMessage(session);  
           message.setFrom(new InternetAddress(from));  
           message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));  
           message.setSubject("Record Rack verification code");  
           message.setText(text);  
          
           // Send message  
           Transport.send(message);  
           System.out.println("message sent successfully....");  

        }
        catch (MessagingException mex) 
        {
            mex.printStackTrace();
        }  
   }  
}  