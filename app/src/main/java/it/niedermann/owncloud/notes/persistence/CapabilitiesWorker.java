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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.model.LocalAccount;

public class CapabilitiesWorker extends Worker {

    private static final String TAG = Objects.requireNonNull(CapabilitiesWorker.class.getCanonicalName());
    private static final String WORKER_TAG = "capabilities";

    private static final Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    private static final PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(CapabilitiesWorker.class, 24, TimeUnit.HOURS)
            .setConstraints(constraints).build();

    public CapabilitiesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotesDatabase db = NotesDatabase.getInstance(getApplicationContext());
        for (LocalAccount account : db.getAccounts()) {
            try {
                SingleSignOnAccount ssoAccount = AccountImporter.getSingleSignOnAccount(getApplicationContext(), account.getAccountName());
                Log.i(TAG, "Refreshing capabilities for " + ssoAccount.name);
            } catch (NextcloudFilesAppAccountNotFoundException e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

    public static void update(@NonNull Context context) {
        deregister(context);
        Log.i(TAG, "Registering worker running each 24 hours.");
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, work);
    }

    private static void deregister(@NonNull Context context) {
        Log.i(TAG, "Deregistering all workers with tag \"" + WORKER_TAG + "\"");
        WorkManager.getInstance(context.getApplicationContext()).cancelAllWorkByTag(WORKER_TAG);
    }
}
