package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.model.LocalAccount;

import static java.util.concurrent.TimeUnit.MINUTES;

public class SyncWorker extends Worker {

    private static final String WORKER_TAG = "background_synchronization";
    private static final String TAG = SyncWorker.class.getCanonicalName();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getApplicationContext());
        for (LocalAccount account : db.getAccounts()) {
            try {
                SingleSignOnAccount ssoAccount = AccountImporter.getSingleSignOnAccount(getApplicationContext(), account.getAccountName());
                Log.v(TAG, "Starting background synchronization for " + ssoAccount.name);
                db.getNoteServerSyncHelper().addCallbackPull(ssoAccount, () -> Log.v(TAG, "Finished background synchronization for " + ssoAccount.name));
                db.getNoteServerSyncHelper().scheduleSync(ssoAccount, false);
            } catch (NextcloudFilesAppAccountNotFoundException e) {
                e.printStackTrace();
            }
        }
        // TODO return result depending on callbackPull
        return Result.success();
    }

    public static void register(@NonNull Context context) {
        Log.v(TAG, "Registering worker running each " + 15 + " " + MINUTES);
        WorkManager
                .getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, new PeriodicWorkRequest.Builder(SyncWorker.class, 15, MINUTES)
                        .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()).build());
    }

    private static void deregister(@NonNull Context context) {
        Log.v(TAG, "Deregistering all workers with tag \"" + WORKER_TAG + "\"");
        WorkManager.getInstance(context.getApplicationContext()).cancelAllWorkByTag(WORKER_TAG);
    }
}
