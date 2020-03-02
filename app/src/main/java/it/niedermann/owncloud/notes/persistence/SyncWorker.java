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

import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.LocalAccount;

public class SyncWorker extends Worker {

    private static final String TAG = SyncWorker.class.getCanonicalName();
    private static final String WORKER_TAG = "background_synchronization";

    private static final Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotesDatabase db = NotesDatabase.getInstance(getApplicationContext());
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

    public static void update(@NonNull Context context, @NonNull String preferenceValue) {
        deregister(context);
        if (!context.getString(R.string.pref_value_sync_off).equals(preferenceValue)) {
            int repeatInterval = 15;
            TimeUnit unit = TimeUnit.MINUTES;
            if (context.getString(R.string.pref_value_sync_1_hour).equals(preferenceValue)) {
                repeatInterval = 1;
                unit = TimeUnit.HOURS;
            } else if (context.getString(R.string.pref_value_sync_6_hours).equals(preferenceValue)) {
                repeatInterval = 6;
                unit = TimeUnit.HOURS;
            }
            PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(SyncWorker.class, repeatInterval, unit)
                    .setConstraints(constraints).build();
            WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, work);
            Log.v(TAG, "Registering worker running each " + repeatInterval + " " + unit);
        }
    }

    private static void deregister(@NonNull Context context) {
        Log.v(TAG, "Deregistering all workers with tag \"" + WORKER_TAG + "\"");
        WorkManager.getInstance(context.getApplicationContext()).cancelAllWorkByTag(WORKER_TAG);
    }
}
