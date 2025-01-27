package it.niedermann.owncloud.notes.share.operations;


import android.os.AsyncTask;

import com.nextcloud.android.lib.resources.profile.GetHoverCardRemoteOperation;
import com.nextcloud.android.lib.resources.profile.HoverCard;
import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import java.lang.ref.WeakReference;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.share.dialog.ProfileBottomSheetDialog;
import it.niedermann.owncloud.notes.shared.user.User;
import it.niedermann.owncloud.notes.shared.util.DisplayUtils;

public class RetrieveHoverCardAsyncTask extends AsyncTask<Void, Void, HoverCard> {
    private final User user;
    private final Account account;
    private final String userId;
    private final WeakReference<FragmentActivity> activityWeakReference;
    private final ClientFactory clientFactory;

    public RetrieveHoverCardAsyncTask(User user,
                                      Account account,
                                      String userId,
                                      FragmentActivity activity,
                                      ClientFactory clientFactory) {
        this.user = user;
        this.account = account;
        this.userId = userId;
        this.activityWeakReference = new WeakReference<>(activity);
        this.clientFactory = clientFactory;
    }

    @Override
    protected HoverCard doInBackground(Void... voids) {
        try {
            NextcloudClient client = clientFactory.createNextcloudClient(user);
            RemoteOperationResult<HoverCard> result = new GetHoverCardRemoteOperation(userId).execute(client);

            if (result.isSuccess()) {
                return result.getResultData();
            } else {
                return null;
            }
        } catch (ClientFactory.CreationException | NullPointerException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(HoverCard hoverCard) {
        FragmentActivity activity = this.activityWeakReference.get();

        if (activity != null && activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            if (hoverCard.getActions().size() > 0) {
                new ProfileBottomSheetDialog(activity,
                        user,
                        account,
                        hoverCard)
                        .show();
            } else {
                DisplayUtils.showSnackMessage(activity, R.string.no_actions);
            }
        }
    }
}
