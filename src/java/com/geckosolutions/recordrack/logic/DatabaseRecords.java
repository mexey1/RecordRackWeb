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
 *
 * @author anthony1
 */
public class DatabaseRecords 
{
    /**
     * this method is called to retrieve data from a given table residing in a given database
     * @param dbName database from which data is to be retrieved
     * @param table table where data resides
     * @param startDate start date of the retrieval
     * @param endDate end date of the retrieval
     * @return requested data as a JSONArray
     */
    public JSONArray getDataInTable(String dbName, String table, String startDate,String endDate)
    {
        JSONArray array = null;
        try
        {
            JSONObject object = new JSONObject();
            object.put("tabelName", table);
            object.put("dbName", dbName);
            object.put("whereArgs", "modified_ts between '"+startDate+"' and '"+endDate+"'");
            array = DatabaseManager.getInstance().fetchData(object);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return array;
    }
}
