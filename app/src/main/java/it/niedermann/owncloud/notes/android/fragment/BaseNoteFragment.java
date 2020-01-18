package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.DisplayUtils;
import it.niedermann.owncloud.notes.util.ICallback;

import static androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported;
import static it.niedermann.owncloud.notes.android.activity.EditNoteActivity.ACTION_SHORTCUT;

public abstract class BaseNoteFragment extends Fragment implements CategoryDialogFragment.CategoryDialogListener {

    private static final String TAG = BaseNoteFragment.class.getSimpleName();

    private static final int MENU_ID_PIN = -1;
    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_NEWNOTE = "newNote";
    private static final String SAVEDKEY_NOTE = "note";
    private static final String SAVEDKEY_ORIGINAL_NOTE = "original_note";

    protected SearchView searchView;
    protected MenuItem searchMenuItem;

    protected String searchQuery = null;

    private LocalAccount localAccount;

    protected DBNote note;
    @Nullable
    private DBNote originalNote;
    protected NoteSQLiteOpenHelper db;
    private NoteFragmentListener listener;

    private TextView activeTextView;
    private boolean isNew = true;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery", "");
        }
    }

    protected void setActiveTextView(TextView textView) {
        activeTextView = textView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            this.localAccount = db.getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(getActivity().getApplicationContext()).name);

            if (savedInstanceState == null) {
                long id = getArguments().getLong(PARAM_NOTE_ID);
                if (id > 0) {
                    long accountId = getArguments().getLong(PARAM_ACCOUNT_ID);
                    if (accountId > 0) {
                        /* Switch account if account id has been provided */
                        this.localAccount = db.getAccount(accountId);
                        SingleAccountHelper.setCurrentAccount(getActivity().getApplicationContext(), localAccount.getAccountName());
                        try {
                            db.getNoteServerSyncHelper().updateAccount();
                        } catch (NextcloudFilesAppAccountNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    isNew = false;
                    note = originalNote = db.getNote(localAccount.getId(), id);
                } else {
                    CloudNote cloudNote = (CloudNote) getArguments().getSerializable(PARAM_NEWNOTE);
                    if (cloudNote == null) {
                        throw new IllegalArgumentException(PARAM_NOTE_ID + " is not given and argument " + PARAM_NEWNOTE + " is missing.");
                    }
                    note = db.getNote(localAccount.getId(), db.addNoteAndSync(localAccount.getId(), cloudNote));
                    originalNote = null;
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (NoteFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getClass() + " must implement " + NoteFragmentListener.class);
        }
        db = NoteSQLiteOpenHelper.getInstance(activity);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveNote(null);
        outState.putSerializable(SAVEDKEY_NOTE, note);
        outState.putSerializable(SAVEDKEY_ORIGINAL_NOTE, originalNote);

        if (searchView != null && !TextUtils.isEmpty(searchView.getQuery().toString())) {
            outState.putString("searchQuery", searchView.getQuery().toString());
        }
    }

    private void colorWithText(String newText) {
        if (activeTextView != null && ViewCompat.isAttachedToWindow(activeTextView)) {
            activeTextView.setText(DisplayUtils.searchAndColor(activeTextView.getText().toString(), new SpannableString
                            (activeTextView.getText()), newText, getResources().getColor(R.color.primary)),
                    TextView.BufferType.SPANNABLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_fragment, menu);

        if (isRequestPinShortcutSupported(getActivity()) && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menu.add(Menu.NONE, MENU_ID_PIN, 110, R.string.pin_to_homescreen);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem itemFavorite = menu.findItem(R.id.menu_favorite);
        prepareFavoriteOption(itemFavorite);

        menu.findItem(R.id.menu_delete).setVisible(!isNew);

        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        if (!TextUtils.isEmpty(searchQuery) && isNew) {
            searchMenuItem.expandActionView();
            searchView.setQuery(searchQuery, true);
            searchView.clearFocus();
        } else {
            searchMenuItem.collapseActionView();
        }


        final LinearLayout searchEditFrame = searchView.findViewById(R.id
                .search_edit_frame);

        searchEditFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int oldVisibility = -1;

            @Override
            public void onGlobalLayout() {
                int currentVisibility = searchEditFrame.getVisibility();

                if (currentVisibility != oldVisibility) {
                    if (currentVisibility != View.VISIBLE) {
                        colorWithText("");
                        searchQuery = "";
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                colorWithText(newText);
                return true;
            }
        });

    }

    private void prepareFavoriteOption(MenuItem item) {
        item.setIcon(note.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
        item.setChecked(note.isFavorite());
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                if (originalNote == null) {
                    db.deleteNoteAndSync(note.getId());
                } else {
                    db.updateNoteAndSync(localAccount.getId(), originalNote, null, null);
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
            case R.id.menu_move:
                AccountChooserDialogFragment.newInstance().show(getFragmentManager(), BaseNoteFragment.class.getCanonicalName());
                return true;
            case R.id.menu_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, note.getContent());


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(Intent.createChooser(shareIntent, note.getTitle()));
                } else {
                    ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                    actionProvider.setShareIntent(shareIntent);
                }

                return false;
            case MENU_ID_PIN:
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ShortcutManager shortcutManager = getActivity().getSystemService(ShortcutManager.class);

                    if (shortcutManager.isRequestPinShortcutSupported()) {
                        Intent intent = new Intent(getActivity(), EditNoteActivity.class);
                        intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
                        intent.setAction(ACTION_SHORTCUT);

                        ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(getActivity(), note.getId() + "")
                                .setShortLabel(note.getTitle())
                                .setIcon(Icon.createWithResource(getActivity().getApplicationContext(), note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                .setIntent(intent)
                                .build();

                        Intent pinnedShortcutCallbackIntent =
                                shortcutManager.createShortcutResultIntent(pinShortcutInfo);

                        PendingIntent successCallback = PendingIntent.getBroadcast(getActivity(), /* request code */ 0,
                                pinnedShortcutCallbackIntent, /* flags */ 0);

                        shortcutManager.requestPinShortcut(pinShortcutInfo,
                                successCallback.getIntentSender());
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCloseNote() {
        if (originalNote == null && getContent().isEmpty()) {
            db.deleteNoteAndSync(note.getId());
        }
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */
    protected void saveNote(@Nullable ICallback callback) {
        Log.d(TAG, "saveData()");
        if (note != null) {
            String newContent = getContent();
            if (note.getContent().equals(newContent)) {
                Log.v(TAG, "... not saving, since nothing has changed");
            } else {
                note = db.updateNoteAndSync(localAccount.getId(), note, newContent, callback);
                listener.onNoteUpdated(note);
            }
        } else {
            Log.e(TAG, "note is null");
        }
    }

    protected float getFontSizeFromPreferences(SharedPreferences sp) {
        final String prefValueSmall = getString(R.string.pref_value_font_size_small);
        final String prefValueMedium = getString(R.string.pref_value_font_size_medium);
        // final String prefValueLarge = getString(R.string.pref_value_font_size_large);
        String fontSize = sp.getString(getString(R.string.pref_key_font_size), prefValueMedium);

        assert fontSize != null;
        if (fontSize.equals(prefValueSmall)) {
            return getResources().getDimension(R.dimen.note_font_size_small);
        } else if (fontSize.equals(prefValueMedium)) {
            return getResources().getDimension(R.dimen.note_font_size_medium);
        } else {
            return getResources().getDimension(R.dimen.note_font_size_large);
        }
    }

    protected abstract String getContent();

    /**
     * Opens a dialog in order to chose a category
     */
    private void showCategorySelector() {
        final String fragmentId = "fragment_category";
        FragmentManager manager = getFragmentManager();
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
        db.setCategory(note, category, null);
        listener.onNoteUpdated(note);
    }

    public void moveNote(LocalAccount account) {
        db.moveNoteToAnotherAccount(note.getAccountId(), note, account.getId());
        listener.close();
    }

    public interface NoteFragmentListener {
        void close();

        void onNoteUpdated(DBNote note);
    }
}
