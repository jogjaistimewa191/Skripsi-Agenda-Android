package com.jimmy.skripsi.helpers;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// Class untuk menyimpan data tanggal
public class MyDate implements Serializable {

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dfDateTime = new SimpleDateFormat("dd MMM HH:mm a");

    private Calendar cal;

    public MyDate() {
        cal = Calendar.getInstance();
    }

    public MyDate(Date date) {
        cal = Calendar.getInstance();
        cal.setTime(date);
    }

    public static String getCurDateTime() {
        Date d = new Date();
        return dfDateTime.format(d);
    }
}