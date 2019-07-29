/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.EmailSender;
import com.geckosolutions.recordrack.logic.EmailVerificationLogic;
import com.geckosolutions.recordrack.logic.RecordRackBusinesses;
import com.geckosolutions.recordrack.logic.RecordRackDatabase;
import com.geckosolutions.recordrack.logic.RecordRackLogger;
import com.geckosolutions.recordrack.logic.UtilityClass;
import com.geckosolutions.recordrack.logic.SMSSender;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anthony1
 */
public class EmailVerificationServlet extends BaseServlet
{
    /**
     * called once by the servlet container. Here we'd make sure we create
     * the RecordRack database.
     */
    @Override
    public void init()
    {
        new RecordRackDatabase().createRecordRackDatabaseAndTables();
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    {
        try
        {
            //Logger logger = RecordRackLogger.getDebugLogger();
            //logger.debug("Emailverificationservlet called");
            long pos = 0;
            //read the data we just receieved
            DataInputStream stream = new DataInputStream(req.getInputStream());
            int read = 0;
            String data = readRequestData(req);
            String response = null;
            System.out.println(data);
            
            JSONObject object = new JSONObject(data.trim());
            String action = object.getString("action");
            
            //if action is that of request, we'd need to generate and send verification code to client
            if(action.equals("request"))
            {
                pos = requestVerificationCode(object.getString("email"));
                //construct response to client
                object = new JSONObject();
                if(pos > 0)
                {
                    object.put("resp_msg", "A verification code has been sent to the email you provided");
                    object.put("resp_cd", "200");
                }
                else
                {
                    object.put("resp_msg", "An error occurred");
                    object.put("resp_cd", "201");
                }
                
                response = object.toString();
            }
            else if(action.equals("submit"))
            {
                System.out.println("request to verify code received");
                boolean result = new EmailVerificationLogic().isVerificationCodeValid(object.getString("email"),object.getString("code"));
                if(result)//if verification code is valid, let's retrieve the different store locations for this business.
                {
                    System.out.print("Code is valid");
                    JSONArray resp = new RecordRackBusinesses().getStoresForEmail(object.getString("email"));
                    object = new JSONObject();
                    object.put("resp_cd", "200");
                    object.put("resp_msg", resp);
                    response = object.toString();
                }
                else
                {
                    System.out.print("Code is invalid");
                    object = new JSONObject();
                    object.put("resp_cd", "201");
                    object.put("resp_msg", "Verification code is invalid");
                    response = object.toString();
                }
            }
            else if(action.equals("view"))
            {
                System.out.println("request to verify code received");
                if(new EmailVerificationLogic().doesEmailAddressExist(object.getString("email")))
                {
                    requestVerificationCode(object.getString("email"));
                    object = new JSONObject();
                    object.put("resp_cd", "200");
                    object.put("resp_msg", "A verification code has been sent to the email provided");
                }
                else
                {
                    object = new JSONObject();
                    object.put("resp_cd", "201");
                    object.put("resp_msg", "No business is registered with the email address provided");
                }
                response = object.toString();
            }
            //write response to client
            res.getWriter().write(response);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private long requestVerificationCode(String email)
    {
        long pos = -1;
        try
        {
            JSONObject object = null;
            EmailVerificationLogic logic = new EmailVerificationLogic();
            //generate verification code of length 7
            String code = logic.generateCode(7);
            //once done generating the random code, send out to user and save to database
            object = new JSONObject();
            object.put("tableName", "email_verification");
            object.put("dbName", "recordrack");
            object.put("actvtn_cd", code);
            object.put("crtd_ts",UtilityClass.getDateTimeForSQL());
            object.put("email", email);
            
            new EmailSender().sendMail(email, code);
            //new SMSSender().sendSMS(code, email);
            pos = DatabaseManager.getInstance().insertData(object);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return pos;
    }
}
