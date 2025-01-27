package it.niedermann.owncloud.notes.share;


import android.content.Context;
import android.os.Handler;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.common.User;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import androidx.annotation.NonNull;

public abstract class SyncOperation extends RemoteOperation {
    private final User user;

    public SyncOperation(@NonNull User user) {
        this.user = user;
    }

    public RemoteOperationResult execute(Context context) {
        return super.execute(user, context);
    }

    public RemoteOperationResult execute(@NonNull NextcloudClient client) {
        return run(client);
    }

    public Thread execute(OwnCloudClient client,
                          OnRemoteOperationListener listener,
                          Handler listenerHandler) {
        return super.execute(client, listener, listenerHandler);
    }

}
