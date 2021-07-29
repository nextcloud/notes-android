package it.niedermann.owncloud.notes.edit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Objects;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerListener;
import it.niedermann.owncloud.notes.databinding.ActivityEditBinding;
import it.niedermann.owncloud.notes.databinding.ActivityEditBinding;
import it.niedermann.owncloud.notes.edit.category.CategoryViewModel;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;

public class EditNoteActivity extends LockedActivity implements BaseNoteFragment.NoteFragmentListener, AccountPickerListener {

    private static final String TAG = EditNoteActivity.class.getSimpleName();

    public static final String ACTION_SHORTCUT = "it.niedermann.owncloud.notes.shortcut";
    private static final String INTENT_GOOGLE_ASSISTANT = "com.google.android.gm.action.AUTO_SEND";
    private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_FAVORITE = "favorite";

    private CategoryViewModel categoryViewModel;
    private ActivityEditBinding binding;

    private BaseNoteFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (SingleAccountHelper.getCurrentSingleSignOnAccount(this) == null) {
                throw new NoCurrentAccountSelectedException();
            }
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Toast.makeText(this, R.string.no_account_configured_yet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            launchNoteFragment();
        } else {
            fragment = (BaseNoteFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        }

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setOnClickListener((v) -> fragment.showEditTitleDialog());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent.getLongExtra(PARAM_NOTE_ID, 0));
        setIntent(intent);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            fragment = null;
        }
        launchNoteFragment();
    }

    private long getNoteId() {
        return getIntent().getLongExtra(PARAM_NOTE_ID, 0);
    }

    private long getAccountId() {
        return getIntent().getLongExtra(PARAM_ACCOUNT_ID, 0);
    }

    /**
     * Starts the note fragment for an existing note or a new note.
     * The actual behavior is triggered by the activity's intent.
     */
    private void launchNoteFragment() {
        long noteId = getNoteId();
        if (noteId > 0) {
            launchExistingNote(getAccountId(), noteId);
        } else {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                launchReadonlyNote();
            } else {
                launchNewNote();
            }
        }
    }

    /**
     * Starts a {@link NoteEditFragment} or {@link NotePreviewFragment} for an existing note.
     * The type of fragment (view-mode) is chosen based on the user preferences.
     *
     * @param noteId ID of the existing note.
     */
    private void launchExistingNote(long accountId, long noteId) {
        final var prefKeyNoteMode = getString(R.string.pref_key_note_mode);
        final var prefKeyLastMode = getString(R.string.pref_key_last_note_mode);
        final var prefValueEdit = getString(R.string.pref_value_mode_edit);
        final var prefValuePreview = getString(R.string.pref_value_mode_preview);
        final var prefValueLast = getString(R.string.pref_value_mode_last);

        final var preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String mode = preferences.getString(prefKeyNoteMode, prefValueEdit);
        final String lastMode = preferences.getString(prefKeyLastMode, prefValueEdit);
        boolean editMode = true;
        if (prefValuePreview.equals(mode) || (prefValueLast.equals(mode) && prefValuePreview.equals(lastMode))) {
            editMode = false;
        }
        launchExistingNote(accountId, noteId, editMode);
    }

    /**
     * Starts a {@link NoteEditFragment} or {@link NotePreviewFragment} for an existing note.
     *
     * @param noteId ID of the existing note.
     * @param edit   View-mode of the fragment:
     *               <code>true</code> for {@link NoteEditFragment},
     *               <code>false</code> for {@link NotePreviewFragment}.
     */
    private void launchExistingNote(long accountId, long noteId, boolean edit) {
        // save state of the fragment in order to resume with the same note and originalNote
        Fragment.SavedState savedState = null;
        if (fragment != null) {
            savedState = getSupportFragmentManager().saveFragmentInstanceState(fragment);
        }
        fragment = edit
                ? NoteEditFragment.newInstance(accountId, noteId)
                : NotePreviewFragment.newInstance(accountId, noteId);

        if (savedState != null) {
            fragment.setInitialSavedState(savedState);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, fragment).commit();
    }

    /**
     * Starts the {@link NoteEditFragment} with a new note.
     * Content ("share" functionality), category and favorite attribute can be preset.
     */
    private void launchNewNote() {
        final var intent = getIntent();

        String categoryTitle = "";
        boolean favorite = false;
        if (intent.hasExtra(PARAM_CATEGORY)) {
            final NavigationCategory categoryPreselection = (NavigationCategory) Objects.requireNonNull(intent.getSerializableExtra(PARAM_CATEGORY));
            final String category = categoryPreselection.getCategory();
            if(category != null) {
                categoryTitle = category;
            }
            favorite = categoryPreselection.getType() == FAVORITES;
        }

        String content = "";
        if (
                intent.hasExtra(Intent.EXTRA_TEXT) &&
                        MIMETYPE_TEXT_PLAIN.equals(intent.getType()) &&
                        (Intent.ACTION_SEND.equals(intent.getAction()) ||
                                INTENT_GOOGLE_ASSISTANT.equals(intent.getAction()))
        ) {
            content = ShareUtil.extractSharedText(intent);
        } else if (intent.hasExtra(PARAM_CONTENT)) {
            content = intent.getStringExtra(PARAM_CONTENT);
        }

        if (content == null) {
            content = "";
        }
        final var newNote = new Note(null, Calendar.getInstance(), NoteUtil.generateNonEmptyNoteTitle(content, this), content, categoryTitle, favorite, null);
        fragment = NoteEditFragment.newInstanceWithNewNote(newNote);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, fragment).commit();
    }

    private void launchReadonlyNote() {
        final var intent = getIntent();
        final var content = new StringBuilder();
        try {
            final var inputStream = getContentResolver().openInputStream(Objects.requireNonNull(intent.getData()));
            final var bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fragment = NoteReadonlyFragment.newInstance(content.toString());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            close();
            return true;
        } else if (itemId == R.id.menu_preview) {
            launchExistingNote(getAccountId(), getNoteId(), false);
            return true;
        } else if (itemId == R.id.menu_edit) {
            launchExistingNote(getAccountId(), getNoteId(), true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Send result and closes the Activity
     */
    public void close() {
        /* TODO enhancement: store last mode in note
         * for cross device functionality per note mode should be stored on the server.
         */
        final var preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String prefKeyLastMode = getString(R.string.pref_key_last_note_mode);
        if (fragment instanceof NoteEditFragment) {
            preferences.edit().putString(prefKeyLastMode, getString(R.string.pref_value_mode_edit)).apply();
        } else {
            preferences.edit().putString(prefKeyLastMode, getString(R.string.pref_value_mode_preview)).apply();
        }
        fragment.onCloseNote();
        finish();
    }

    @Override
    public void onNoteUpdated(Note note) {
        if (note != null) {
            binding.toolbar.setTitle(note.getTitle());
            if (TextUtils.isEmpty(note.getCategory())) {
                binding.toolbar.setSubtitle(null);
            } else {
                binding.toolbar.setSubtitle(NoteUtil.extendCategory(note.getCategory()));
            }
        }
    }

    @Override
    public void onAccountPicked(@NonNull Account account) {
        fragment.moveNote(account);
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.appBar, binding.toolbar);
    }
}