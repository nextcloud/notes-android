/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit;

import static androidx.core.view.ViewCompat.isAttachedToWindow;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.FragmentNoteEditBinding;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.util.DisplayUtils;

public class NoteEditFragment extends SearchableBaseNoteFragment {

    private static final String TAG = NoteEditFragment.class.getSimpleName();

    private static final String LOG_TAG_AUTOSAVE = "AutoSave";

    private static final long DELAY = 2000; // Wait for this time after typing before saving
    private static final long DELAY_AFTER_SYNC = 5000; // Wait for this time after saving before checking for next save

    private FragmentNoteEditBinding binding;

    private Handler handler;
    private boolean saveActive;
    private boolean unsavedEdit;
    private final Runnable runAutoSave = new Runnable() {
        @Override
        public void run() {
            if (unsavedEdit) {
                Log.d(LOG_TAG_AUTOSAVE, "runAutoSave: start AutoSave");
                autoSave();
            } else {
                Log.d(LOG_TAG_AUTOSAVE, "runAutoSave: nothing changed");
            }
        }
    };
    private TextWatcher textWatcher;
    private boolean keyboardShown = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(false);
        menu.findItem(R.id.menu_preview).setVisible(true);
    }

    @Override
    public ScrollView getScrollView() {
        return binding.scrollView;
    }

    @Override
    protected void scrollToY(int y) {
        if (binding != null) {
            binding.scrollView.post(() -> binding.scrollView.setScrollY(y));
        }
    }

    @Override
    protected Layout getLayout() {
        binding.editContent.onPreDraw();
        return binding.editContent.getLayout();
    }

    @Override
    protected FloatingActionButton getSearchNextButton() {
        return binding.searchNext;
    }

    @Override
    protected FloatingActionButton getSearchPrevButton() {
        return binding.searchPrev;
    }

    @Override
    protected @NonNull ExtendedFloatingActionButton getDirectEditingButton() {
        return binding.directEditing;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNoteEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do here...
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do here...
            }

            @Override
            public void afterTextChanged(final Editable s) {
                unsavedEdit = true;
                if (!saveActive) {
                    handler.removeCallbacks(runAutoSave);
                    handler.postDelayed(runAutoSave, DELAY);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.editContent.addTextChangedListener(textWatcher);

        if (keyboardShown) {
            openSoftKeyboard();
        }
    }

    @Override
    protected void onNoteLoaded(Note note) {
        super.onNoteLoaded(note);
        if (TextUtils.isEmpty(note.getContent())) {
            openSoftKeyboard();
        }

        binding.editContent.setMarkdownString(note.getContent());
        binding.editContent.setEnabled(true);

        final var sp = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
        binding.editContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(requireContext(), sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.editContent.setTypeface(Typeface.MONOSPACE);
        }
    }

    private void openSoftKeyboard() {
        binding.editContent.postDelayed(() -> {
            binding.editContent.requestFocus();

            final var imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.editContent, InputMethodManager.SHOW_IMPLICIT);
            } else {
                Log.e(TAG, InputMethodManager.class.getSimpleName() + " is null.");
            }
            //Without a small delay the keyboard does not show reliably
        }, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.editContent.removeTextChangedListener(textWatcher);
        cancelTimers();

        final ViewGroup parentView = requireActivity().findViewById(android.R.id.content);
        if (parentView != null && parentView.getChildCount() > 0) {
            keyboardShown = DisplayUtils.isSoftKeyboardVisible(parentView.getChildAt(0));
        } else {
            keyboardShown = false;
        }
    }

    private void cancelTimers() {
        handler.removeCallbacks(runAutoSave);
    }

    /**
     * Gets the current content of the EditText field in the UI.
     *
     * @return String of the current content.
     */
    @Override
    protected String getContent() {
        final var editable = binding.editContent.getText();
        return editable == null ? "" : editable.toString();
    }

    @Override
    protected void saveNote(@Nullable ISyncCallback callback) {
        super.saveNote(callback);
        unsavedEdit = false;
    }

    /**
     * Saves the current changes and show the status in the ActionBar
     */
    private void autoSave() {
        Log.d(LOG_TAG_AUTOSAVE, "STARTAUTOSAVE");
        saveActive = true;
        saveNote(new ISyncCallback() {
            @Override
            public void onFinish() {
                onSaved();
            }

            @Override
            public void onScheduled() {
                onSaved();
            }

            private void onSaved() {
                // AFTER SYNCHRONIZATION
                Log.d(LOG_TAG_AUTOSAVE, "FINISHED AUTOSAVE");
                saveActive = false;

                // AFTER "DELAY_AFTER_SYNC" SECONDS: allow next auto-save or start it directly
                handler.postDelayed(runAutoSave, DELAY_AFTER_SYNC);

            }
        });
    }

    @Override
    protected void colorWithText(@NonNull String newText, @Nullable Integer current, int color) {
        if (binding != null && isAttachedToWindow(binding.editContent)) {
            binding.editContent.clearFocus();
            binding.editContent.setSearchText(newText, current);
        }
    }

    @Override
    public void applyBrand(int color) {
        super.applyBrand(color);

        final var util = BrandingUtil.of(color, requireContext());
        binding.editContent.setSearchColor(color);
        binding.editContent.setHighlightColor(util.notes.getTextHighlightBackgroundColor(requireContext(), color, colorPrimary, colorAccent));
    }

    public static BaseNoteFragment newInstance(long accountId, long noteId) {
        final var fragment = new NoteEditFragment();
        final var args = new Bundle();
        args.putLong(PARAM_NOTE_ID, noteId);
        args.putLong(PARAM_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    public static BaseNoteFragment newInstanceWithNewNote(Note newNote) {
        final var fragment = new NoteEditFragment();
        final var args = new Bundle();
        args.putSerializable(PARAM_NEWNOTE, newNote);
        fragment.setArguments(args);
        return fragment;
    }
}
