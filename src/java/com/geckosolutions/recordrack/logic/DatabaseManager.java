/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author anthony1
 */
package com.geckosolutions.recordrack.logic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseManager 
{
    private static DatabaseManager dbManager;
    private DataSource dataSource;
    private PoolProperties props;
    private String jdbcPath = "jdbc:mysql://127.0.0.1:3306/";
    private String uname = "root";
    private String pass = "hello";
    private static String [][] escapeChar = new String[][]
    {//search string      sql replacement string
        {"#$"               ,               "&"},
        {"\u0000"           ,                "\\0" },
        {"'"                ,                  "''"  },
        {"\""               ,                    "\\\\\""},
        {"\b"               ,                 "\\b"},
        {"\t"               ,                    "\\t"},
        {"\u001A"           ,                     "\\z"},
        {"\\"               ,                      "\\\\"}
    };
    
    
    public static DatabaseManager getInstance()
    {
        if(dbManager == null)
        {
            dbManager = new DatabaseManager();
            dbManager.init();
        }
        
        return dbManager;
    }
    
    private void init()
    {
        props = new PoolProperties();
        dataSource = new DataSource();
        props.setUrl(jdbcPath);
        //props.setUsername(jdbcPath);
        props.setUsername(uname);
        props.setPassword(pass);
        props.setMaxActive(100);
        props.setInitialSize(10);
        props.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setPoolProperties(props);
    }
    
    public Connection getConnection() throws SQLException
    {
        Connection conn = dataSource.getConnection();
        if(!conn.isValid(1000))
        {
            dataSource.close(true);
            init();
            conn = dataSource.getConnection();
        } 
        return conn;
    }
    
    public void execSQL(String dbName, String sql)
    {
        Connection conn = null;
        try
        {
            conn = getConnection();
            conn.prepareStatement("use database "+dbName).execute();
            conn.prepareStatement(sql).execute();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeConnection(conn);
        }
    }
    
    private void closeConnection(Connection conn)
    {
        try
        {
            conn.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
    * this method performs database queries. The query parameters are embedded in a JSONObject 
    * with the following keys; columns, tableName, dbName, whereArgs or extra. The columns variable(String array) 
    * represents the columns to select, if this value is omitted, all columns are selected.
    * the tableName specifies which table to query. dbName is the name of the database where the table resides
    * and the whereArgs is the condition that the query should satisfy. if whereArgs is left out, then the WHERE clause is omitted from the 
    * query statement. Note that the whereArgs doesn't include the WHERE clause. extra is put
    * when other SQL constraints are to be made.
    * @param queryString
    * @return a JSONArray containing the query result. The keys are obtained from the columns selected
    */
   public JSONArray fetchData(JSONObject queryString) throws SQLException
   {
        Connection conn = getConnection();
        String [] columns = null;
        String whereArgs = null;
        String extra = "";
        if(queryString.has("columns"))
                columns = (String [])queryString.get("columns");
        if(queryString.has("whereArgs"))
                whereArgs = queryString.getString("whereArgs");
        if(queryString.has("extra"))
                extra = queryString.getString("extra");
        int columnCount = -1;
        int loop = 0;
        String tableName = queryString.getString("tableName");
        String dbName = queryString.getString("dbName");
        StringBuilder builder = new StringBuilder();
        JSONArray array = null;
        JSONObject object = null;

        builder.append("SELECT ");
        if(columns == null)//no columns were specified, so all columns are selected
                builder.append("*");
        else
        {
            while(loop < columns.length)
            {
                builder.append(columns[loop]);
                if(loop+1 < columns.length)
                        builder.append(", ");
                loop++;
            }
        }

        builder.append(" FROM "+dbName+"."+tableName+ (whereArgs == null?"":" WHERE "+whereArgs)+ extra);
        try
        {
            System.out.println("This is the sql "+builder.toString());
            ResultSet result = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY).executeQuery(builder.toString());
            columnCount = columns == null?result.getMetaData().getColumnCount():columns.length;

            String val = null;
            if(result != null)
            {
                array = new JSONArray();
                while(result.next())
                {
                    object = new JSONObject();
                    for(loop = 0;loop<columnCount;loop++)
                    {
                        val = result.getString(loop+1);
                        object.put(columns == null?result.getMetaData().getColumnName(loop+1)
                                                                :columns[loop], val==null?"":val);
                    }
                    array.put(object);
                    object = null;
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeConnection(conn);
        }
        //System.out.println("result "+array);
        return array;
   }
   
   /**
    * this method is called to update records in the different tables. It accepts 
    * JSONObject with the following keys: 
    * dbName: name of database containing table to be updated
    * tableName: name of the table to update
    * whereArgs: constraint restricting which record in the table would be updated
    * the columns and their values are entered as key/value pairs
    * @param data the JSONObject containing information about database,table and values to be updated.
    * @throws SQLException 
    */
   public  long updateData(JSONObject data) 
   {
       long returnValue = -1;
       Connection conn = null;
       try
       {
            conn = getConnection();
            Statement state = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            StringBuilder columns = new StringBuilder();
            String whereArgs = data.getString("whereArgs");
            String dbName = data.getString("dbName");
            data.remove("whereArgs");
            data.remove("dbName");
            columns.append("UPDATE "+dbName+"."+data.getString("tableName")+" SET ");
            //remove tableName from the JSONObject, it's no longer needed
            data.remove("tableName");
            Iterator<String> keys = data.keys();
            String key = null;
            while(keys.hasNext())
            {
                 key = keys.next();
                 String value = null;

                 value = getDataFromJSON(data,key);
                 /*
                 if(data.get(key).getClass().getSimpleName().equals("Boolean"))
                    value=""+data.getBoolean(key);
                 else if(data.get(key).getClass().getSimpleName().equals("Integer"))
                    value=""+data.getInt(key);
                 else if(data.get(key).getClass().getSimpleName().equals("Double"))
                    value=""+data.getDouble(key);
                 else if(data.get(key).getClass().getSimpleName().equals("Long"))
                    value=""+data.getLong(key);
                 else
                    value=data.getString(key);*/

                 if(keys.hasNext())
                     columns.append(key+"= "+value+", ");
                 else
                     columns.append(key+"= "+value+" WHERE ");
             }
             columns.append(whereArgs);
             //state.execute("USE database "+dbName);
             System.out.println(columns.toString());
             returnValue = state.executeUpdate(columns.toString());
             System.out.println(columns.toString());
             conn.close();
             state.close();
             columns = null;
             keys = null;
             whereArgs = null;
       }
       catch(SQLException e)
       {
           e.printStackTrace();
       }
       finally
       {
            closeConnection(conn);
       }
         
         return returnValue;
   }
   
   /**
    * this method is called to insert data into the database. The JSONObject contains the 
    * following keys; tableName: table to insert into. dbName name of the database that contains 
    * the table. the different columns and their values are added in the JSONObject as key/value pairs.
    * @param data the JSONObject containing information to be inserted.
    * @return returns the position in the database that the data was inserted into or -1 if an error occurred.
    * @throws SQLException 
    */
   public long insertData(JSONObject data) throws SQLException
   {
        Connection conn = getConnection();
        int pos=0;
        Statement state = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        long returnValue = -1;
        try
        {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            String dbName = data.getString("dbName");
            String value="";
            columns.append("INSERT INTO "+dbName+"."+data.getString("tableName")+" (");
            values.append(" VALUES (");
            //remove tableName, it's no longer needed
            data.remove("tableName");
            
            data.remove("dbName");
            Iterator<String> keys = data.keys();
            String key = null;
            while(keys.hasNext())
            {
                key = keys.next();
                if(keys.hasNext())
                {
                    value = getDataFromJSON(data,key);
                    if(!value.equals("''"))
                    {
                        if(pos==0)
                        {
                            columns.append(key);
                            values.append(value);
                        }
                        else
                        {
                            columns.append(",").append(key);
                            values.append(",").append(value);
                        }
                        pos++;
                        //values;
                    }
                    
                    /*
                    if(data.get(key).getClass().getSimpleName().equals("Boolean"))
                         values.append(data.getBoolean(key)+", ");
                    else if(data.get(key).getClass().getSimpleName().equals("Integer"))
                         values.append(""+data.getInt(key)+", ");
                    else if(data.get(key).getClass().getSimpleName().equals("Double"))
                         values.append(""+data.getDouble(key)+", ");
                    else if(data.get(key).getClass().getSimpleName().equals("Long"))
                         values.append(""+data.getLong(key)+", ");
                    else
                         values.append("'"+escapeCharacters(data.getString(key))+"', ");*/
                }
                else
                {
                    value = getDataFromJSON(data,key);
                    if(!value.equals("''"))
                    {
                        columns.append(",").append(key);
                        values.append(",").append(value);
                    }
                    columns.append(")");
                    values.append(")");
                    /*
                    if(data.get(key).getClass().getSimpleName().equals("Boolean"))
                         values.append(data.getBoolean(key)+", ");
                    else
                         values.append("'"+escapeCharacters(data.getString(key))+"') ");*/
                }
             }
            columns.append(values.toString());
            System.out.println(columns.toString());
            //state.execute("USE database "+dbName);
            
            returnValue = state.executeUpdate(columns.toString());
            state.close();
            conn.close();
            columns = null;
            values = null;
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeConnection(conn);
        }
        
        return returnValue;
   }
   
   public long deleteData(JSONObject data) throws SQLException
   { 
       Connection conn = getConnection();
       Statement state = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
       long returnValue = -1;
       String whereArgs = (data.has("whereArgs")?data.getString("whereArgs"):"");
       String dbName = data.getString("dbName");
       String query = "DELETE FROM "+ dbName+"."+data.getString("tableName")+ (whereArgs.length()==0?"":" WHERE "+whereArgs);
       System.out.println(query);
       returnValue = state.executeUpdate(query);
       state.close();
       conn.close();
       
       return returnValue;
   }
   
   private String getDataFromJSON(JSONObject data,String key)
   {
       String value = "";
       try
       {
           if(data.get(key).getClass().getSimpleName().equals("Boolean"))
                value = ((Boolean)data.getBoolean(key)).toString();
            else if(data.get(key).getClass().getSimpleName().equals("Integer"))
                 value = ((Integer)data.getInt(key)).toString();
            else if(data.get(key).getClass().getSimpleName().equals("Double"))
                 value= ((Double)data.getDouble(key)).toString();
            else if(data.get(key).getClass().getSimpleName().equals("Long"))
                 value = ((Long)data.getLong(key)).toString();
           else
                value = "'"+escapeCharacters(data.getString(key))+"'";
       }
       catch(JSONException e)
       {
           e.printStackTrace();
       }
       
       return value.trim();
   }
   
   public void finalize()
   {
       if(dataSource !=null)
            dataSource.close();
   }
     
   //this code doesn't look efiicient...improve it
   private static String escapeCharacters(String text)
   {
       for(int count = 0; count<escapeChar.length; count++)
           text = text.replace(escapeChar[count][0], escapeChar[count][1]);
       return text;
   }   
}
