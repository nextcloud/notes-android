package it.niedermann.owncloud.notes.edit;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandedFragment;
import it.niedermann.owncloud.notes.edit.details.NoteDetailsDialogFragment;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;

import static it.niedermann.owncloud.notes.NotesApplication.isDarkThemeActive;

public abstract class BaseNoteFragment extends BrandedFragment implements NoteDetailsDialogFragment.NoteDetailsListener {

    private static final String TAG = BaseNoteFragment.class.getSimpleName();
    protected final ExecutorService executor = Executors.newCachedThreadPool();

    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_NEWNOTE = "newNote";
    private static final String SAVEDKEY_NOTE = "note";
    private static final String SAVEDKEY_ORIGINAL_NOTE = "original_note";

    private Account localAccount;

    protected Note note;
    // TODO do we really need this? The reference to note is currently the same
    @Nullable
    private Note originalNote;
    private int originalScrollY;
    protected NotesRepository repo;
    private NoteFragmentListener listener;
    private boolean titleModified = false;

    protected boolean isNew = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (NoteFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement " + NoteFragmentListener.class);
        }
        repo = NotesRepository.getInstance(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executor.submit(() -> {
            try {
                SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext().getApplicationContext());
                this.localAccount = repo.getAccountByName(ssoAccount.name);

                if (savedInstanceState == null) {
                    long id = requireArguments().getLong(PARAM_NOTE_ID);
                    if (id > 0) {
                        long accountId = requireArguments().getLong(PARAM_ACCOUNT_ID);
                        if (accountId > 0) {
                            /* Switch account if account id has been provided */
                            this.localAccount = repo.getAccountById(accountId);
                            SingleAccountHelper.setCurrentAccount(requireContext().getApplicationContext(), localAccount.getAccountName());
                        }
                        isNew = false;
                        note = originalNote = repo.getNoteById(id);
                        requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                        requireActivity().invalidateOptionsMenu();
                    } else {
                        Note cloudNote = (Note) requireArguments().getSerializable(PARAM_NEWNOTE);
                        String content = requireArguments().getString(PARAM_CONTENT);
                        if (cloudNote == null) {
                            if (content == null) {
                                throw new IllegalArgumentException(PARAM_NOTE_ID + " is not given, argument " + PARAM_NEWNOTE + " is missing and " + PARAM_CONTENT + " is missing.");
                            } else {
                                note = new Note(-1, null, Calendar.getInstance(), NoteUtil.generateNoteTitle(content), content, getString(R.string.category_readonly), false, null, DBStatus.VOID, -1, "", 0);
                                requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                                requireActivity().invalidateOptionsMenu();
                            }
                        } else {
                            note = repo.addNote(localAccount.getId(), cloudNote);
                            originalNote = null;
                            requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                            requireActivity().invalidateOptionsMenu();
                        }
                    }
                } else {
                    note = (Note) savedInstanceState.getSerializable(SAVEDKEY_NOTE);
                    originalNote = (Note) savedInstanceState.getSerializable(SAVEDKEY_ORIGINAL_NOTE);
                    requireActivity().runOnUiThread(() -> onNoteLoaded(note));
                    requireActivity().invalidateOptionsMenu();
                }
            } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                e.printStackTrace();
            }
        });
        setHasOptionsMenu(true);
    }

    @Nullable
    protected abstract ScrollView getScrollView();

    protected abstract void scrollToY(int scrollY);

    @Override
    public void onResume() {
        super.onResume();
        listener.onNoteUpdated(note);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveNote(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveNote(null);
        outState.putSerializable(SAVEDKEY_NOTE, note);
        outState.putSerializable(SAVEDKEY_ORIGINAL_NOTE, originalNote);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_cancel) {
            executor.submit(() -> {
                if (originalNote == null) {
                    repo.deleteNoteAndSync(localAccount, note.getId());
                } else {
                    repo.updateNoteAndSync(localAccount, originalNote, null, null, null);
                }
            });
            listener.close();
            return true;
        } else if (itemId == R.id.menu_delete) {
            repo.deleteNoteAndSync(localAccount, note.getId());
            listener.close();
            return true;
        } else if (itemId == R.id.menu_move) {
            executor.submit(() -> AccountPickerDialogFragment
                    .newInstance(new ArrayList<>(repo.getAccounts()), note.getAccountId())
                    .show(requireActivity().getSupportFragmentManager(), BaseNoteFragment.class.getSimpleName()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @CallSuper
    protected void onNoteLoaded(Note note) {
        this.originalScrollY = note.getScrollY();
        scrollToY(originalScrollY);
        final ScrollView scrollView = getScrollView();
        if (scrollView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scrollView.setOnScrollChangeListener((View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
                    if (scrollY > 0) {
                        note.setScrollY(scrollY);
                    }
                });
            }
        }
    }

    public void onCloseNote() {
        if (!titleModified && originalNote == null && getContent().isEmpty()) {
            repo.deleteNoteAndSync(localAccount, note.getId());
        }
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */
    protected void saveNote(@Nullable ISyncCallback callback) {
        Log.d(TAG, "saveData()");
        if (note != null) {
            final String newContent = getContent();
            if (note.getContent().equals(newContent)) {
                if (note.getScrollY() != originalScrollY) {
                    Log.v(TAG, "... only saving new scroll state, since content did not change");
                    repo.updateScrollY(note.getId(), note.getScrollY());
                } else {
                    Log.v(TAG, "... not saving, since nothing has changed");
                }
                if (callback != null) callback.onScheduled();
            } else {
                // FIXME requires database queries on main thread!
                note = repo.updateNoteAndSync(localAccount, note, newContent, null, callback);
                listener.onNoteUpdated(note);
                requireActivity().invalidateOptionsMenu();
            }
        } else {
            Log.e(TAG, "note is null");
        }
    }

    protected abstract String getContent();

    /**
     * Opens a dialog in order to chose a category
     */
    public void showNoteDetailsDialog() {
        saveNote(new ISyncCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onScheduled() {
                final String fragmentId = "fragment_note_details";
                final FragmentManager manager = requireActivity().getSupportFragmentManager();
                Fragment frag = manager.findFragmentByTag(fragmentId);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }
                DialogFragment noteDetailsFragment = NoteDetailsDialogFragment.newInstance(localAccount, note.getId());
                noteDetailsFragment.setTargetFragment(BaseNoteFragment.this, 0);
                noteDetailsFragment.show(manager, fragmentId);
            }
        });
    }

    @Override
    public void onNoteDetailsEdited(String title, String category) {
        titleModified = true;
        note.setTitle(title);
        note.setCategory(category);
        listener.onNoteUpdated(note);
    }

    public void moveNote(Account account) {
        final LiveData<Note> moveLiveData = repo.moveNoteToAnotherAccount(account, note);
        moveLiveData.observe(this, (v) -> moveLiveData.removeObservers(this));
        listener.close();
    }

    @ColorInt
    protected static int getTextHighlightBackgroundColor(@NonNull Context context, @ColorInt int mainColor, @ColorInt int colorPrimary, @ColorInt int colorAccent) {
        if (isDarkThemeActive(context)) { // Dark background
            if (ColorUtil.INSTANCE.isColorDark(mainColor)) { // Dark brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorPrimary)) { // But also dark text
                    return mainColor;
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            } else { // Light brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorAccent)) { // But also dark text
                    return Color.argb(77, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            }
        } else { // Light background
            if (ColorUtil.INSTANCE.isColorDark(mainColor)) { // Dark brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorAccent)) { // But also dark text
                    return Color.argb(77, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            } else { // Light brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorPrimary)) { // But also dark text
                    return mainColor;
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            }
        }
    }

    public interface NoteFragmentListener {
        void close();

        void onNoteUpdated(Note note);
    }
}
