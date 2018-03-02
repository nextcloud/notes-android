package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;

public abstract class BaseNoteFragment extends Fragment implements CategoryDialogFragment.CategoryDialogListener {

    public interface NoteFragmentListener {
        void close();
        void onNoteUpdated(DBNote note);
    }

    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_NEWNOTE = "newNote";
    private static final String SAVEDKEY_NOTE = "note";
    private static final String SAVEDKEY_ORIGINAL_NOTE = "original_note";

    protected DBNote note;
    @Nullable
    private DBNote originalNote;
    private NoteSQLiteOpenHelper db;
    private NoteFragmentListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            long id = getArguments().getLong(PARAM_NOTE_ID);
            if(id>0) {
                note = originalNote = db.getNote(id);
            } else {
                CloudNote cloudNote = (CloudNote) getArguments().getSerializable(PARAM_NEWNOTE);
                if(cloudNote == null) {
                    throw new IllegalArgumentException(PARAM_NOTE_ID + " is not given and argument " + PARAM_NEWNOTE +" is missing.");
                }
                note = db.getNote(db.addNoteAndSync(cloudNote));
                originalNote = null;
            }
        } else {
            note = (DBNote) savedInstanceState.getSerializable(SAVEDKEY_NOTE);
            originalNote = (DBNote) savedInstanceState.getSerializable(SAVEDKEY_ORIGINAL_NOTE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (NoteFragmentListener) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.getClass()+" must implement "+NoteFragmentListener.class);
        }
        db = NoteSQLiteOpenHelper.getInstance(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.onNoteUpdated(note);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVEDKEY_NOTE, note);
        outState.putSerializable(SAVEDKEY_ORIGINAL_NOTE, originalNote);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_fragment, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem itemFavorite = menu.findItem(R.id.menu_favorite);
        prepareFavoriteOption(itemFavorite);
    }

    private void prepareFavoriteOption(MenuItem item) {
        item.setIcon(note.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_outline_white_24dp);
        item.setChecked(note.isFavorite());
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                if(originalNote == null) {
                    db.deleteNoteAndSync(note.getId());
                } else {
                    db.updateNoteAndSync(originalNote, null, null);
                }
                listener.close();
                return true;
            case R.id.menu_delete:
                db.deleteNoteAndSync(note.getId());
                listener.close();
                return true;
            case R.id.menu_favorite:
                db.toggleFavorite(note, null);
                listener.onNoteUpdated(note);
                prepareFavoriteOption(item);
                return true;
            case R.id.menu_category:
                showCategorySelector();
                return true;
            case R.id.menu_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, note.getContent());

                ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                actionProvider.setShareIntent(shareIntent);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onPrepareClose() {
        saveNote(null);
    }

    public void onFinalClose() {
        if(originalNote==null && getContent().isEmpty()) {
            db.deleteNoteAndSync(note.getId());
        }
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */
    protected void saveNote(@Nullable ICallback callback) {
        Log.d(getClass().getSimpleName(), "saveData()");
        note = db.updateNoteAndSync(note, getContent(), callback);
        listener.onNoteUpdated(note);
    }
    protected abstract String getContent();

    /**
     * Opens a dialog in order to chose a category
     */
    private void showCategorySelector() {
        final String fragmentId = "fragment_category";
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag(fragmentId);
        if(frag!=null) {
            manager.beginTransaction().remove(frag).commit();
        }
        Bundle arguments = new Bundle();
        arguments.putString(CategoryDialogFragment.PARAM_CATEGORY, note.getCategory());
        CategoryDialogFragment categoryFragment = new CategoryDialogFragment();
        categoryFragment.setArguments(arguments);
        categoryFragment.setTargetFragment(this, 0);
        categoryFragment.show(manager, fragmentId);
    }

    @Override
    public void onCategoryChosen(String category) {
        db.setCategory(note, category, null);
        listener.onNoteUpdated(note);
    }
}
