package it.niedermann.owncloud.notes.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;

import it.niedermann.owncloud.notes.android.activity.ExceptionActivity;

import static it.niedermann.owncloud.notes.android.activity.ExceptionActivity.KEY_THROWABLE;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity context;

    public ExceptionHandler(Activity context) {
        super();
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        Intent intent = new Intent(context.getApplicationContext(), ExceptionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle extras = new Bundle();
        intent.putExtra(KEY_THROWABLE, (Serializable) e);
        extras.putSerializable(KEY_THROWABLE, e);
        intent.putExtras(extras);
        context.getApplicationContext().startActivity(intent);
        context.finish();
        Runtime.getRuntime().exit(0);
    }
}
