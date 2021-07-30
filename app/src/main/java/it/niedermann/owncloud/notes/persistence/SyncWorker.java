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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public class SyncWorker extends Worker {

    private static final String TAG = Objects.requireNonNull(SyncWorker.class.getSimpleName());
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
        final var repo = NotesRepository.getInstance(getApplicationContext());
        for (final var account : repo.getAccounts()) {
            Log.v(TAG, "Starting background synchronization for " + account.getAccountName());
            repo.addCallbackPull(account, () -> Log.v(TAG, "Finished background synchronization for " + account.getAccountName()));
            repo.scheduleSync(account, false);
        }
        // TODO return result depending on callbackPull
        return Result.success();
    }

    /**
     * Set up sync work to enabled every 15 minutes or just disabled
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/1168
     * @param context the application
     * @param backgroundSync the toggle result backgroundSync
     */

    public static void update(@NonNull Context context, boolean backgroundSync) {
        deregister(context);
        if (backgroundSync) {
            final var work = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                    .setConstraints(constraints).build();
            WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, work);
            Log.i(TAG, "Registering worker running each " + 15 + " " + TimeUnit.MINUTES);
        }
    }

    private static void deregister(@NonNull Context context) {
        Log.i(TAG, "Deregistering all workers with tag \"" + WORKER_TAG + "\"");
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(WORKER_TAG);
    }
}
