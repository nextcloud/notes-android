package it.niedermann.owncloud.notes.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class Notes extends Application {
    private static final String TAG = Notes.class.getCanonicalName();

    private static final String PREF_KEY_DARK_THEME = "darkTheme";
    private static final String PREF_KEY_LOCKED = "lock";
    private static boolean lockedPreference = false;
    private static boolean locked = true;

    @Override
    public void onCreate() {
        setAppTheme(getAppTheme(getApplicationContext()));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lockedPreference = prefs.getBoolean(PREF_KEY_LOCKED, false);
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
        return prefs.getBoolean(PREF_KEY_DARK_THEME, false);
    }

    public static void setLockedPreference(boolean lockedPreference) {
        Log.d(TAG, "New locked preference: " + lockedPreference);
        Notes.lockedPreference = lockedPreference;
    }

    public static boolean isLocked() {
        return lockedPreference && locked;
    }

    public static void lock() {
        locked = true;
    }

    public static void unlock() {
        locked = false;
    }
}
