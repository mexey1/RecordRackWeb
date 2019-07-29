/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;
// Install the Java helper library from twilio.com/docs/java/install
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 *
 * @author anthony1
 */
public class SMSSender 
{
    // Find your Account Sid and Token at twilio.com/user/account
    public static final String ACCOUNT_SID = "ACbb8b2f23a09054c8555cdf94ef514ca9";
    public static final String AUTH_TOKEN = "a0491366dd1edb93af320cf447571588";
    public void sendSMS(String msg,String to) 
    {
      Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
      //+14124139581
      Message message = Message.creator(new PhoneNumber(to),
          new PhoneNumber("+14124139581"), "This is your Record Rack verification code: \n"+msg).create();

      System.out.println(message.getSid());

    }
    
}
