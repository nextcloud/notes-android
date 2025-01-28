package it.niedermann.owncloud.notes.share.helper;


import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_CLEAR_AT;
import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_ICON;
import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_MESSAGE;
import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_STATUS;

import android.app.SearchManager;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.resources.users.Status;
import com.owncloud.android.lib.resources.users.StatusType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.ShareRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.model.ShareesData;
import it.niedermann.owncloud.notes.share.model.UsersAndGroupsSearchConfig;

/**
 * Content provider for search suggestions, to search for users and groups existing in an ownCloud server.
 */
public class UsersAndGroupsSearchProvider  {

    private static final String TAG = UsersAndGroupsSearchProvider.class.getSimpleName();

    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    private static final int SEARCH = 1;

    private static final int RESULTS_PER_PAGE = 50;
    private static final int REQUESTED_PAGE = 1;

    public static String ACTION_SHARE_WITH;

    public static final String CONTENT = "content";

    private String AUTHORITY;
    private String DATA_USER;
    private String DATA_GROUP;
    private String DATA_ROOM;
    private String DATA_REMOTE;
    private String DATA_EMAIL;
    private String DATA_CIRCLE;

    private UriMatcher mUriMatcher;

    private ShareRepository repository;
    private Account account;
    private Context context;

    public UsersAndGroupsSearchProvider(Context context, Account account, ShareRepository repository) {
        this.context = context;
        this.account = account;
        this.repository = repository;

        AUTHORITY = context.getString(R.string.users_and_groups_search_authority);
        setActionShareWith(context);
        DATA_USER = AUTHORITY + ".data.user";
        DATA_GROUP = AUTHORITY + ".data.group";
        DATA_ROOM = AUTHORITY + ".data.room";
        DATA_REMOTE = AUTHORITY + ".data.remote";
        DATA_EMAIL = AUTHORITY + ".data.email";
        DATA_CIRCLE = AUTHORITY + ".data.circle";

        sShareTypes.put(DATA_USER, ShareType.USER);
        sShareTypes.put(DATA_GROUP, ShareType.GROUP);
        sShareTypes.put(DATA_ROOM, ShareType.ROOM);
        sShareTypes.put(DATA_REMOTE, ShareType.FEDERATED);
        sShareTypes.put(DATA_EMAIL, ShareType.EMAIL);
        sShareTypes.put(DATA_CIRCLE, ShareType.CIRCLE);

        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
    }

    private static final Map<String, ShareType> sShareTypes = new HashMap<>();

    public static ShareType getShareType(String authority) {
        return sShareTypes.get(authority);
    }

    private static void setActionShareWith(@NonNull Context context) {
        ACTION_SHARE_WITH = context.getString(R.string.users_and_groups_share_with);
    }

    public ShareesData searchForUsersOrGroups(String userQuery) {
        final SingleSignOnAccount ssoAcc;
        try {
            ssoAcc = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            return repository.getSharees(ssoAcc, userQuery, REQUESTED_PAGE, RESULTS_PER_PAGE).blockingGet();
        } catch (Exception e) {
            Log_OC.e(TAG, "Exception while searching", e);
        }

        return null;
    }

    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        try {
            Bitmap avatar = Glide.with(context)
                    .asBitmap()
                    .load(new SingleSignOnUrl(account.getAccountName(), account.getUrl() + "/index.php/avatar/" + Uri.encode(account.getUserName()) + "/64"))
                    .placeholder(R.drawable.ic_account_circle_grey_24dp)
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .submit()
                    .get();

            // create a file to write bitmap data
            File f = new File(context.getCacheDir(), "test");
            try {
                if (f.exists()) {
                    if (!f.delete()) {
                        throw new IllegalStateException("Existing file could not be deleted!");
                    }
                }
                if (!f.createNewFile()) {
                    throw new IllegalStateException("File could not be created!");
                }

                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                avatar.compress(Bitmap.CompressFormat.PNG, 90, bos);
                byte[] bitmapData = bos.toByteArray();

                //write the bytes in file
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(bitmapData);
                } catch (FileNotFoundException e) {
                    Log_OC.e(TAG, "File not found: " + e.getMessage());
                }

            } catch (Exception e) {
                Log_OC.e(TAG, "Error opening file: " + e.getMessage());
            }

            return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
