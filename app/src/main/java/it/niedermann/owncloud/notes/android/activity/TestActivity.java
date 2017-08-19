package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;

import static android.R.string.cancel;

/**
 * Created by dbailey on 18/08/2017.
 */

public class TestActivity extends NotesListViewActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 //       setContentView(R.layout.activity_notes_list_view);

   //     getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_select_single_note);

        findViewById(R.id.fab_create).setVisibility(View.INVISIBLE);

        getSupportActionBar().setTitle(R.string.activity_select_single_note);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return true; }

    @Override
    public void onNoteClick(int position, View v) {

        ItemAdapter adapter = new ItemAdapter(this);

        Log.d(TAG, "onNoteClick: " + position);
        Item item = adapter.getItem(position);
        Log.d(TAG, "onNoteClick: " + (DBNote) item);
    }

    @Override
    public boolean onNoteLongClick(int position, View v) { return false; }

    @Override
    public void onNoteFavoriteClick(int position, View view) { }
}
