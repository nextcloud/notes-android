package it.niedermann.owncloud.notes.persistence.sharedprefs;

import android.content.SharedPreferences;

/**
 * https://stackoverflow.com/a/57074217
 */
public class SharedPreferenceIntLiveData extends SharedPreferenceLiveData<Integer> {

    public SharedPreferenceIntLiveData(SharedPreferences prefs, String key, Integer defValue) {
        super(prefs, key, defValue);
    }

    @Override
    Integer getValueFromPreferences(String key, Integer defValue) {
        return sharedPrefs.getInt(key, defValue);
    }

}