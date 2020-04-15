package it.niedermann.owncloud.notes.android.appwidget;

import android.content.SharedPreferences;

import it.niedermann.owncloud.notes.android.DarkModeSetting;

final class NoteWidgetHelper {
    private NoteWidgetHelper() {
        // Helper class for static methods
    }

    @SuppressWarnings("WeakerAccess") //Making it package-private would generate a warning in PMD
    public static DarkModeSetting getDarkThemeSetting(SharedPreferences prefs, String darkModeKey, int appWidgetId) {
        try {
            String themeName = prefs.getString(darkModeKey + appWidgetId, DarkModeSetting.SYSTEM_DEFAULT.name());
            return DarkModeSetting.valueOf(themeName);
        } catch (ClassCastException e) {
            //DARK_THEME was a boolean in older versions of the app. We thereofre have to still support the old setting.
            boolean isDarkTheme = prefs.getBoolean(darkModeKey + appWidgetId, false);
            return isDarkTheme ? DarkModeSetting.DARK : DarkModeSetting.LIGHT;
        }
    }
}
