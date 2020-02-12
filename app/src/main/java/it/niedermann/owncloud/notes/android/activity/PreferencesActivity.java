package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.nextcloud.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.PreferencesFragment;

/**
 * Allows to change application settings.
 */

public class PreferencesActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this, ExceptionActivity.class));
        setContentView(R.layout.activity_preferences);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setResult(RESULT_CANCELED);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new PreferencesFragment())
                .commit();
    }
}
