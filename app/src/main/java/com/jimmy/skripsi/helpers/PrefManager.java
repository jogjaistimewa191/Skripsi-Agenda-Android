package com.jimmy.skripsi.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.jimmy.skripsi.App;

public class PrefManager {
    private static final String KEY_PREF = "KEY_PREF";
    private static final String KEY_STATUS_LOGIN = "STATUS_LOGIN";
    private static final String KEY_IS_LOGIN = "KEY_IS_LOGIN";
    private static final String KEY_NAME = "KEY_NAME";
    private static final String KEY_UID= "KEY_UID";
    private static SharedPreferences mSharedPreferences;

    public static void setAdmin(boolean isAdmin) {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_STATUS_LOGIN, isAdmin);
        editor.apply();
    }

    public static boolean isAdmin() {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_STATUS_LOGIN, false);
    }

    public static void setLogin(boolean isLogin) {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGIN, isLogin);
        editor.apply();
    }

    public static boolean isLogin() {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_IS_LOGIN, false);
    }

    public static void clear(){
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void setName(String name) {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public static String getName() {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_NAME, null);
    }

    public static void setUID(String uid) {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_UID, uid);
        editor.apply();
    }

    public static String getUID() {
        mSharedPreferences = App.getInstance().getSharedPreferences(KEY_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_UID, null);
    }


}
