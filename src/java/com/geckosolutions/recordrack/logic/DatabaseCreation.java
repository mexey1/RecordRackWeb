/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *This class is responsible for creating a new database and all the tables it'd be needing
 * @author anthony1
 */
public class DatabaseCreation 
{
     /*******************
     * Archived ==1 means deleted, 0 means not deleted
     * Suspended == 1 means suspended transaction, 0 means not suspended
     * All edits result in the item being deleted and then re-saved. Only exception is the unit table
     * 
     * CHANGES MADE TO THE DIFFERENT TABLES
     * CLIENT: CHANGED THE phone_number TYPE FROM TEXT TO VARCHAR(50)
     */
    //tables used by the record rack mobile application
    private static DatabaseManager dbManager;
    private static int dbCount;
    private static String business_details = "CREATE TABLE IF NOT EXISTS business_details(id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, address TEXT DEFAULT NULL, type TEXT DEFAULT NULL, establishment_year TEXT NOT NULL, phone_number TEXT DEFAULT NULL, business_logo BLOB, extra_details TEXT DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL) ";
    private static String user = "CREATE TABLE IF NOT EXISTS user(id INTEGER PRIMARY KEY NOT NULL, name TEXT, username TEXT, password TEXT, secret_question TEXT, answer TEXT) ";
    private static String category = "CREATE TABLE IF NOT EXISTS category(id INTEGER PRIMARY KEY  NOT NULL, category VARCHAR(100) UNIQUE NOT NULL, short_form VARCHAR(25) DEFAULT NULL, archived TINYINT(4) NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String client = "CREATE TABLE IF NOT EXISTS client(id INTEGER PRIMARY KEY , first_name TEXT DEFAULT NULL, last_name TEXT DEFAULT NULL, preferred_name TEXT NOT NULL, phone_number VARCHAR(50) UNIQUE NOT NULL, alternate_phone_number TEXT NOT NULL, address TEXT DEFAULT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String credit_payment = "CREATE TABLE IF NOT EXISTS credit_payment(id INTEGER PRIMARY KEY , credit_transaction_id INTEGER NOT NULL, total_debt DECIMAL(19,2) NOT NULL, amount_paid DECIMAL(19,2) NOT NULL, balance DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, due_date DATETIME NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String credit_transaction = "CREATE TABLE IF NOT EXISTS credit_transaction(id INTEGER PRIMARY KEY  NOT NULL, client_id INTEGER NOT NULL, transaction_table TEXT NOT NULL, transaction_id INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";

    private static String creditor = "CREATE TABLE IF NOT EXISTS creditor(id INTEGER PRIMARY KEY , client_id INTEGER NOT NULL, credit DECIMAL(19,2) NOT NULL, last_due_date DATETIME NOT NULL, currency VARCHAR(4) NOT NULL, archived INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    //quantity values in the current quantity table are in base unit
    private static String current_quantity = "CREATE TABLE IF NOT EXISTS current_quantity(id INTEGER PRIMARY KEY , item_id INTEGER UNIQUE, quantity DOUBLE, created DATETIME, last_edited DATETIME, user_id INTEGER)";
    private static String customer = "CREATE TABLE IF NOT EXISTS customer(id INTEGER PRIMARY KEY NOT NULL, client_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String db_info = "CREATE TABLE IF NOT EXISTS db_info(version_number VARCHAR(15), rack_id VARCHAR(20), current_backup_filename VARCHAR(70), last_upload DATETIME, validation_key VARCHAR(150), expiry_date DATETIME, created DATETIME, last_edited DATETIME)";
    private static String debt_payment = "CREATE TABLE IF NOT EXISTS debt_payment(id INTEGER PRIMARY KEY, debt_transaction_id INTEGER NOT NULL, total_debt DECIMAL(19,2) NOT NULL, amount_paid DECIMAL(19,2) NOT NULL, balance DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, due_date DATETIME NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String debt_transaction = "CREATE TABLE IF NOT EXISTS debt_transaction(id INTEGER PRIMARY KEY NOT NULL, debtor_id INTEGER NOT NULL, transaction_table TEXT NOT NULL, transaction_id INTEGER UNIQUE NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";

    private static String debtor = "CREATE TABLE IF NOT EXISTS debtor(id INTEGER PRIMARY KEY, client_id INTEGER NOT NULL, debt DECIMAL(19,2) NOT NULL, last_due_date DATETIME NOT NULL, currency VARCHAR(4) NOT NULL, archived INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String expense_purpose = "CREATE TABLE IF NOT EXISTS expense_purpose(id INTEGER PRIMARY KEY, purpose VARCHAR(100) NOT NULL, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String expense = "CREATE TABLE IF NOT EXISTS expense(id INTEGER PRIMARY KEY, name TEXT DEFAULT NULL, client_id INTEGER default 0, purpose VARCHAR(200) NOT NULL, amount DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, archived TINYINT NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String income = "CREATE TABLE IF NOT EXISTS income(id INTEGER PRIMARY KEY, name TEXT DEFAULT NULL, client_id INTEGER default 0, purpose VARCHAR(200) NOT NULL, amount DECIMAL(19,2) NOT NULL, currency VARCHAR(4) NOT NULL, archived TINYINT NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String income_purpose = "CREATE TABLE IF NOT EXISTS income_purpose(id INTEGER PRIMARY KEY, purpose VARCHAR(20), created DATETIME, last_edited DATETIME, user_id INTEGER)";
    private static String initial_quantity = "CREATE TABLE IF NOT EXISTS initial_quantity(id INTEGER PRIMARY KEY, item_id INTEGER NOT NULL, quantity DOUBLE NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String item = "CREATE TABLE IF NOT EXISTS item(id INTEGER PRIMARY KEY, category_id INTEGER NOT NULL, item VARCHAR(200) NOT NULL, short_form VARCHAR(25) DEFAULT NULL, divisible INTEGER DEFAULT 1, archived TINYINT NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL, barcode VARCHAR(100) DEFAULT NULL)";
    private static String last_used_date_time ="CREATE TABLE IF NOT EXISTS last_used_date_time(date_time DATETIME, created DATETIME)";
    //private static String name = "CREATE TABLE IF NOT EXISTS name(id INTEGER PRIMARY KEY, name VARCHAR(100), created DATETIME, last_edited DATETIME, user_id INTEGER)";
    private static String notes = "CREATE TABLE IF NOT EXISTS notes(id INTEGER PRIMARY KEY, notes VARCHAR(300) NOT NULL, table_name VARCHAR(30) DEFAULT NULL, table_id INTEGER DEFAULT NULL, reminder_date DATETIME DEFAULT NULL, reminder_frequency INTEGER DEFAULT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String notifications = "CREATE TABLE IF NOT EXISTS notifications(id INTEGER PRIMARY KEY, title VARCHAR(300) NOT NULL, short_description VARCHAR(50) DEFAULT NULL, long_description VARCHAR(300) DEFAULT NULL, table_name TEXT DEFAULT NULL, table_id INTEGER DEFAULT NULL, link VARCHAR(70) DEFAULT NULL, created DATETIME NOT NULL , last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String pending_user = "CREATE TABLE IF NOT EXISTS pending_user(id INTEGER PRIMARY KEY, surname VARCHAR(30), first_name VARCHAR(30), other_name VARCHAR(30), username VARCHAR(25), password VARCHAR(60), secret_question VARCHAR(60), secret_answer VARCHAR(60), gender VARCHAR(10), photo BLOB, phone_number VARCHAR(20), alternate_phone_number VARCHAR(20), email_address VARCHAR(100), active INTEGER, created DATETIME, last_edited DATETIME)";
    private static String purchase_item = "CREATE TABLE IF NOT EXISTS purchase_item(id INTEGER PRIMARY KEY, purchase_transaction_id INTEGER NOT NULL, item_id INTEGER NOT NULL, UNIT_ID INTEGER NOT NULL, unit_price DECIMAL(19,2) NOT NULL, quantity DOUBLE NOT NULL, _quantity DOUBLE NOT NULL, cost DOUBLE NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String purchase_transaction ="CREATE TABLE IF NOT EXISTS purchase_transaction(id INTEGER PRIMARY KEY, name VARCHAR(50) NOT NULL, client_id INTEGER DEFAULT NULL, amount_paid DOUBLE NOT NULL DEFAULT 0, total DOUBLE NOT NULL DEFAULT 0, suspended INTEGER NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, ded_frm_rev TINYINT DEFAULT 0, archived INTEGER NOT NULL, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    //private static String purchase = "CREATE TABLE IF NOT EXISTS purchase(id INTEGER PRIMARY KEY, transact_id INTEGER, name_id INTEGER, category_id INTEGER, item_id INTEGER, unit_price DECIMAL(19,2), quantity DOUBLE, unit_id INTEGER, total_cost DECIMAL(19,2), amount_paid DECIMAL(19,2), balance DECIMAL(19,2), currency VARCHAR(4), archived TINYINT, notes_id INTEGER, created VARCHAR(40), last_edited VARCHAR(40), user_id INTEGER)";
    //private static String sales = "CREATE TABLE IF NOT EXISTS sales(id INTEGER PRIMARY KEY, transact_id INTEGER, name_id INTEGER, category_id INTEGER, item_id INTEGER, unit_price DECIMAL(19,2), quantity DOUBLE, unit_id INTEGER, total_cost DECIMAL(19,2), amount_paid DECIMAL(19,2), balance DECIMAL(19,2), currency VARCHAR(4), archived TINYINT, notes_id INTEGER, created VARCHAR(40), last_edited VARCHAR(40), user_id INTEGER)";
    private static String sale_item = "CREATE TABLE IF NOT EXISTS sale_item(id INTEGER PRIMARY KEY, sale_transaction_id INTEGER NOT NULL, item_id INTEGER NOT NULL, unit_id INTEGER NOT NULL, unit_price DECIMAL(19,2) NOT NULL, quantity DOUBLE NOT NULL, _quantity DOUBLE NOT NULL, cost DOUBLE NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String sale_transaction ="CREATE TABLE IF NOT EXISTS sale_transaction(id INTEGER PRIMARY KEY, name VARCHAR(50) NOT NULL, amount_paid DOUBLE NOT NULL, total DOUBLE NOT NULL DEFAULT 0, client_id INTEGER DEFAULT NULL, suspended INTEGER NOT NULL DEFAULT 0, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String special_quantity ="CREATE TABLE IF NOT EXISTS special_quantity(id INTEGER PRIMARY KEY, item_id INTEGER NOT NULL, formula VARCHAR(200) NOT NULL, notes_id INTEGER, archived TINYINT NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String unit = "CREATE TABLE IF NOT EXISTS unit(id INTEGER PRIMARY KEY, item_id INTEGER NOT NULL, unit VARCHAR(100) NOT NULL, short_form VARCHAR(10) NOT NULL, base_unit_equivalent DOUBLE NOT NULL, cost_price DECIMAL(19,2) NOT NULL DEFAULT 0.00, retail_price DECIMAL(19,2) NOT NULL DEFAULT 0.00, is_default INTEGER NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT 0, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String unit_relation = "CREATE TABLE IF NOT EXISTS unit_relation(id INTEGER PRIMARY KEY, item_id INTEGER NOT NULL, old_unit_quantity DOUBLE NOT NULL, old_unit_id INTEGER NOT NULL, new_unit_quantity DOUBLE NOT NULL, new_unit_id INTEGER NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String suspend = "CREATE TABLE IF NOT EXISTS sale_item(id INTEGER PRIMARY KEY, sale_transaction_id INTEGER NOT NULL, item_id INTEGER NOT NULL, unit_id INTEGER NOT NULL, unit_price DECIMAL(19,2) NOT NULL, quantity DOUBLE NOT NULL, _quantity DOUBLE NOT NULL, cost DOUBLE NOT NULL, currency VARCHAR(4) NOT NULL, notes_id INTEGER DEFAULT NULL, archived INTEGER NOT NULL DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String vendor = "CREATE TABLE IF NOT EXISTS vendor(id INTEGER PRIMARY KEY, client_id INTEGER UNIQUE NOT NULL, notes_id INTEGER DEFAULT NULL,archived INTEGER DEFAULT 0, created DATETIME NOT NULL, last_edited DATETIME NOT NULL, user_id INTEGER NOT NULL)";
    private static String settings ="CREATE TABLE IF NOT EXISTS settings(comp_nm TEXT, val TEXT)";
    
    //purchase transaction modified in version 3 of database
    private static String alter_purchase_transaction_amount_pd = "ALTER TABLE purchase_transaction ADD COLUMN  amount_paid DOUBLE NOT NULL DEFAULT 0";
    private static String getAlter_purchase_transaction_ded_frm_rev ="ALTER TABLE purchase_transaction ADD COLUMN ded_frm_rev TINYINT DEFAULT 0";

    //SALE_TRANSACTION TABLE MODIFIED IN VERSION 4 OF DATABASE
    private static String alter_sale_transaction_total = "ALTER TABLE sale_transaction ADD COLUMN total DOUBLE NOT NULL DEFAULT 0";
    private static String alter_purchase_transaction_total = "ALTER TABLE purchase_transaction ADD COLUMN total DOUBLE NOT NULL DEFAULT 0";

   
    private static int user_id;
    
    /**
     * this method is called to create a database
     * @param dbName the name of the database to be created
     * @param type the database type, if it's being used by the mobile or web application
     */
    public void createDatabase(String dbName,String type)
    {
        try
        {
            Connection conn = DatabaseManager.getInstance().getConnection();
            conn.prepareStatement("CREATE DATABASE IF NOT EXISTS "+dbName).execute();
            conn.prepareStatement("USE "+dbName).execute();
            if(type.equals("WEB"))
            {
                
            }
            else if(type.equals("MOBILE"))
            {
                conn.prepareStatement(business_details).execute();
                conn.prepareStatement(user).execute();
                conn.prepareStatement(category).execute();
                conn.prepareStatement(client).execute();
                conn.prepareStatement(creditor).execute();
                conn.prepareStatement(credit_payment).execute();
                conn.prepareStatement(credit_transaction).execute();
                conn.prepareStatement(current_quantity).execute();
                conn.prepareStatement(customer).execute();
                conn.prepareStatement(db_info).execute();
                conn.prepareStatement(debtor).execute();
                conn.prepareStatement(debt_payment).execute();
                conn.prepareStatement(debt_transaction).execute();
                conn.prepareStatement(expense_purpose).execute();
                conn.prepareStatement(expense).execute();
                conn.prepareStatement(income).execute();
                conn.prepareStatement(income_purpose).execute();
                conn.prepareStatement(initial_quantity).execute();
                conn.prepareStatement(item).execute();
                conn.prepareStatement(last_used_date_time).execute();
                //conn.prepareStatement(name).execute();
                conn.prepareStatement(notes).execute();
                conn.prepareStatement(notifications).execute();
                conn.prepareStatement(pending_user).execute();
                //conn.prepareStatement(purchase).execute();
                conn.prepareStatement(purchase_item).execute();
                conn.prepareStatement(purchase_transaction).execute();
                conn.prepareStatement(sale_item).execute();
                conn.prepareStatement(sale_transaction).execute();
                //conn.prepareStatement(sales).execute();
                conn.prepareStatement(suspend).execute();
                conn.prepareStatement(special_quantity).execute();
                conn.prepareStatement(unit).execute();
                conn.prepareStatement(unit_relation).execute();
                conn.prepareStatement(vendor).execute();
                conn.prepareStatement(settings).execute();
                
                conn.close();
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public boolean doesDatabaseExist(String dbName)
    {
        boolean result = false;
        try
        {
            JSONObject object = new JSONObject();
            object.put("dbName", "recordrack");
            object.put("tableName", "store_databases");
            object.put("whereArgs", "db_name = '"+dbName+"'");
            result =DatabaseManager.getInstance().fetchData(object).length() > 0;
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
    public void deleteDatabase(String dbName)
    {
        //in the fure, we'd have to take a backup of the db first
        try
        {
            //ProcessBuilder builder = new ProcessBuilder();
            String command = "/usr/local/mysql/bin/mysqldump -uroot -phello "+dbName+">/var/recordrack/db_backup/"+dbName+".sql";
            FileWriter writer = new FileWriter("/var/recordrack/db_backup/command.sh");
            writer.write(command);
            writer.flush();
            writer.close();
            System.out.println(command+"would also get erryg");
            Process proc = Runtime.getRuntime().exec("/var/recordrack/db_backup/command.sh");
            BufferedInputStream bis = new BufferedInputStream(proc.getErrorStream());
            //StringBuilder builder = new StringBuilder();
            byte d[] = new byte[8*1024];
            int r=0;
            while((r=bis.read(d,0,d.length)) !=-1)
                System.out.println(new String(d,0,r));
            
            int res = proc.waitFor();
            System.out.println("Result from backup "+res);
            //builder.start();
            Connection conn = DatabaseManager.getInstance().getConnection();
            conn.createStatement().execute("drop database "+dbName);
            conn.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        
    }
    
}
