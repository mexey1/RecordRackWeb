/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.servlets;

import com.geckosolutions.recordrack.logic.DatabaseCreation;
import com.geckosolutions.recordrack.logic.DatabaseManager;
import com.geckosolutions.recordrack.logic.EmailVerificationLogic;
import com.geckosolutions.recordrack.logic.UtilityClass;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anthony1
 */
public class StoreRegistrationServlet extends BaseServlet
{
    @Override
    public void init()
    {
        
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
    {
        JSONObject object = null;
        try
        {
            String data = readRequestData(req);
            String rackID = registerStore(new JSONObject(data.trim()));
            object = new JSONObject();
            object.put("resp_cd", "200");
            object.put("resp_msg", rackID);
            
            res.getWriter().write(object.toString());
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
    
    /**
     * this method is called to register a new store. It creates a new database
     * and the necessary tables.
     * @param json a JSONObject containing the email to register the store with
     * @return the database name for the store, which is also the rack_id
     */
    private String registerStore(JSONObject json)
    {
       String rackID = null;
       try
       {
           //first, we create the database(rack_id) for this store and put it in 
           //recordrack.databases table, mapping the email address to the database name
           String db_name = new EmailVerificationLogic().generateCode(10);
           String email = json.getString("email");
           JSONObject object = new JSONObject();
           object.put("dbName", "recordrack");
           object.put("tableName", "store_databases");
           object.put("email", email);
           object.put("crtd_ts",UtilityClass.getDateTimeForSQL());
           object.put("db_name", db_name);
           DatabaseCreation dbCreation = new DatabaseCreation();
           
           if(!dbCreation.doesDatabaseExist(db_name))
                DatabaseManager.getInstance().insertData(object);
           
           //once done, we'd go ahead to create the new database and the tables;
           
           dbCreation.createDatabase(db_name, "MOBILE");
           rackID = db_name;
       }
       catch(JSONException e)
       {
           e.printStackTrace();
       }
       catch(SQLException e)
       {
           e.printStackTrace();
       }
       
       return rackID;
    }
}
