/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.exception;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = ExceptionHandler.class.getSimpleName();

    @NonNull
    private final Activity activity;

    public ExceptionHandler(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e(TAG, e.getMessage(), e);
        activity.getApplicationContext().startActivity(ExceptionActivity.createIntent(activity.getApplicationContext(), e));
        activity.finish();
        Runtime.getRuntime().exit(0);
    }
}
