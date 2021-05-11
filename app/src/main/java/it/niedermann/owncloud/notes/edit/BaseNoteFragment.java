package it.niedermann.owncloud.notes.edit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
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

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.Calendar;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandedFragment;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment.CategoryDialogListener;
import it.niedermann.owncloud.notes.edit.title.EditTitleDialogFragment;
import it.niedermann.owncloud.notes.edit.title.EditTitleDialogFragment.EditTitleListener;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

import static androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported;
import static it.niedermann.owncloud.notes.NotesApplication.isDarkThemeActive;
import static it.niedermann.owncloud.notes.branding.BrandingUtil.tintMenuIcon;
import static it.niedermann.owncloud.notes.edit.EditNoteActivity.ACTION_SHORTCUT;
import static java.lang.Boolean.TRUE;

public abstract class BaseNoteFragment extends BrandedFragment implements CategoryDialogListener, EditTitleListener {

    private static final String TAG = BaseNoteFragment.class.getSimpleName();

    protected static final int MENU_ID_PIN = -1;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(() -> {
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
        }).start();
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

        if (isRequestPinShortcutSupported(requireActivity()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menu.add(Menu.NONE, MENU_ID_PIN, 110, R.string.pin_to_homescreen);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (note != null) {
            prepareFavoriteOption(menu.findItem(R.id.menu_favorite));

            final ApiVersion preferredApiVersion = ApiVersionUtil.getPreferredApiVersion(localAccount.getApiVersion());
            menu.findItem(R.id.menu_title).setVisible(preferredApiVersion != null && preferredApiVersion.compareTo(ApiVersion.API_VERSION_1_0) >= 0);
            menu.findItem(R.id.menu_delete).setVisible(!isNew);
        }
    }

    private void prepareFavoriteOption(MenuItem item) {
        item.setIcon(TRUE.equals(note.getFavorite()) ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
        item.setChecked(note.getFavorite());
        tintMenuIcon(item, colorAccent);
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_cancel) {
            new Thread(() -> {
                if (originalNote == null) {
                    repo.deleteNoteAndSync(localAccount, note.getId());
                } else {
                    repo.updateNoteAndSync(localAccount, originalNote, null, null, null);
                }
            }).start();
            listener.close();
            return true;
        } else if (itemId == R.id.menu_delete) {
            repo.deleteNoteAndSync(localAccount, note.getId());
            listener.close();
            return true;
        } else if (itemId == R.id.menu_favorite) {
            repo.toggleFavoriteAndSync(localAccount, note.getId());
            listener.onNoteUpdated(note);
            prepareFavoriteOption(item);
            return true;
        } else if (itemId == R.id.menu_category) {
            showCategorySelector();
            return true;
        } else if (itemId == R.id.menu_title) {
            showEditTitleDialog();
            return true;
        } else if (itemId == R.id.menu_move) {
            new Thread(() -> {
                AccountPickerDialogFragment
                        .newInstance(new ArrayList<>(), note.getAccountId())
                        .show(requireActivity().getSupportFragmentManager(), BaseNoteFragment.class.getSimpleName());
            }).start();
            return true;
        } else if (itemId == R.id.menu_share) {
            ShareUtil.openShareDialog(requireContext(), note.getTitle(), note.getContent());
            return false;
        } else if (itemId == MENU_ID_PIN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final ShortcutManager shortcutManager = requireActivity().getSystemService(ShortcutManager.class);
                if (shortcutManager != null) {
                    if (shortcutManager.isRequestPinShortcutSupported()) {
                        final ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(getActivity(), note.getId() + "")
                                .setShortLabel(note.getTitle())
                                .setIcon(Icon.createWithResource(requireActivity().getApplicationContext(), TRUE.equals(note.getFavorite()) ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                .setIntent(new Intent(getActivity(), EditNoteActivity.class).putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId()).setAction(ACTION_SHORTCUT))
                                .build();

                        shortcutManager.requestPinShortcut(pinShortcutInfo, PendingIntent.getBroadcast(getActivity(), 0, shortcutManager.createShortcutResultIntent(pinShortcutInfo), 0).getIntentSender());
                    } else {
                        Log.i(TAG, "RequestPinShortcut is not supported");
                    }
                } else {
                    Log.e(TAG, ShortcutManager.class.getSimpleName() + " is null");
                }
            }

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
    private void showCategorySelector() {
        final String fragmentId = "fragment_category";
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag(fragmentId);
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        final DialogFragment categoryFragment = CategoryDialogFragment.newInstance(note.getAccountId(), note.getCategory());
        categoryFragment.setTargetFragment(this, 0);
        categoryFragment.show(manager, fragmentId);
    }

    /**
     * Opens a dialog in order to chose a category
     */
    public void showEditTitleDialog() {
        saveNote(null);
        final String fragmentId = "fragment_edit_title";
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag(fragmentId);
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        DialogFragment editTitleFragment = EditTitleDialogFragment.newInstance(note.getTitle());
        editTitleFragment.setTargetFragment(this, 0);
        editTitleFragment.show(manager, fragmentId);
    }

    @Override
    public void onCategoryChosen(String category) {
        repo.setCategory(localAccount, note.getId(), category);
        note.setCategory(category);
        listener.onNoteUpdated(note);
    }

    @Override
    public void onTitleEdited(String newTitle) {
        titleModified = true;
        note.setTitle(newTitle);
        new Thread(() -> {
            note = repo.updateNoteAndSync(localAccount, note, note.getContent(), newTitle, null);
            requireActivity().runOnUiThread(() -> listener.onNoteUpdated(note));
        }).start();
    }

    public void moveNote(Account account) {
        repo.moveNoteToAnotherAccount(account, note);
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
