package it.niedermann.owncloud.notes.edit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import androidx.preference.Preference;
import androidx.test.core.app.ApplicationProvider;
import androidx.fragment.app.testing.FragmentScenario;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.preferences.PreferencesFragment;

@RunWith(RobolectricTestRunner.class)
public class EditorFlagTest {

    @Test
    public void changePreferenceTest(){
        Assert.assertFalse(EditNoteActivity.getIfKeepScreenOn());
        Context context = ApplicationProvider.getApplicationContext();
        String key = context.getString(R.string.pref_key_keep_screen_on);
        FragmentScenario<PreferencesFragment> fragment = FragmentScenario.launch(PreferencesFragment.class);
        fragment.onFragment(frag -> {
            Preference preference = frag.getPreferenceScreen().findPreference(key);
            Assert.assertNotNull(preference);
            preference.callChangeListener(true);
            Assert.assertTrue(EditNoteActivity.getIfKeepScreenOn());
        });
    }

    @Test
    public void defaultLunchTest(){
        Assert.assertFalse(EditNoteActivity.getIfKeepScreenOn());
    }
}
