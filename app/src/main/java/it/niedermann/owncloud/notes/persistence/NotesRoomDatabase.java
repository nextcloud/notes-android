package it.niedermann.owncloud.notes.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.preference.PreferenceManager;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.dao.CategoryDao;
import it.niedermann.owncloud.notes.persistence.dao.LocalAccountDao;
import it.niedermann.owncloud.notes.persistence.dao.NoteDao;
import it.niedermann.owncloud.notes.persistence.dao.WidgetNotesListDao;
import it.niedermann.owncloud.notes.persistence.dao.WidgetSingleNoteDao;
import it.niedermann.owncloud.notes.persistence.entity.CategoryEntity;
import it.niedermann.owncloud.notes.persistence.entity.Converters;
import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;
import it.niedermann.owncloud.notes.persistence.entity.NoteEntity;
import it.niedermann.owncloud.notes.persistence.entity.WidgetNotesListEntity;
import it.niedermann.owncloud.notes.persistence.entity.WidgetSingleNoteEntity;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.Category;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.CloudNote;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;
import it.niedermann.owncloud.notes.shared.util.ColorUtil;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

import static it.niedermann.owncloud.notes.edit.EditNoteActivity.ACTION_SHORTCUT;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt;
import static it.niedermann.owncloud.notes.widget.notelist.NoteListWidget.updateNoteListWidgets;
import static it.niedermann.owncloud.notes.widget.singlenote.SingleNoteWidget.updateSingleNoteWidgets;

@Database(
        entities = {
                LocalAccountEntity.class,
                NoteEntity.class,
                CategoryEntity.class,
                WidgetSingleNoteEntity.class,
                WidgetNotesListEntity.class
        }, version = 18
)
@TypeConverters({Converters.class})
public abstract class NotesRoomDatabase extends RoomDatabase {

    private static final String TAG = NotesRoomDatabase.class.getSimpleName();
    private static final String NOTES_DB_NAME = "OWNCLOUD_NOTES";
    private static NotesRoomDatabase instance;
    private static Context context;
    private static NoteServerSyncHelper syncHelper;

