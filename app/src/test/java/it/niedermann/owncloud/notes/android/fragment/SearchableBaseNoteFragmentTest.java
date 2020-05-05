package it.niedermann.owncloud.notes.android.fragment;

import it.niedermann.owncloud.notes.android.fragment.SearchableBaseNoteFragment;

import android.util.Log;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SearchableBaseNoteFragmentTest {
    @Test
    public void testCountOccurrences() {
        try {
            Method method = SearchableBaseNoteFragment.class.getDeclaredMethod("countOccurrences", String.class, String.class);
            method.setAccessible(true);



        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
            Log.e("Test_12_getCategoryIdByTitle", Arrays.toString(e.getStackTrace()));
        }
    }
}