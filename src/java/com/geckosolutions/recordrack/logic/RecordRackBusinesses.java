/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *this class is responsible for retrieving details concerning businesses that use
 * RecorRack
 * @author anthony1
 */
public class RecordRackBusinesses 
{
    /**
     * this method retrieves all the store locations associated with an email 
     * address
     * @param email the email address of the business
     * @return store names and locations for the business
     */
    public JSONArray getStoresForEmail(String email)
    {
        JSONArray stores = null;
        try
        {
            JSONObject object = new JSONObject();
            object.put("dbName", "recordrack");
            object.put("tableName", "store_databases");
            object.put("whereArgs", "email='"+email+"'");
            JSONArray result = DatabaseManager.getInstance().fetchData(object);
            String dbName = null;
            if(result.length() == 0)//no store is currently using this email address
                stores = new JSONArray();
            else//get associated details from the different databases
            {
                stores = new JSONArray();
                for(int i=0; i<result.length();i++)
                {
                    object = new JSONObject();
                    dbName = result.getJSONObject(i).getString("db_name");
                    object.put("dbName", dbName);
                    object.put("tableName", "business_details");
                    object.put("columns", new String[]{"name","address","type","establishment_year","phone_number","created","last_edited"});
                    JSONArray res = DatabaseManager.getInstance().fetchData(object);
                    System.out.print("This is the request query "+res);
                    
                    //hopefully, the business table in each db contains only one entry
                    //we take that and add it to the stores JSONArray
                    stores.put(res.getJSONObject(0).put("rack_id", dbName).put("tableName", "business_details"));
                }
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
        
        return stores;
    }
}
