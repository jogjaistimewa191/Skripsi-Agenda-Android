package com.jimmy.skripsi.helpers;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Gxon
{
    public static String to(Object o)
    {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        return gson.toJson(o);
    }

    public static <T extends Object> T from(String s, Class<T> c)
    {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        return gson.fromJson(s, c);
    }

    public static <T extends Object> List<T> fromToList(String s, Class<T> c)
    {
        com.google.gson.Gson gson = new com.google.gson.Gson();

        List<T> result = new ArrayList<>();

        try
        {
            JSONArray ja = new JSONArray(s);

            for (int a = 0; a < ja.length(); a++)
            {
                result.add(gson.fromJson(ja.getJSONObject(a).toString(), c));
            }
        }
        catch (JSONException e)
        {
            return null;
        }

        return result;
    }
}
