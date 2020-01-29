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
        Log.v(TAG, "Starting background synchronization");
        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getApplicationContext());
        db.getNoteServerSyncHelper().addCallbackPull(() -> Log.v(TAG, "Finished background synchronization"));
        db.getNoteServerSyncHelper().scheduleSync(false);
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
