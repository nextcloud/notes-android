package it.niedermann.owncloud.notes.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

public class Notes extends Application {
    private static final String DARK_THEME = "darkTheme";
    private static boolean locked = true;

    @Override
    public void onCreate() {
        setAppTheme(getAppTheme(getApplicationContext()));
        super.onCreate();
    }

    public static void setAppTheme(Boolean darkTheme) {
        if (darkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static boolean getAppTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(DARK_THEME, false);
    }

    public static boolean isLocked() {
        return locked;
    }

    public static void lock() {
        locked = true;
    }

    public static void unlock() {
        locked = false;
    }
}
