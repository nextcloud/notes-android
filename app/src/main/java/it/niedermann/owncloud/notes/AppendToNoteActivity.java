package it.niedermann.owncloud.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.LiveData;

import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;

public class AppendToNoteActivity extends MainActivity {

    private static final String TAG = AppendToNoteActivity.class.getSimpleName();

    String receivedText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent receivedIntent = getIntent();
        receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        @Nullable final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.append_to_note);
        } else {
            Log.e(TAG, "SupportActionBar is null. Expected toolbar to be present to set a title.");
        }
        binding.activityNotesListView.toolbar.setSubtitle(receivedText);
    }

    @Override
    public void onNoteClick(int position, View v) {
        if (receivedText != null && receivedText.length() > 0) {
            final LiveData<NoteWithCategory> fullNote$ = mainViewModel.getFullNoteWithCategory(((NoteWithCategory) adapter.getItem(position)).getId());
            fullNote$.observe(this, (fullNote) -> {
                fullNote$.removeObservers(this);
                final String oldContent = fullNote.getContent();
                String newContent;
                if (oldContent != null && oldContent.length() > 0) {
                    newContent = oldContent + "\n\n" + receivedText;
                } else {
                    newContent = receivedText;
                }
                LiveData<Void> updateLiveData = mainViewModel.updateNoteAndSync(fullNote, newContent, null);
                updateLiveData.observe(this, (next) -> {
                    Toast.makeText(this, getString(R.string.added_content, receivedText), Toast.LENGTH_SHORT).show();
                    updateLiveData.removeObservers(this);
                });
            });
        } else {
            Toast.makeText(this, R.string.shared_text_empty, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
