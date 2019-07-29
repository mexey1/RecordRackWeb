/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author anthony1
 */
public class ExceptionLogger 
{
    private final String logDir = "/tmp/recordrack/android_logs/";
    public void writeException(String rackID, String fileName,String text)
    {
        try
        {
            File biz = new File(logDir+rackID+"/");
            File log = new File(biz+"/"+fileName+".txt");
            
            if(!biz.exists())
                biz.mkdir();
            
            if(!log.exists())
                log.createNewFile();
            
            PrintWriter pw = new PrintWriter(new FileWriter(log,true));
            pw.append(text);
            pw.flush();
            pw.println();
            pw.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
    }
}
