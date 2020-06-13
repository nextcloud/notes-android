package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.PreferencesFragment;
import it.niedermann.owncloud.notes.databinding.ActivityPreferencesBinding;

/**
 * Allows to change application settings.
 */

public class PreferencesActivity extends LockedActivity {

    private ActivityPreferencesBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setResult(RESULT_CANCELED);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new PreferencesFragment())
                .commit();
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.toolbar);
    }
}
