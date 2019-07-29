/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 *
 * @author anthony1
 */
public class RecordRackLogger 
{
    private static Logger debugLogger, errorLogger;
    
    private static void initLogger(String typ)
    {
        
        System.setProperty("log4j.configurationFile", "/Users/anthony1/Downloads/log4j.properties");
        if(typ.length() == 0)
            debugLogger = LogManager.getRootLogger();//LogManager.getRootLogger();
        else
            errorLogger = LogManager.getLogger("com.geckosolutions.recordrack");
        //PropertyConfigurator.configure("/Users/anthony1/Downloads/log4j.properties");
        
        //PropertyConfigurator.configure("/usr/apache-tomcat-8.0.46/conf/log4j.properties");
    }
    
    public static Logger getDebugLogger()
    {
        if(debugLogger == null)
            initLogger("");
        return debugLogger;
    }
    
    public static Logger getErrorLogger()
    {
        if(errorLogger == null)
            initLogger("");
        return errorLogger;
    }
    
}
