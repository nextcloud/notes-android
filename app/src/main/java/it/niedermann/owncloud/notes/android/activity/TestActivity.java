package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import it.niedermann.owncloud.notes.R;

/**
 * Created by dbailey on 18/08/2017.
 */

public class TestActivity extends NotesListViewActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 //       setContentView(R.layout.activity_notes_list_view);

   //     getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_select_single_note);

        getSupportActionBar().setTitle(R.string.activity_select_single_note);
    }

    @Override
    public void onNoteClick(int position, View v) { }

    @Override
    public boolean onNoteLongClick(int position, View v) { return false; }

    @Override
    public void onNoteFavoriteClick(int position, View view) { }
}
