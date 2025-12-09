package com.monochrome.monochrome_player;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "MonochromePlayerSettings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_SORT_MODE = "sort_mode";
    
    public static final String THEME_ANDROID = "android";
    public static final String THEME_MATERIAL_YOU = "material_you";
    public static final String THEME_MONOCHROME = "monochrome";
    
    public static final String SORT_ALPHABETICAL = "alphabetical";
    public static final String SORT_DATE_ADDED = "date_added";
    
    private final SharedPreferences prefs;
    
    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_ANDROID);
    }
    
    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }
    
    public String getSortMode() {
        return prefs.getString(KEY_SORT_MODE, SORT_ALPHABETICAL);
    }
    
    public void setSortMode(String sortMode) {
        prefs.edit().putString(KEY_SORT_MODE, sortMode).apply();
    }
}
