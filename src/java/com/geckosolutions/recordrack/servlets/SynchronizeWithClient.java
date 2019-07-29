/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import com.geckosolutions.recordrack.logic.DatabaseCreation;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *This servlet receives request from the client to delete whatever database is on the server
 * and allow the client provide new data. It's more of taking a backup of the client.
 * @author anthony1
 */
public class SynchronizeWithClient extends BaseServlet
{
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    {
        String data = readRequestData(req);
        System.out.print("this is the data "+data);
        boolean success = false;
        //String query = req.getParameter("query");
        try
        {
            long pos = 0;
            JSONObject object = new JSONObject(data);
            //first, we'd delete the current database and then recreate it. 
            //Note, in the future, we'd have to first take a backup before deleting the db
            if(object.has("sync"))
            {
                DatabaseCreation dbc = new DatabaseCreation();
                dbc.deleteDatabase(object.getString("dbName"));
                dbc.createDatabase(object.getString("dbName"), "MOBILE");
                object = new JSONObject();
                object.put("resp_cd", "200");
                object.put("resp_msg", "Database created");
            }
            else //if this is an actual table data, we'd come here
            {
                //retrieve the array from the JSONObject
                JSONArray array = new JSONArray(object.getString("array"));
                String dbName = object.getString("dbName");//get dbName
                String tableName = object.getString("tableName");//get table name
                for(int i=0; i<array.length();i++)//loop through the array and insert
                {
                    //add db and table name to the object
                    object = array.getJSONObject(i);
                    object.put("tableName", tableName);
                    object.put("dbName", dbName);
                    DatabaseManager.getInstance().insertData(object);
                }
                object = new JSONObject();
                object.put("resp_cd", "200");
                object.put("resp_msg", "insert successful");
            }
            res.getWriter().write(object.toString());
            success = true;
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
        finally
        {
            try
            {
                if(!success)
                {
                   JSONObject object = new JSONObject();
                   object.put("resp_cd", "201");
                   object.put("resp_msg", "error occurred");
                   res.getWriter().write(object.toString());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
