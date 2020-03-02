package it.niedermann.owncloud.notes.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import it.niedermann.owncloud.notes.android.DarkModeSetting;

public class Notes extends Application {
    private static final String TAG = Notes.class.getCanonicalName();

    private static final String DARK_THEME = "darkTheme";
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

    public static void setAppTheme(DarkModeSetting setting) {
        AppCompatDelegate.setDefaultNightMode(setting.getModeId());
    }

    public static DarkModeSetting getAppTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String mode;
        try {
            mode = prefs.getString(DARK_THEME, DarkModeSetting.SYSTEM_DEFAULT.name());
        } catch (ClassCastException e) {
            boolean darkModeEnabled = prefs.getBoolean(DARK_THEME, false);
            mode = darkModeEnabled ? DarkModeSetting.DARK.name() : DarkModeSetting.LIGHT.name();
        }
        return DarkModeSetting.valueOf(mode);
    }

    public static boolean isDarkThemeActive(Context context, DarkModeSetting setting) {
        if (setting == DarkModeSetting.SYSTEM_DEFAULT) {
            return isDarkThemeActive(context);
        } else {
            return setting == DarkModeSetting.DARK;
        }
    }

    public static boolean isDarkThemeActive(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
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
