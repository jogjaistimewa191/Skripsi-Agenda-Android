package com.jimmy.skripsi.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Util {

    public static void showToast(Context activity, String message){
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static ProgressDialog showProgressDialog(Context context, String message){
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        return dialog;
    }

    public static String getAddressFromLocation(Activity mActivity, double lat, double lng) {
        Geocoder gcd = new Geocoder(mActivity, Locale.getDefault());
        List<Address> addresses = null;
        String strAddress = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                strAddress = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality();
            } else {
                // do your staff
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strAddress;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateCurrent(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    public static String ConvertDate(String date, String formatAsal, String formatTujuan) {
        SimpleDateFormat sdfa = new SimpleDateFormat(formatAsal, Locale.US);
        SimpleDateFormat sdft = new SimpleDateFormat(formatTujuan, Locale.US);

        Date tgl = null;
        try {
            tgl = sdfa.parse(date);
        } catch (ParseException e) {
            Log.e("SimpleDateFormat", e.getMessage());
        }

        return sdft.format(tgl);
    }

    public static Date toDate(String date, String format) {
        SimpleDateFormat sdfa = new SimpleDateFormat(format, Locale.US);

        Date tgl = null;
        try {
            tgl = sdfa.parse(date);
        } catch (ParseException e) {
            Log.e("SimpleDateFormat", e.getMessage());
        }

        return tgl;
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static int getRandColor(){
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return color;
    }

}
