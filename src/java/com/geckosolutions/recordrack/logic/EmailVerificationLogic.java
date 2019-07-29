/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anthony1
 */
public class EmailVerificationLogic 
{
    public String generateCode( int length)
    {
        //String code = null;
        
        char alphabets[] = new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        Random charPicker = new Random(new Date().getTime());
        Random numberPicker = new Random(new Date().getTime());
        Random alphabetPicker = new Random(new Date().getTime());
        int numInt = 0,alphabetInt = 0;
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<length;i++)
        {
            //we use time as a seed, we'd use modulo to cast the resulting integer into appropriate bounds
            //first we'd pick a number from 0 and 1. 0 is digit 1 is alphabetic
            //if 1, we'd randomly pick a number between 0 and 9, if alphabet,we'd pick a number 
            //between a and z
            int charInt = convertToPositiveNumber(charPicker.nextInt())%2;
            
            if(charInt == 0)//pick a number from 0 to 9
            {
                numInt = convertToPositiveNumber(numberPicker.nextInt());
                System.out.println("number int" + numInt);
                builder.append(numInt%10);
            }
            else if(charInt == 1)
            {
                alphabetInt = convertToPositiveNumber(alphabetPicker.nextInt());
                System.out.println("alphabet int" + alphabetInt);
                builder.append(alphabets[alphabetInt%26]);
            }
        }   
        return builder.toString();
    }
    
    public boolean isVerificationCodeValid(String email, String code)
    {
        boolean result = false;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", "email_verification");
            object.put("dbName", "recordrack");
            object.put("whereArgs", "email='"+email+"' AND actvtn_cd='"+code+"' AND used=0");
            
            JSONArray array = DatabaseManager.getInstance().fetchData(object);
            result = array.length()>0;
            
            if(result)//that means the verification code entered is correct, we'd like to set it as used
            {
                object = new JSONObject();
                object.put("tableName", "email_verification");
                object.put("dbName", "recordrack");
                object.put("used", "1");
                object.put("whereArgs", "email='"+email+"' AND actvtn_cd='"+code+"' AND used=0");
                
                DatabaseManager.getInstance().updateData(object);
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public boolean doesEmailAddressExist(String email)
    {
        boolean result = false;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tableName", "store_databases");
            object.put("dbName", "recordrack");
            object.put("whereArgs","email='"+email+"'");
            JSONArray a = DatabaseManager.getInstance().fetchData(object);
            result = a.length()>0;
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            RecordRackLogger.getErrorLogger().error(e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    private int convertToPositiveNumber(int num)
    {
        return (num < 0)?(num* -1):num;
    }
}
