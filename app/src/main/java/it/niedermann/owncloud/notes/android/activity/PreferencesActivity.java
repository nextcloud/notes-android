package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import it.niedermann.nextcloud.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.PreferencesFragment;
import it.niedermann.owncloud.notes.databinding.ActivityPreferencesBinding;

/**
 * Allows to change application settings.
 */

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        ActivityPreferencesBinding binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setResult(RESULT_CANCELED);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new PreferencesFragment())
                .commit();
    }
}
