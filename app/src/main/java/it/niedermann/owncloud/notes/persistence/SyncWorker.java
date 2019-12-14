package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import it.niedermann.owncloud.notes.util.ICallback;

public class SyncWorker extends Worker {

    private static final String WORKER_TAG = "background_synchronization";
    private static final String TAG = SyncWorker.class.getCanonicalName();
    private static final Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    synchronized public Result doWork() {
        Log.v(TAG, "Starting background synchronization");
        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getApplicationContext());
        db.getNoteServerSyncHelper().addCallbackPull(new ICallback() {
            @Override
            synchronized public void onFinish() {
                SyncWorker.this.notify();
            }

            @Override
            public void onScheduled() {

            }
        });

        db.getNoteServerSyncHelper().scheduleSync(false);

        try {
            wait();
            Log.v(TAG, "Finished background synchronization");
            return Result.success();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

//    public static void update(@NonNull Context context) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        update(context, sharedPreferences.getString(context.getString(R.string.pref_key_background_sync), context.getString(R.string.pref_value_background_15_minutes)));
//    }
//
//    public static void update(@NonNull Context context, String preferenceValue) {
//        if (context.getString(R.string.pref_value_background_sync_off).equals(preferenceValue)) {
//            deregister(context);
//        } else {
//            int repeatInterval = 15;
//            TimeUnit unit = TimeUnit.MINUTES;
//            if (context.getString(R.string.pref_value_background_1_hour).equals(preferenceValue)) {
//                repeatInterval = 1;
//                unit = TimeUnit.HOURS;
//            } else if (context.getString(R.string.pref_value_background_6_hours).equals(preferenceValue)) {
//                repeatInterval = 6;
//                unit = TimeUnit.HOURS;
//            }
//            PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(SyncWorker.class, repeatInterval, unit)
//                    .setConstraints(constraints).build();
//            Log.v(TAG, "Registering worker running each " + repeatInterval + " " + unit);
//            WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, work);
//        }
//    }

    private static void deregister(@NonNull Context context) {
        Log.v(TAG, "Deregistering all workers with tag \"" + WORKER_TAG + "\"");
        WorkManager.getInstance(context.getApplicationContext()).cancelAllWorkByTag(WORKER_TAG);
    }
}
