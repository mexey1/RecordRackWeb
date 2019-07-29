/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geckosolutions.recordrack.logic;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author anthony1
 */
public class UtilityClass 
{
    public static String getDateTimeForSQL()
    {
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        return ts.toString();
    }
}
