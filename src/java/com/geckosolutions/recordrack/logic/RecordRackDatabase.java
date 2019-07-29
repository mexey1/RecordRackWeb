/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *this class is responsible for creating database and tables used by record rack web app
 * @author anthony1
 */

public class RecordRackDatabase 
{
    //tables used by record rack web application
    //databases holds all the databases of all record rack mobile users and the associated email address
    //email verification table is used to hold the verification code that's sent to the user
    private static String database ="CREATE TABLE IF NOT EXISTS recordrack.store_databases(id INTEGER AUTO_INCREMENT PRIMARY KEY, db_name VARCHAR(100), email VARCHAR(100), password TEXT,crtd_ts DATETIME, INDEX _email(email,db_name,crtd_ts));";
    private static String email_verification ="CREATE TABLE IF NOT EXISTS recordrack.email_verification(id INTEGER AUTO_INCREMENT PRIMARY KEY, email VARCHAR(100), actvtn_cd VARCHAR(8), used TINYINT DEFAULT 0,crtd_ts DATETIME, INDEX _email(email,crtd_ts));";
    private static String users = "CREATE TABLE IF NOT EXISTS recordrack.users (id INTEGER AUTO_INCREMENT PRIMARY KEY, email VARCHAR(100), password TEXT, phone_number VARCHAR(20),crtd_ts DATETIME, INDEX _phone(phone_number,crtd_ts));";
    /*
     *this method is called to create databases used by record rack web
     */
    public void createRecordRackDatabaseAndTables()
    {
        try
        {
            Connection conn = DatabaseManager.getInstance().getConnection();
            conn.prepareStatement("CREATE DATABASE IF NOT EXISTS recordrack;").execute();
            //conn.prepareStatement("USE recordrack;").execute();
            conn.prepareStatement(database).execute();
            conn.prepareStatement(email_verification).execute();
            conn.prepareStatement(users).execute();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
}
