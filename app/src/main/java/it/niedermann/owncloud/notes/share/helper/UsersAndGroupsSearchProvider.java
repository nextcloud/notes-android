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
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.owncloud.android.lib.resources.shares.GetShareesRemoteOperation;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.resources.users.Status;
import com.owncloud.android.lib.resources.users.StatusType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.share.model.UsersAndGroupsSearchConfig;
import it.niedermann.owncloud.notes.share.repository.ShareRepository;

/**
 * Content provider for search suggestions, to search for users and groups existing in an ownCloud server.
 */
public class UsersAndGroupsSearchProvider {

    public static final String SHARE_TYPE = "SHARE_TYPE";
    public static final String SHARE_WITH = "SHARE_WITH";

    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SHARE_WITH,
            SHARE_TYPE
    };

    private static final int SEARCH = 1;

    private static final int RESULTS_PER_PAGE = 50;
    private static final int REQUESTED_PAGE = 1;

    public static String ACTION_SHARE_WITH;

    public static final String CONTENT = "content";

    private final String AUTHORITY;
    private final String DATA_USER;
    private final String DATA_GROUP;
    private final String DATA_ROOM;
    private final String DATA_REMOTE;
    private final String DATA_EMAIL;
    private final String DATA_CIRCLE;

    private final ShareRepository repository;
    private final Context context;

    public UsersAndGroupsSearchProvider(Context context, ShareRepository repository) {
        this.context = context;
        this.repository = repository;

        AUTHORITY = context.getString(R.string.users_and_groups_search_provider_authority);
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

        UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
    }

    private static final Map<String, ShareType> sShareTypes = new HashMap<>();

    private static void setActionShareWith(@NonNull Context context) {
        ACTION_SHARE_WITH = context.getString(R.string.users_and_groups_search_provider_share_with);
    }

    public Cursor searchForUsersOrGroups(String userQuery) throws JSONException {
        final var names = repository.getSharees(userQuery, REQUESTED_PAGE, RESULTS_PER_PAGE);
        MatrixCursor response = null;
        if (!names.isEmpty()) {
            response = new MatrixCursor(COLUMNS);

            Uri userBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_USER).build();
            Uri groupBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_GROUP).build();
            Uri roomBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_ROOM).build();
            Uri remoteBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_REMOTE).build();
            Uri emailBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_EMAIL).build();
            Uri circleBaseUri = new Uri.Builder().scheme(CONTENT).authority(DATA_CIRCLE).build();

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

                if (UsersAndGroupsSearchConfig.INSTANCE.getSearchOnlyUsers() && type != ShareType.USER) {
                    // TODO: Why we need this check?
                    // skip all types but users, as E2E secure share is only allowed to users on same server
                    // continue;
                }

                switch (type) {
                    case GROUP:
                        displayName = userName;
                        icon = R.drawable.ic_group;
                        dataUri = Uri.withAppendedPath(groupBaseUri, shareWith);
                        break;

                    case FEDERATED:
                        // TODO: federatedShareAllowed
                        if (true) {
                            icon = R.drawable.ic_account_circle_grey_24dp;
                            dataUri = Uri.withAppendedPath(remoteBaseUri, shareWith);

                            if (userName.equals(shareWith)) {
                                displayName = name;
                                subline = context.getString(R.string.remote);
                            } else {
                                String[] uriSplitted = shareWith.split("@");
                                displayName = name;
                                subline = context.getString(R.string.share_known_remote_on_clarification,
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

                        icon = builder.build().toString();

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
                            .add(count++)
                            .add(displayName)
                            .add(subline)
                            .add(icon)
                            .add(dataUri)
                            .add(SHARE_WITH, shareWith)
                            .add(SHARE_TYPE, type.getValue());
                }
            }
        }

        return response;
    }
}
