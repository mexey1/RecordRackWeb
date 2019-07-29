/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import java.io.DataInputStream;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author anthony1
 */
public class BaseServlet extends HttpServlet
{
    protected String readRequestData(HttpServletRequest req)
    {
        StringBuilder builder = new StringBuilder();
        try
        {
            int read = 0;
            //read the data we just receieved
            DataInputStream stream = new DataInputStream(req.getInputStream());
            byte [] data = new byte[4*1024];
            while((read = stream.read(data,0,data.length)) !=-1)
                builder.append(new String(data,0,read));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
