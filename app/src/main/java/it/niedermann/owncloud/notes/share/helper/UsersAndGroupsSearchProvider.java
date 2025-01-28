package it.niedermann.owncloud.notes.share.helper;


import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_CLEAR_AT;
import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_ICON;
import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_MESSAGE;
import static com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation.PROPERTY_STATUS;

import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.ApiProvider;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.model.UsersAndGroupsSearchConfig;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;

/**
 * Content provider for search suggestions, to search for users and groups existing in an ownCloud server.
 */
public class UsersAndGroupsSearchProvider extends ContentProvider {

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

    private OwnCloudClient client;
    private Account account;

    public UsersAndGroupsSearchProvider(Account account, OwnCloudClient client) {
        this.account = account;
        this.client = client;
    }

    private static final Map<String, ShareType> sShareTypes = new HashMap<>();

    public static ShareType getShareType(String authority) {

        return sShareTypes.get(authority);
    }

    private static void setActionShareWith(@NonNull Context context) {
        ACTION_SHARE_WITH = context.getString(R.string.users_and_groups_share_with);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // TODO implement
        return null;
    }

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }

        AUTHORITY = getContext().getString(R.string.users_and_groups_search_authority);
        setActionShareWith(getContext());
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

        return true;
    }

    /**
     * returns sharee from server
     *
     * Reference: http://developer.android.com/guide/topics/search/adding-custom-suggestions.html#CustomContentProvider
     *
     * @param uri           Content {@link Uri}, formatted as "content://com.nextcloud.android.providers.UsersAndGroupsSearchProvider/"
     *                      + {@link android.app.SearchManager#SUGGEST_URI_PATH_QUERY} + "/" +
     *                      'userQuery'
     * @param projection    Expected to be NULL.
     * @param selection     Expected to be NULL.
     * @param selectionArgs Expected to be NULL.
     * @param sortOrder     Expected to be NULL.
     * @return Cursor with possible sharees in the server that match 'query'.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log_OC.d(TAG, "query received in thread " + Thread.currentThread().getName());

        int match = mUriMatcher.match(uri);
        if (match == SEARCH) {
            return searchForUsersOrGroups(uri);
        }
        return null;
    }

    private Cursor searchForUsersOrGroups(Uri uri) {

        // TODO check searchConfig and filter results
        Log.d(TAG, "searchForUsersOrGroups: searchConfig only users: " +  UsersAndGroupsSearchConfig.INSTANCE.getSearchOnlyUsers());

        String lastPathSegment = uri.getLastPathSegment();

        if (lastPathSegment == null) {
            throw new IllegalArgumentException("Wrong URI passed!");
        }

        String userQuery = lastPathSegment.toLowerCase(Locale.ROOT);

        // ApiProvider.getInstance().getFilesAPI(getContext(), account, ApiVersion.API_VERSION_1_0).getDirectEditingInfo();


        // request to the OC server about users and groups matching userQuery
        GetShareesRemoteOperation searchRequest = new GetShareesRemoteOperation(userQuery,
                REQUESTED_PAGE,
                RESULTS_PER_PAGE);
        RemoteOperationResult<ArrayList<JSONObject>> result = searchRequest.execute(client);
        List<JSONObject> names = new ArrayList<>();

        if (result.isSuccess()) {
            names = result.getResultData();
        } else {
            showErrorMessage(result);
        }

        MatrixCursor response = null;
        // convert the responses from the OC server to the expected format
        if (names.size() > 0) {
            if (getContext() == null) {
                throw new IllegalArgumentException("Context may not be null!");
            }

            response = new MatrixCursor(COLUMNS);

            Uri userBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_USER).build();
            Uri groupBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_GROUP).build();
            Uri roomBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_ROOM).build();
            Uri remoteBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_REMOTE).build();
            Uri emailBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_EMAIL).build();
            Uri circleBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_CIRCLE).build();

            try {
                Iterator<JSONObject> namesIt = names.iterator();
                JSONObject item;
                String displayName;
                String subline = null;
                Object icon = 0;
                Uri dataUri;
                int count = 0;
                while (namesIt.hasNext()) {
                    item = namesIt.next();
                    dataUri = null;
                    displayName = null;
                    String userName = item.getString(GetShareesRemoteOperation.PROPERTY_LABEL);
                    String name = item.isNull("name") ? "" : item.getString("name");
                    JSONObject value = item.getJSONObject(GetShareesRemoteOperation.NODE_VALUE);
                    ShareType type = ShareType.fromValue(value.getInt(GetShareesRemoteOperation.PROPERTY_SHARE_TYPE));
                    String shareWith = value.getString(GetShareesRemoteOperation.PROPERTY_SHARE_WITH);

                    Status status;
                    JSONObject statusObject = item.optJSONObject(PROPERTY_STATUS);

                    if (statusObject != null) {
                        status = new Status(
                                StatusType.valueOf(statusObject.getString(PROPERTY_STATUS).toUpperCase(Locale.US)),
                                statusObject.isNull(PROPERTY_MESSAGE) ? "" : statusObject.getString(PROPERTY_MESSAGE),
                                statusObject.isNull(PROPERTY_ICON) ? "" : statusObject.getString(PROPERTY_ICON),
                                statusObject.isNull(PROPERTY_CLEAR_AT) ? -1 : statusObject.getLong(PROPERTY_CLEAR_AT));
                    } else {
                        status = new Status(StatusType.OFFLINE, "", "", -1);
                    }

                    if ( UsersAndGroupsSearchConfig.INSTANCE.getSearchOnlyUsers() && type != ShareType.USER) {
                        // skip all types but users, as E2E secure share is only allowed to users on same server
                        continue;
                    }

                    switch (type) {
                        case GROUP:
                            displayName = userName;
                            icon = R.drawable.ic_group;
                            dataUri = Uri.withAppendedPath(groupBaseUri, shareWith);
                            break;

                        case FEDERATED:
                            if (true) {
                                icon = R.drawable.ic_account_circle_grey_24dp;
                                dataUri = Uri.withAppendedPath(remoteBaseUri, shareWith);

                                if (userName.equals(shareWith)) {
                                    displayName = name;
                                    subline = getContext().getString(R.string.remote);
                                } else {
                                    String[] uriSplitted = shareWith.split("@");
                                    displayName = name;
                                    subline = getContext().getString(R.string.share_known_remote_on_clarification,
                                            uriSplitted[uriSplitted.length - 1]);
                                }
                            }
                            break;

                        case USER:
                            displayName = userName;
                            subline = (status.getMessage() == null || status.getMessage().isEmpty()) ? null :
                                    status.getMessage();
                            Uri.Builder builder = Uri.parse("content://" + AUTHORITY + "/icon").buildUpon();

                            builder.appendQueryParameter("shareWith", shareWith);
                            builder.appendQueryParameter("displayName", displayName);
                            builder.appendQueryParameter("status", status.getStatus().toString());

                            if (!TextUtils.isEmpty(status.getIcon()) && !"null".equals(status.getIcon())) {
                                builder.appendQueryParameter("icon", status.getIcon());
                            }

                            icon = builder.build();

                            dataUri = Uri.withAppendedPath(userBaseUri, shareWith);
                            break;

                        case EMAIL:
                            icon = R.drawable.ic_email;
                            displayName = name;
                            subline = shareWith;
                            dataUri = Uri.withAppendedPath(emailBaseUri, shareWith);
                            break;

                        case ROOM:
                            icon = R.drawable.ic_talk;
                            displayName = userName;
                            dataUri = Uri.withAppendedPath(roomBaseUri, shareWith);
                            break;

                        case CIRCLE:
                            icon = R.drawable.ic_circles;
                            displayName = userName;
                            dataUri = Uri.withAppendedPath(circleBaseUri, shareWith);
                            break;

                        default:
                            break;
                    }

                    if (displayName != null && dataUri != null) {
                        response.newRow()
                                .add(count++)             // BaseColumns._ID
                                .add(displayName)         // SearchManager.SUGGEST_COLUMN_TEXT_1
                                .add(subline)             // SearchManager.SUGGEST_COLUMN_TEXT_2
                                .add(icon)                // SearchManager.SUGGEST_COLUMN_ICON_1
                                .add(dataUri);
                    }
                }

            } catch (JSONException e) {
                Log_OC.e(TAG, "Exception while parsing data of users/groups", e);
            }
        }

        return response;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        try {
            Bitmap avatar = Glide.with(getContext())
                    .asBitmap()
                    .load(new SingleSignOnUrl(account.getAccountName(), account.getUrl() + "/index.php/avatar/" + Uri.encode(account.getUserName()) + "/64"))
                    .placeholder(R.drawable.ic_account_circle_grey_24dp)
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .submit()
                    .get();

            // create a file to write bitmap data
            File f = new File(getContext().getCacheDir(), "test");
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

    /**
     * Show error message
     *
     * @param result Result with the failure information.
     */
    private void showErrorMessage(final RemoteOperationResult result) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            // The Toast must be shown in the main thread to grant that will be hidden correctly; otherwise
            // the thread may die before, an exception will occur, and the message will be left on the screen
            // until the app dies

            Context context = getContext();

            if (context == null) {
                throw new IllegalArgumentException("Context may not be null!");
            }

            Toast.makeText(getContext().getApplicationContext(),
                    result.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }


}
