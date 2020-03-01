package it.niedermann.owncloud.notes.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import it.niedermann.owncloud.notes.android.DarkModeSetting;

public class Notes extends Application {
    private static final String DARK_THEME = "darkTheme";

    @Override
    public void onCreate() {
        setAppTheme(getAppTheme(getApplicationContext()));
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
}
