package it.niedermann.owncloud.notes.android.fragment;

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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.android.fragment.CategoryDialogFragment.CategoryDialogListener;
import it.niedermann.owncloud.notes.branding.BrandedFragment;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.ISyncCallback;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.util.ColorUtil;
import it.niedermann.owncloud.notes.util.NoteUtil;
import it.niedermann.owncloud.notes.util.ShareUtil;

import static androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported;
import static it.niedermann.owncloud.notes.android.activity.EditNoteActivity.ACTION_SHORTCUT;
import static it.niedermann.owncloud.notes.branding.BrandingUtil.tintMenuIcon;
import static it.niedermann.owncloud.notes.util.ColorUtil.isColorDark;
import static it.niedermann.owncloud.notes.util.Notes.isDarkThemeActive;

public abstract class BaseNoteFragment extends BrandedFragment implements CategoryDialogListener {

    private static final String TAG = BaseNoteFragment.class.getSimpleName();

    protected static final int MENU_ID_PIN = -1;
    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_NEWNOTE = "newNote";
    private static final String SAVEDKEY_NOTE = "note";
    private static final String SAVEDKEY_ORIGINAL_NOTE = "original_note";

    private LocalAccount localAccount;
    private SingleSignOnAccount ssoAccount;

    protected DBNote note;
    // TODO do we really need this? The reference to note is currently the same
    @Nullable
    private DBNote originalNote;
    private int originalScrollY;
    protected NotesDatabase db;
    private NoteFragmentListener listener;

    protected boolean isNew = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(requireActivity().getApplicationContext());
            this.localAccount = db.getLocalAccountByAccountName(ssoAccount.name);

