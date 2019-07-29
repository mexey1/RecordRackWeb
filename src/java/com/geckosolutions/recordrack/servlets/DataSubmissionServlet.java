/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import com.geckosolutions.recordrack.logic.DatabaseManager;
import javax.servlet.http.HttpServlet;
import com.mysql.jdbc.*;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *Servlet that handles submission of daily transactions from the client devices.
 * @author anthony1
 */
public class DataSubmissionServlet  extends BaseServlet
{
    @Override
    public void init()
    {
        
    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    {
        String data = readRequestData(req);
        System.out.print("this is the data "+data);
        //String query = req.getParameter("query");
        try
        {
            long pos = 0;
            JSONObject object = new JSONObject(data);
            String action = object.getString("action");
            object.remove("action");
            if(action.equals("insert"))
                pos = DatabaseManager.getInstance().insertData(object);
            else if(action.equals("update"))
                pos = DatabaseManager.getInstance().updateData(object);
            else if (action.equals("delete"))
                pos = DatabaseManager.getInstance().deleteData(object);
            
            
            //prepare response to send to client
            object = new JSONObject();
            if(pos > 0)
            {
                object.put("resp_cd", "200");
                object.put("resp_msg", "success");
            }
            else
            {
                object.put("resp_cd", "201");
                object.put("resp_msg", "failure");
            }
            res.getWriter().write(object.toString());
            //DatabaseManager.getInstance().
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
                e.printStackTrace();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
}
