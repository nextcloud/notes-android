package it.niedermann.owncloud.notes.util;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.Constants;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import static com.nextcloud.android.sso.AccountImporter.CHOOSE_ACCOUNT_SSO;

public class SSOUtil {

    private static final String TAG = SSOUtil.class.getSimpleName();

    private SSOUtil() {

    }

    /**
     * Opens a dialog which allows the user to pick a Nextcloud account (which previously has to be configured in the files app).
     * Also allows to configure a new Nextcloud account in the files app and directly import it.
     *
     * @param activity should implement AccountImporter.onActivityResult
     */
    public static void askForNewAccount(@NonNull Activity activity) {
        try {
            AccountImporter.pickNewAccount(activity);
        } catch (NextcloudFilesAppNotInstalledException e1) {
            UiExceptionManager.showDialogForException(activity, e1);
            Log.w(SSOUtil.class.toString(), "=============================================================");
            Log.w(SSOUtil.class.toString(), "Nextcloud app is not installed. Cannot choose account");
            e1.printStackTrace();
        } catch (AndroidGetAccountsPermissionNotGranted e2) {
            AccountImporter.requestAndroidAccountPermissionsAndPickAccount(activity);
        }
    }

    /**
     * Opens the same dialog like AccountImporter.pickNewAccount() but preselects the given account
     *
     * @param activity should implement CHOOSE_ACCOUNT_SSO in onActivityResult
     * @param accountName account that should be preselected
     */
    public static void authorizeExistingAccount(@NonNull Activity activity, @NonNull String accountName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permission not granted.");
                // Well... do you want to use this SSO account or not?
                return;
            }
        }

        Log.d(TAG, "Permission granted.");
        Intent intent = AccountManager.newChooseAccountIntent(
                new Account(accountName, Constants.ACCOUNT_TYPE_PROD),
                null,
                new String[]{Constants.ACCOUNT_TYPE_PROD, Constants.ACCOUNT_TYPE_DEV},
                true,
                null,
                null,
                null,
                null
        );
        activity.startActivityForResult(intent, CHOOSE_ACCOUNT_SSO);
    }
}
