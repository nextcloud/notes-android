package it.niedermann.owncloud.notes.share.operations;


import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.net.Uri;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.common.PlainClient;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.accounts.AccountUtils;

import java.io.IOException;

import it.niedermann.owncloud.notes.shared.user.User;

public interface ClientFactory {

    /**
     * This exception wraps all possible errors thrown by trigger-happy OwnCloudClient constructor, making try-catch
     * blocks manageable.
     * <p>
     * This is a temporary refactoring measure, until a better error handling method can be procured.
     */
    @Deprecated
    class CreationException extends Exception {

        private static final long serialVersionUID = 0L;

        CreationException(Throwable t) {
            super(t);
        }
    }

    OwnCloudClient create(User user) throws CreationException;

    NextcloudClient createNextcloudClient(User user) throws CreationException;

    @Deprecated
    OwnCloudClient create(Account account)
            throws OperationCanceledException, AuthenticatorException, IOException,
            AccountUtils.AccountNotFoundException;

    @Deprecated
    OwnCloudClient create(Account account, Activity currentActivity)
            throws OperationCanceledException, AuthenticatorException, IOException,
            AccountUtils.AccountNotFoundException;

    OwnCloudClient create(Uri uri,
                          boolean followRedirects,
                          boolean useNextcloudUserAgent);

    OwnCloudClient create(Uri uri, boolean followRedirects);

    PlainClient createPlainClient();
}
