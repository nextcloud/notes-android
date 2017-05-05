package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import it.niedermann.owncloud.notes.android.fragment.PreferencesFragment;

/**
 * Allows to change application settings.
 */

public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();
    }
}
