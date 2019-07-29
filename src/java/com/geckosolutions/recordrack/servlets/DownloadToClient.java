/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import com.geckosolutions.recordrack.logic.DatabaseManager;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anthony1
 */
public class DownloadToClient extends BaseServlet
{
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    {
        try
        {
            String data = readRequestData(req);
            JSONObject object = new JSONObject(data);
            JSONObject obj = new JSONObject();
            //String dbName = object.getString("dbName");
            String tableName = object.getString("tableName");
            JSONArray array = DatabaseManager.getInstance().fetchData(object);
            object = new JSONObject();
            object.put("array", array.toString());
            object.put("tableName", tableName);
            obj.put("resp_cd", "200");
            obj.put("resp_msg", object.toString());
            res.getWriter().write(obj.toString());
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
}
