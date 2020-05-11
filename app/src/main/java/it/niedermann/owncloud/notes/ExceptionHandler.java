package it.niedermann.owncloud.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.android.activity.ExceptionActivity;


public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = ExceptionHandler.class.getSimpleName();
    private Context context;
    private Class<? extends Activity> errorActivity;
    public static final String KEY_THROWABLE = "T";

    public ExceptionHandler(Context context) {
        super();
        this.context = context;
        this.errorActivity = ExceptionActivity.class;
    }

    public ExceptionHandler(Context context, Class<? extends Activity> errorActivity) {
        super();
        this.context = context;
        this.errorActivity = errorActivity;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e(TAG, e.getMessage(), e);
        Intent intent = new Intent(context.getApplicationContext(), errorActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle extras = new Bundle();
        intent.putExtra(KEY_THROWABLE, e);
        extras.putSerializable(KEY_THROWABLE, e);
        intent.putExtras(extras);
        context.getApplicationContext().startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
        Runtime.getRuntime().exit(0);
    }
}
