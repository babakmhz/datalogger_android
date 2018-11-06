package com.android.datalogger;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private final SharedPreferences preferences;
    private Context context;

    private static final String IP_KEY = "ip";

    public Prefs(Context context) {
        preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.context = context;
    }

    public void saveUrl(String url) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(IP_KEY, url);
        editor.apply();
    }

    public String getUrl(){
        return preferences.getString(IP_KEY,"");
    }

}
