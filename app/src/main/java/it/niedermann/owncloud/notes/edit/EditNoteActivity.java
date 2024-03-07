/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit;

import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Objects;

import it.niedermann.android.sharedpreferences.SharedPreferenceBooleanLiveData;
import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerListener;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityEditBinding;
import it.niedermann.owncloud.notes.edit.category.CategoryViewModel;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

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
    private NotesRepository repo;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = NotesRepository.getInstance(getApplicationContext());

        try {
            if (SingleAccountHelper.getCurrentSingleSignOnAccount(this) == null) {
                throw new NoCurrentAccountSelectedException(this);
            }
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            Toast.makeText(this, R.string.no_account_configured_yet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final var preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        new SharedPreferenceBooleanLiveData(preferences, getString(R.string.pref_key_keep_screen_on), true).observe(this, keepScreenOn -> {
            if (keepScreenOn) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });


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

    @Override
    protected void onStop() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }

    private long getNoteId() {
        return getIntent().getLongExtra(PARAM_NOTE_ID, 0);
    }

    private long getAccountId() {
        final long idParam = getIntent().getLongExtra(PARAM_ACCOUNT_ID, 0);
        if (idParam == 0) {
            try {
                final SingleSignOnAccount ssoAcc = SingleAccountHelper.getCurrentSingleSignOnAccount(this);
                return repo.getAccountByName(ssoAcc.name).getId();
            } catch (NextcloudFilesAppAccountNotFoundException |
                     NoCurrentAccountSelectedException e) {
                Log.w(TAG, "getAccountId: no current account", e);
            }
        }
        return idParam;
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
        launchExistingNote(accountId, noteId, null);
    }

    private void launchExistingNote(long accountId, long noteId, @Nullable final String mode) {
        launchExistingNote(accountId, noteId, mode, false);
    }

    /**
     * Starts a {@link NoteEditFragment} or {@link NotePreviewFragment} for an existing note.
     *
     * @param noteId       ID of the existing note.
     * @param mode         View-mode of the fragment (pref value or null). If null will be chosen based on
     *                     user preferences.
     * @param discardState If true, the state of the fragment will be discarded and a new fragment will be created
     */
    private void launchExistingNote(long accountId, long noteId, @Nullable final String mode, final boolean discardState) {
        // save state of the fragment in order to resume with the same note and originalNote
        runOnUiThread(() -> {
            Fragment.SavedState savedState = null;
            if (fragment != null && !discardState) {
                savedState = getSupportFragmentManager().saveFragmentInstanceState(fragment);
            }
            fragment = getNoteFragment(accountId, noteId, mode);
            if (savedState != null) {
                fragment.setInitialSavedState(savedState);
            }
            replaceFragment();
        });
    }

    private void replaceFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, fragment).commit();
        if (!fragment.shouldShowToolbar()) {
            binding.toolbar.setVisibility(View.GONE);
        } else {
            binding.toolbar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Returns the preferred mode for the account. If the mode is "remember last" the last mode is returned.
     * If the mode is "direct edit" and the account does not support direct edit, the default mode is returned.
     */
    private String getPreferenceMode(long accountId) {

        final var prefKeyNoteMode = getString(R.string.pref_key_note_mode);
        final var prefKeyLastMode = getString(R.string.pref_key_last_note_mode);
        final var defaultMode = getString(R.string.pref_value_mode_edit);
        final var prefValueLast = getString(R.string.pref_value_mode_last);
        final var prefValueDirectEdit = getString(R.string.pref_value_mode_direct_edit);


        final var preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String modePreference = preferences.getString(prefKeyNoteMode, defaultMode);

        String effectiveMode = modePreference;
        if (modePreference.equals(prefValueLast)) {
            effectiveMode = preferences.getString(prefKeyLastMode, defaultMode);
        }

        if (effectiveMode.equals(prefValueDirectEdit)) {
            final Account accountById = repo.getAccountById(accountId);
            final var directEditAvailable = accountById != null && accountById.isDirectEditingAvailable();
            if (!directEditAvailable) {
                effectiveMode = defaultMode;
            }
        }

        return effectiveMode;
    }

    private BaseNoteFragment getNoteFragment(long accountId, long noteId, final @Nullable String modePref) {

        final var effectiveMode = modePref == null ? getPreferenceMode(accountId) : modePref;

        final var prefValueEdit = getString(R.string.pref_value_mode_edit);
        final var prefValueDirectEdit = getString(R.string.pref_value_mode_direct_edit);
        final var prefValuePreview = getString(R.string.pref_value_mode_preview);

        if (effectiveMode.equals(prefValueEdit)) {
            return NoteEditFragment.newInstance(accountId, noteId);
        } else if (effectiveMode.equals(prefValueDirectEdit)) {
            return NoteDirectEditFragment.newInstance(accountId, noteId);
        } else if (effectiveMode.equals(prefValuePreview)) {
            return NotePreviewFragment.newInstance(accountId, noteId);
        } else {
            throw new IllegalStateException("Unknown note modePref: " + modePref);
        }
    }


    @NonNull
    private BaseNoteFragment getNewNoteFragment(Note newNote) {
        final var mode = getPreferenceMode(getAccountId());

        final var prefValueDirectEdit = getString(R.string.pref_value_mode_direct_edit);

        if (mode.equals(prefValueDirectEdit)) {
            return NoteDirectEditFragment.newInstanceWithNewNote(newNote);
        } else {
            return NoteEditFragment.newInstanceWithNewNote(newNote);
        }
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
        fragment = getNewNoteFragment(newNote);
        replaceFragment();
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
        replaceFragment();
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
            changeMode(Mode.PREVIEW, false);
            return true;
        } else if (itemId == R.id.menu_edit) {
            changeMode(Mode.EDIT, false);
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
        } else if (fragment instanceof NotePreviewFragment) {
            preferences.edit().putString(prefKeyLastMode, getString(R.string.pref_value_mode_preview)).apply();
        } else if (fragment instanceof NoteDirectEditFragment) {
            preferences.edit().putString(prefKeyLastMode, getString(R.string.pref_value_mode_direct_edit)).apply();
        }
        fragment.onCloseNote();

        if(isTaskRoot()) {
            Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            finish();
        }

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
    public void changeMode(@NonNull Mode mode, boolean reloadNote) {
        switch (mode) {
            case EDIT -> launchExistingNote(getAccountId(), getNoteId(), getString(R.string.pref_value_mode_edit), reloadNote);
            case PREVIEW -> launchExistingNote(getAccountId(), getNoteId(), getString(R.string.pref_value_mode_preview), reloadNote);
            case DIRECT_EDIT -> launchExistingNote(getAccountId(), getNoteId(), getString(R.string.pref_value_mode_direct_edit), reloadNote);
            default -> throw new IllegalStateException("Unknown mode: " + mode);
        }
    }


    @Override
    public void onAccountPicked(@NonNull Account account) {
        fragment.moveNote(account);
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.platform.themeStatusBar(this);
        util.material.themeToolbar(binding.toolbar);
    }
}
