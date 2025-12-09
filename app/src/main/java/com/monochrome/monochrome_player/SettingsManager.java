package com.monochrome.monochrome_player;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "MonochromePlayerSettings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_SORT_MODE = "sort_mode";
    private static final String KEY_WALLPAPER = "wallpaper_uri";
    private static final String KEY_WALLPAPER_PREFIX = "wallpaper_uri_";
    private static final String KEY_INCLUDED_FOLDERS = "included_folders";
    private static final String KEY_INCLUDE_DOWNLOADS = "include_downloads";
    private static final String KEY_INCLUDE_DOCUMENTS = "include_documents";
    private static final String KEY_INCLUDE_MUSIC = "include_music";
    
    public static final String THEME_ANDROID = "android";
    public static final String THEME_MATERIAL_YOU = "material_you";
    public static final String THEME_MONOCHROME = "monochrome";
    public static final String THEME_BOZKURT = "bozkurt";
    public static final String THEME_MECHA = "mecha";
    
    public static final String SORT_ALPHABETICAL = "alphabetical";
    public static final String SORT_DATE_ADDED = "date_added";
    
    private final SharedPreferences prefs;
    
    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Included folders (persisted SAF-tree URIs) - use String set
    public java.util.Set<String> getIncludedFolders() {
        java.util.Set<String> set = prefs.getStringSet(KEY_INCLUDED_FOLDERS, null);
        if (set == null) return new java.util.HashSet<>();
        return new java.util.HashSet<>(set);
    }

    public void addIncludedFolder(String uriString) {
        java.util.Set<String> set = getIncludedFolders();
        set.add(uriString);
        prefs.edit().putStringSet(KEY_INCLUDED_FOLDERS, set).apply();
    }

    public void removeIncludedFolder(String uriString) {
        java.util.Set<String> set = getIncludedFolders();
        if (set.remove(uriString)) {
            prefs.edit().putStringSet(KEY_INCLUDED_FOLDERS, set).apply();
        }
    }

    public void setIncludedFolders(java.util.Set<String> folders) {
        prefs.edit().putStringSet(KEY_INCLUDED_FOLDERS, new java.util.HashSet<>(folders)).apply();
    }

    // Default folder toggles (Download, Documents, Music)
    public boolean isIncludeDownloads() {
        return prefs.getBoolean(KEY_INCLUDE_DOWNLOADS, true);
    }

    public void setIncludeDownloads(boolean v) {
        prefs.edit().putBoolean(KEY_INCLUDE_DOWNLOADS, v).apply();
    }

    public boolean isIncludeDocuments() {
        return prefs.getBoolean(KEY_INCLUDE_DOCUMENTS, true);
    }

    public void setIncludeDocuments(boolean v) {
        prefs.edit().putBoolean(KEY_INCLUDE_DOCUMENTS, v).apply();
    }

    public boolean isIncludeMusic() {
        return prefs.getBoolean(KEY_INCLUDE_MUSIC, true);
    }

    public void setIncludeMusic(boolean v) {
        prefs.edit().putBoolean(KEY_INCLUDE_MUSIC, v).apply();
    }
    
    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_ANDROID);
    }
    
    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public String getWallpaperUri() {
        return prefs.getString(KEY_WALLPAPER, null);
    }

    public void setWallpaperUri(String uri) {
        prefs.edit().putString(KEY_WALLPAPER, uri).apply();
    }

    public String getWallpaperUriForTheme(String theme) {
        if (theme == null) theme = "";
        return prefs.getString(KEY_WALLPAPER_PREFIX + theme, null);
    }

    public void setWallpaperUriForTheme(String theme, String uri) {
        if (theme == null) theme = "";
        prefs.edit().putString(KEY_WALLPAPER_PREFIX + theme, uri).apply();
    }
    
    public String getSortMode() {
        return prefs.getString(KEY_SORT_MODE, SORT_ALPHABETICAL);
    }
    
    public void setSortMode(String sortMode) {
        prefs.edit().putString(KEY_SORT_MODE, sortMode).apply();
    }
}
