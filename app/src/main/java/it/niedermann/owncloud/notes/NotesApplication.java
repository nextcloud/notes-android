package it.niedermann.owncloud.notes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import it.niedermann.owncloud.notes.preferences.DarkModeSetting;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class NotesApplication extends Application {
    private static final String TAG = NotesApplication.class.getSimpleName();

    private static final long LOCK_TIME = 30 * 1000;
    private static boolean lockedPreference = false;
    private static boolean isLocked = true;
    private static long lastInteraction = 0;
    private static String PREF_KEY_THEME;
    private static boolean isGridViewEnabled = false;

    @Override
    public void onCreate() {
        PREF_KEY_THEME = getString(R.string.pref_key_theme);
        setAppTheme(getAppTheme(getApplicationContext()));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lockedPreference = prefs.getBoolean(getString(R.string.pref_key_lock), false);
        isGridViewEnabled = getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_gridview), false);
        super.onCreate();
    }

    public static void setAppTheme(DarkModeSetting setting) {
        AppCompatDelegate.setDefaultNightMode(setting.getModeId());
    }

    public static boolean isGridViewEnabled() {
        return isGridViewEnabled;
    }

    public static void updateGridViewEnabled(boolean gridView) {
        isGridViewEnabled = gridView;
    }

    public static DarkModeSetting getAppTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String mode;
        try {
            mode = prefs.getString(PREF_KEY_THEME, DarkModeSetting.SYSTEM_DEFAULT.name());
        } catch (ClassCastException e) {
            boolean darkModeEnabled = prefs.getBoolean(PREF_KEY_THEME, false);
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
        Log.i(TAG, "New locked preference: " + lockedPreference);
        NotesApplication.lockedPreference = lockedPreference;
    }

    public static boolean isLocked() {
        if (!isLocked && System.currentTimeMillis() > (LOCK_TIME + lastInteraction)) {
            isLocked = true;
        }
        return lockedPreference && isLocked;
    }

    public static void unlock() {
        isLocked = false;
    }

    public static void updateLastInteraction() {
        lastInteraction = System.currentTimeMillis();
    }
}
