package it.niedermann.owncloud.notes.util;

import android.app.Activity;
import android.util.Log;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import it.niedermann.owncloud.notes.android.activity.NotesListViewActivity;

public class SSOUtil {

    private SSOUtil() {

    }

    public static void askForNewAccount(Activity activity) {
        try {
            AccountImporter.pickNewAccount(activity);
        } catch (NextcloudFilesAppNotInstalledException e1) {
            UiExceptionManager.showDialogForException(activity, e1);
            Log.w(NotesListViewActivity.class.toString(), "=============================================================");
            Log.w(NotesListViewActivity.class.toString(), "Nextcloud app is not installed. Cannot choose account");
            e1.printStackTrace();
        } catch (AndroidGetAccountsPermissionNotGranted e2) {
            AccountImporter.requestAndroidAccountPermissionsAndPickAccount(activity);
        }
    }
}