            if (savedInstanceState == null) {
                long id = requireArguments().getLong(PARAM_NOTE_ID);
                if (id > 0) {
                    long accountId = requireArguments().getLong(PARAM_ACCOUNT_ID);
                    if (accountId > 0) {
                        /* Switch account if account id has been provided */
                        this.localAccount = db.getAccount(accountId);
                        SingleAccountHelper.setCurrentAccount(requireActivity().getApplicationContext(), localAccount.getAccountName());
                    }
                    isNew = false;
                    note = originalNote = db.getNote(localAccount.getId(), id);
                } else {
                    CloudNote cloudNote = (CloudNote) requireArguments().getSerializable(PARAM_NEWNOTE);
                    String content = requireArguments().getString(PARAM_CONTENT);
                    if (cloudNote == null) {
                        if (content == null) {
                            throw new IllegalArgumentException(PARAM_NOTE_ID + " is not given, argument " + PARAM_NEWNOTE + " is missing and " + PARAM_CONTENT + " is missing.");
                        } else {
                            note = new DBNote(-1, -1, null, NoteUtil.generateNoteTitle(content), content, false, getString(R.string.category_readonly), null, DBStatus.VOID, -1, "", 0);
                        }
                    } else {
                        note = db.getNote(localAccount.getId(), db.addNoteAndSync(ssoAccount, localAccount.getId(), cloudNote));
                        originalNote = null;
                    }
                }
            } else {
                note = (DBNote) savedInstanceState.getSerializable(SAVEDKEY_NOTE);
                originalNote = (DBNote) savedInstanceState.getSerializable(SAVEDKEY_ORIGINAL_NOTE);
            }
            setHasOptionsMenu(true);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    protected abstract ScrollView getScrollView();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ScrollView scrollView = getScrollView();
        if (scrollView != null) {
            scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                if (scrollView.getScrollY() > 0) {
                    note.setScrollY(scrollView.getScrollY());
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ScrollView scrollView = getScrollView();
        if (scrollView != null) {
            this.originalScrollY = note.getScrollY();
            scrollView.post(() -> scrollView.scrollTo(0, originalScrollY));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (NoteFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement " + NoteFragmentListener.class);
        }
        db = NotesDatabase.getInstance(context);
    }

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
        MenuItem itemFavorite = menu.findItem(R.id.menu_favorite);
        prepareFavoriteOption(itemFavorite);

        menu.findItem(R.id.menu_delete).setVisible(!isNew);
    }

    private void prepareFavoriteOption(MenuItem item) {
        item.setIcon(note.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
        item.setChecked(note.isFavorite());
        tintMenuIcon(item, colorAccent);
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                if (originalNote == null) {
                    db.deleteNoteAndSync(ssoAccount, note.getId());
                } else {
                    db.updateNoteAndSync(ssoAccount, localAccount.getId(), originalNote, null, null);
                }
                listener.close();
                return true;
            case R.id.menu_delete:
                db.deleteNoteAndSync(ssoAccount, note.getId());
                listener.close();
                return true;
            case R.id.menu_favorite:
                db.toggleFavorite(ssoAccount, note, null);
                listener.onNoteUpdated(note);
                prepareFavoriteOption(item);
                return true;
            case R.id.menu_category:
                showCategorySelector();
                return true;
            case R.id.menu_move:
                MoveAccountDialogFragment.newInstance().show(requireActivity().getSupportFragmentManager(), BaseNoteFragment.class.getSimpleName());
                return true;
            case R.id.menu_share:
                ShareUtil.openShareDialog(requireContext(), note.getTitle(), note.getContent());
                return false;
            case MENU_ID_PIN:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ShortcutManager shortcutManager = requireActivity().getSystemService(ShortcutManager.class);

                    if (shortcutManager != null) {
                        if (shortcutManager.isRequestPinShortcutSupported()) {
                            Intent intent = new Intent(getActivity(), EditNoteActivity.class);
                            intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
                            intent.setAction(ACTION_SHORTCUT);

                            ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(getActivity(), note.getId() + "")
                                    .setShortLabel(note.getTitle())
                                    .setIcon(Icon.createWithResource(requireActivity().getApplicationContext(), note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                    .setIntent(intent)
                                    .build();

                            Intent pinnedShortcutCallbackIntent =
                                    shortcutManager.createShortcutResultIntent(pinShortcutInfo);

                            PendingIntent successCallback = PendingIntent.getBroadcast(getActivity(), /* request code */ 0,
                                    pinnedShortcutCallbackIntent, /* flags */ 0);

                            shortcutManager.requestPinShortcut(pinShortcutInfo,
                                    successCallback.getIntentSender());
                        } else {
                            Log.i(TAG, "RequestPinShortcut is not supported");
                        }
                    } else {
                        Log.e(TAG, "ShortcutManager is null");
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCloseNote() {
        if (originalNote == null && getContent().isEmpty()) {
            db.deleteNoteAndSync(ssoAccount, note.getId());
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
            String newContent = getContent();
            if (note.getContent().equals(newContent)) {
                if (note.getScrollY() != originalScrollY) {
                    Log.v(TAG, "... only saving new scroll state, since content did not change");
                    db.updateScrollY(note.getId(), note.getScrollY());
                } else {
                    Log.v(TAG, "... not saving, since nothing has changed");
                }
            } else {
                note = db.updateNoteAndSync(ssoAccount, localAccount.getId(), note, newContent, callback);
                listener.onNoteUpdated(note);
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
        Bundle arguments = new Bundle();
        arguments.putString(CategoryDialogFragment.PARAM_CATEGORY, note.getCategory());
        arguments.putLong(CategoryDialogFragment.PARAM_ACCOUNT_ID, note.getAccountId());
        CategoryDialogFragment categoryFragment = new CategoryDialogFragment();
        categoryFragment.setArguments(arguments);
        categoryFragment.setTargetFragment(this, 0);
        categoryFragment.show(manager, fragmentId);
    }

    @Override
    public void onCategoryChosen(String category) {
        db.setCategory(ssoAccount, note, category, null);
        listener.onNoteUpdated(note);
    }

    public void moveNote(LocalAccount account) {
        db.moveNoteToAnotherAccount(ssoAccount, note.getAccountId(), note, account.getId());
        listener.close();
    }

    @ColorInt
    protected static int getTextHighlightBackgroundColor(@NonNull Context context, @ColorInt int mainColor, @ColorInt int colorPrimary, @ColorInt int colorAccent) {
        if (isDarkThemeActive(context)) { // Dark background
            if (isColorDark(mainColor)) { // Dark brand color
                if (ColorUtil.contrastRatioIsSufficient(mainColor, colorPrimary)) { // But also dark text
                    return mainColor;
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            } else { // Light brand color
                if (ColorUtil.contrastRatioIsSufficient(mainColor, colorAccent)) { // But also dark text
                    return Color.argb(77, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            }
        } else { // Light background
            if (isColorDark(mainColor)) { // Dark brand color
                if (ColorUtil.contrastRatioIsSufficient(mainColor, colorAccent)) { // But also dark text
                    return Color.argb(77, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            } else { // Light brand color
                if (ColorUtil.contrastRatioIsSufficient(mainColor, colorPrimary)) { // But also dark text
                    return mainColor;
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            }
        }
    }

    public interface NoteFragmentListener {
        void close();

        void onNoteUpdated(DBNote note);
    }
}