    public static NotesRoomDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = create(context);
            NotesRoomDatabase.context = context;
            syncHelper = NoteServerSyncHelper.getInstance(NotesDatabase.getInstance(context), instance);
        }
        return instance;
    }

    private static NotesRoomDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                NotesRoomDatabase.class,
                NOTES_DB_NAME)
                .addMigrations(OLD_STUFF)
                .fallbackToDestructiveMigration()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Log.v(TAG, NotesRoomDatabase.class.getSimpleName() + " created.");
                    }
                })
                .allowMainThreadQueries() // FIXME remove
                .build();
    }

    private static final Migration OLD_STUFF = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };

    public abstract LocalAccountDao getLocalAccountDao();

    public abstract CategoryDao getCategoryDao();

    public abstract NoteDao getNoteDao();

    public abstract WidgetSingleNoteDao getWidgetSingleNoteDao();

    public abstract WidgetNotesListDao getWidgetNotesListDao();

    @SuppressWarnings("UnusedReturnValue")
    public long addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) {
        final LocalAccountEntity entity = new LocalAccountEntity();
        entity.setUrl(url);
        entity.setUsername(username);
        entity.setAccountName(accountName);
        entity.setCapabilities(capabilities);
        return getLocalAccountDao().insert(entity);
    }

    public void updateBrand(long accountId, @NonNull Capabilities capabilities) throws IllegalArgumentException {
        validateAccountId(accountId);

        String color;
        try {
            color = ColorUtil.formatColorToParsableHexString(capabilities.getColor()).substring(1);
        } catch (Exception e) {
            color = "0082C9";
        }

        String textColor;
        try {
            textColor = ColorUtil.formatColorToParsableHexString(capabilities.getTextColor()).substring(1);
        } catch (Exception e) {
            textColor = "FFFFFF";
        }

        getLocalAccountDao().updateBrand(accountId, color, textColor);
    }


    void deleteNote(long id, @NonNull DBStatus forceDBStatus) {
        getNoteDao().deleteByCardId(id, forceDBStatus);
        getCategoryDao().removeEmptyCategory(id);
    }


    /**
     * Get the category if with the given category title
     * The method does not support fuzzy search.
     * Because the category title in database is unique, there will not at most one result.
     * If there is no such category, database will create it if create flag is set.
     * Otherwise this method will return -1 as default value.
     *
     * @param accountId     The user {@link LocalAccountEntity} Id
     * @param categoryTitle The category title which will be search in the db
     * @return -1 if there is no such category else the corresponding id
     */
    @NonNull
    @WorkerThread
    private Long getOrCreateCategoryIdByTitle(long accountId, @NonNull String categoryTitle) {
        validateAccountId(accountId);
        Long categoryId = getCategoryDao().getCategoryIdByTitle(accountId, categoryTitle);
        if (categoryId > 0) {
            return categoryId;
        } else {
            CategoryEntity entity = new CategoryEntity();
            entity.setAccountId(accountId);
            entity.setTitle(categoryTitle);
            return getCategoryDao().addCategory(entity);
        }
    }

    public void moveNoteToAnotherAccount(SingleSignOnAccount ssoAccount, long oldAccountId, NoteEntity note, long newAccountId) {
        // Add new note
        addNoteAndSync(ssoAccount, newAccountId, new CloudNote(0, note.getModified(), note.getTitle(), note.getContent(), note.getFavorite(), note.getCategory().getTitle(), null));
        deleteNoteAndSync(ssoAccount, note.getId());

        notifyWidgets();
        syncHelper.scheduleSync(ssoAccount, true);
    }

    /**
     * Marks a Note in the Database as Deleted. In the next Synchronization it will be deleted
     * from the Server.
     *
     * @param id long - ID of the Note that should be deleted
     */
    public void deleteNoteAndSync(SingleSignOnAccount ssoAccount, long id) {
        getNoteDao().updateStatus(id, DBStatus.LOCAL_DELETED);
        notifyWidgets();
        syncHelper.scheduleSync(ssoAccount, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager != null) {
                shortcutManager.getPinnedShortcuts().forEach((shortcut) -> {
                    String shortcutId = id + "";
                    if (shortcut.getId().equals(shortcutId)) {
                        Log.v(TAG, "Removing shortcut for " + shortcutId);
                        shortcutManager.disableShortcuts(Collections.singletonList(shortcutId), context.getResources().getString(R.string.note_has_been_deleted));
                    }
                });
            } else {
                Log.e(TAG, ShortcutManager.class.getSimpleName() + "is null.");
            }
        }
    }

    /**
     * Creates a new Note in the Database and adds a Synchronization Flag.
     *
     * @param note Note
     */
    public long addNoteAndSync(SingleSignOnAccount ssoAccount, long accountId, CloudNote note) {
        NoteEntity entity = new NoteEntity(0, 0, note.getModified(), note.getTitle(), note.getContent(), note.isFavorite(), note.getCategory(), note.getEtag(), DBStatus.LOCAL_EDITED, accountId, generateNoteExcerpt(note.getContent(), note.getTitle()), 0);
        long id = addNote(accountId, entity);
        notifyWidgets();
        syncHelper.scheduleSync(ssoAccount, true);
        return id;
    }

    /**
     * Inserts a note directly into the Database.
     * No Synchronisation will be triggered! Use addNoteAndSync()!
     *
     * @param note Note to be added. Remotely created Notes must be of type CloudNote and locally created Notes must be of Type DBNote (with DBStatus.LOCAL_EDITED)!
     */
    long addNote(long accountId, NoteEntity note) {
        NoteEntity entity = new NoteEntity();
        if (entity.getId() != null) {
            if (entity.getId() > 0) {
                entity.setId(entity.getId());
            }
            entity.setStatus(entity.getStatus());
            entity.setAccountId(entity.getAccountId());
            entity.setExcerpt(entity.getExcerpt());
        } else {
            entity.setStatus(DBStatus.VOID);
            entity.setAccountId(accountId);
            entity.setExcerpt(generateNoteExcerpt(note.getContent(), note.getTitle()));
        }
        if (note.getRemoteId() > 0) {
            entity.setRemoteId(note.getRemoteId());
        }
        entity.setTitle(note.getTitle());
        entity.setModified(note.getModified());
        entity.setContent(note.getContent());
        entity.setFavorite(note.getFavorite());
        // FIXME
//        entity.setCategory(getOrCreateCategoryIdByTitle(accountId, note.getCategory()));
        entity.setETag(note.getETag());
        return getNoteDao().addNote(entity);
    }

    private static void validateAccountId(long accountId) {
        if (accountId < 1) {
            throw new IllegalArgumentException("accountId must be greater than 0");
        }
    }

    /**
     * Notify about changed notes.
     */
    protected void notifyWidgets() {
        updateSingleNoteWidgets(context);
        updateNoteListWidgets(context);
    }


    void updateDynamicShortcuts(long accountId) {
        new Thread(() -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = context.getApplicationContext().getSystemService(ShortcutManager.class);
                if (shortcutManager != null) {
                    if (!shortcutManager.isRateLimitingActive()) {
                        List<ShortcutInfo> newShortcuts = new ArrayList<>();

                        for (NoteEntity note : getNoteDao().getRecentNotes(accountId)) {
                            if (!TextUtils.isEmpty(note.getTitle())) {
                                Intent intent = new Intent(context.getApplicationContext(), EditNoteActivity.class);
                                intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
                                intent.setAction(ACTION_SHORTCUT);

                                newShortcuts.add(new ShortcutInfo.Builder(context.getApplicationContext(), note.getId() + "")
                                        .setShortLabel(note.getTitle() + "")
                                        .setIcon(Icon.createWithResource(context.getApplicationContext(), note.getFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                        .setIntent(intent)
                                        .build());
                            } else {
                                // Prevent crash https://github.com/stefan-niedermann/nextcloud-notes/issues/613
                                Log.e(TAG, "shortLabel cannot be empty " + note);
                            }
                        }
                        Log.d(TAG, "Update dynamic shortcuts");
                        shortcutManager.removeAllDynamicShortcuts();
                        shortcutManager.addDynamicShortcuts(newShortcuts);
                    }
                }
            }
        }).start();
    }

    /**
     * Set the category for a given note.
     * This method will search in the database to find out the category id in the db.
     * If there is no such category existing, this method will create it and search again.
     *
     * @param ssoAccount The single sign on account
     * @param note       The note which will be updated
     * @param category   The category title which should be used to find the category id.
     * @param callback   When the synchronization is finished, this callback will be invoked (optional).
     */
    public void setCategory(SingleSignOnAccount ssoAccount, @NonNull NoteEntity note, @NonNull String category, @Nullable ISyncCallback callback) {
        note.setCategory(getCategoryDao().getCategory(getCategoryDao().getCategoryIdByTitle(getLocalAccountDao().getLocalAccountByAccountName(ssoAccount.name).getId(), category)));
        getNoteDao().updateStatus(note.getId(), DBStatus.LOCAL_DELETED);
        long categoryId = getOrCreateCategoryIdByTitle(note.getAccountId(), note.getCategory().getTitle());
        getNoteDao().updateCategory(note.getId(), categoryId);
        getCategoryDao().removeEmptyCategory(note.getAccountId());
        if (callback != null) {
            syncHelper.addCallbackPush(ssoAccount, callback);
        }
        syncHelper.scheduleSync(ssoAccount, true);
    }

    @NonNull
    @WorkerThread
    public Map<Long, Long> getIdMap(long accountId) {
        validateAccountId(accountId);
        Map<Long, Long> result = new HashMap<>();
        for (NoteEntity note : getNoteDao().getRemoteIdAndId(accountId)) {
            result.put(note.getRemoteId(), note.getId());
        }
        return result;
    }

    /**
     * Modifies the sorting method for one category, the category can be normal category or
     * one of "All notes", "Favorite", and "Uncategorized".
     * If category is one of these three, sorting method will be modified in android.content.SharedPreference.
     * The user can determine use which sorting method to show the notes for a category.
     * When the user changes the sorting method, this method should be called.
     *
     * @param accountId     The user accountID
     * @param category      The category to be modified
     * @param sortingMethod The sorting method in {@link CategorySortingMethod} enum format
     */
    public void modifyCategoryOrder(
            long accountId, Category category, CategorySortingMethod sortingMethod) {
        validateAccountId(accountId);

        final Context ctx = context.getApplicationContext();
        final SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        int orderIndex = sortingMethod.getCSMID();
        if (category.category == null) {
            if (category.favorite != null && category.favorite) {
                // Favorite
                sp.putInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_favorites),
                        orderIndex);
            } else {
                // All notes
                sp.putInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_all_notes),
                        orderIndex);
            }
        } else if (category.category.isEmpty()) {
            // Uncategorized
            sp.putInt(ctx.getString(R.string.action_sorting_method) +
                            ' ' + ctx.getString(R.string.action_uncategorized),
                    orderIndex);
        } else {
            getCategoryDao().modifyCategoryOrderByTitle(accountId, category.category, sortingMethod);
            return;
        }
        sp.apply();
    }

    /**
     * Gets the sorting method of a category, the category can be normal category or
     * one of "All notes", "Favorite", and "Uncategorized".
     * If category is one of these three, sorting method will be got from android.content.SharedPreference.
     * The sorting method of the category can be used to decide
     * to use which sorting method to show the notes for each categories.
     *
     * @param accountId The user accountID
     * @param category  The category
     * @return The sorting method in CategorySortingMethod enum format
     */
    public CategorySortingMethod getCategoryOrder(long accountId, Category category) {
        validateAccountId(accountId);

        final Context ctx = context.getApplicationContext();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        int orderIndex;

        if (category.category == null) {
            if (category.favorite != null && category.favorite) {
                // Favorite
                orderIndex = sp.getInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_favorites),
                        0);
            } else {
                // All notes
                orderIndex = sp.getInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_all_notes),
                        0);
            }
        } else if (category.category.isEmpty()) {
            // Uncategorized
            orderIndex = sp.getInt(ctx.getString(R.string.action_sorting_method) +
                            ' ' + ctx.getString(R.string.action_uncategorized),
                    0);
        } else {
            return getCategoryDao().getCategoryOrderByTitle(accountId, category.category);
        }

        return CategorySortingMethod.getCSM(orderIndex);
    }

    public void toggleFavoriteAndSync(SingleSignOnAccount ssoAccount, long noteId, @Nullable ISyncCallback callback) {
        getNoteDao().toggleFavorite(noteId);
        if (callback != null) {
            syncHelper.addCallbackPush(ssoAccount, callback);
        }
        syncHelper.scheduleSync(ssoAccount, true);
    }

    /**
     * Updates a single Note with data from the server, (if it was not modified locally).
     * Thereby, an optimistic concurrency control is realized in order to prevent conflicts arising due to parallel changes from the UI and synchronization.
     * This is used by the synchronization task, hence no Synchronization will be triggered. Use updateNoteAndSync() instead!
     *
     * @param id                        local ID of Note
     * @param remoteNote                Note from the server.
     * @param forceUnchangedDBNoteState is not null, then the local note is updated only if it was not modified meanwhile
     */
    void updateNote(long accountId, long id, @NonNull NoteEntity remoteNote, @Nullable NoteEntity forceUnchangedDBNoteState) {
        validateAccountId(accountId);
        // First, update the remote ID, since this field cannot be changed in parallel, but have to be updated always.
        getNoteDao().updateRemoteId(id, remoteNote.getRemoteId());

        // The other columns have to be updated in dependency of forceUnchangedDBNoteState,
        // since the Synchronization-Task must not overwrite locales changes!
        if (forceUnchangedDBNoteState != null) {
            getNoteDao().updateIfModifiedLocallyDuringSync(id, remoteNote.getModified().getTimeInMillis() / 1000, remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getCategory().getTitle(), remoteNote.getETag(), remoteNote.getContent());
        } else {
            getNoteDao().updateIfNotModifiedLocallyAndRemoteColumnHasChanged(id, remoteNote.getModified().getTimeInMillis() / 1000, remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getCategory().getTitle(), remoteNote.getETag(), remoteNote.getContent());
        }
        getCategoryDao().removeEmptyCategory(accountId);
        Log.d(TAG, "updateNote: " + remoteNote + " || forceUnchangedDBNoteState: " + forceUnchangedDBNoteState + "");
    }

    /**
     * Updates a single Note with a new content.
     * The title is derived from the new content automatically, and modified date as well as DBStatus are updated, too -- if the content differs to the state in the database.
     *
     * @param oldNote    Note to be changed
     * @param newContent New content. If this is <code>null</code>, then <code>oldNote</code> is saved again (useful for undoing changes).
     * @param newTitle   New title. If this is <code>null</code>, then either the old title is reused (in case the note has been synced before) or a title is generated (in case it is a new note)
     * @param callback   When the synchronization is finished, this callback will be invoked (optional).
     * @return changed {@link DBNote} if differs from database, otherwise the old {@link DBNote}.
     */
    public NoteEntity updateNoteAndSync(SingleSignOnAccount ssoAccount, @NonNull LocalAccountEntity localAccount, @NonNull NoteEntity oldNote, @Nullable String newContent, @Nullable String newTitle, @Nullable ISyncCallback callback) {
        NoteEntity newNote;
        if (newContent == null) {
            newNote = new NoteEntity(oldNote.getId(), oldNote.getRemoteId(), oldNote.getModified(), oldNote.getTitle(), oldNote.getContent(), oldNote.getFavorite(), oldNote.getCategory().getTitle(), oldNote.getETag(), DBStatus.LOCAL_EDITED, localAccount.getId(), oldNote.getExcerpt(), oldNote.getScrollY());
        } else {
            final String title;
            if (newTitle != null) {
                title = newTitle;
            } else {
                if (oldNote.getRemoteId() == 0 || localAccount.getPreferredApiVersion() == null || localAccount.getPreferredApiVersion().compareTo(new ApiVersion("1.0", 0, 0)) < 0) {
                    title = NoteUtil.generateNonEmptyNoteTitle(newContent, context);
                } else {
                    title = oldNote.getTitle();
                }
            }
            newNote = new NoteEntity(oldNote.getId(), oldNote.getRemoteId(), Calendar.getInstance(), title, newContent, oldNote.getFavorite(), oldNote.getCategory().getTitle(), oldNote.getETag(), DBStatus.LOCAL_EDITED, localAccount.getId(), generateNoteExcerpt(newContent, title), oldNote.getScrollY());
        }
        int rows = getNoteDao().updateNote(newNote);
        getCategoryDao().removeEmptyCategory(localAccount.getId());
        // if data was changed, set new status and schedule sync (with callback); otherwise invoke callback directly.
        if (rows > 0) {
            notifyWidgets();
            if (callback != null) {
                syncHelper.addCallbackPush(ssoAccount, callback);
            }
            syncHelper.scheduleSync(ssoAccount, true);
            return newNote;
        } else {
            if (callback != null) {
                callback.onFinish();
            }
            return oldNote;
        }
    }
}
