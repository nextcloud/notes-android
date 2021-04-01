package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public class Migration_19_20 {

    /**
     * Removes <code>branding</code> from {@link SharedPreferences} because we do no longer allow to disable it.
     *
     * @param context {@link Context}
     */
    public Migration_19_20(@NonNull Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("branding").apply();
    }
}
