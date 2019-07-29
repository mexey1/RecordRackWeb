/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import com.geckosolutions.recordrack.logic.ExceptionLogger;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author anthony1
 */
public class ExceptionServlet extends BaseServlet
{
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
    {
        try
        {
            String reqData = readRequestData(req);
            JSONObject object = new JSONObject(reqData);
            String file = object.getString("file");
            String data = object.getString("data");
            String rackID = object.getString("rackID");
            new ExceptionLogger().writeException(rackID,file, data);
        
            object = new JSONObject();
            object.put("resp_cd", "200");
            object.put("resp_msg", rackID);
            
            res.getWriter().write(object.toString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
