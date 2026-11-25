/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2020 Christoph Loy <loy.christoph@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import it.niedermann.owncloud.notes.branding.BrandedActivity;
import it.niedermann.owncloud.notes.exception.ExceptionHandler;

public abstract class LockedActivity extends BrandedActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_prevent_screen_capture), false)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                NotesApplication.updateLastInteraction();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        askToUnlock();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isTaskRoot()) {
            NotesApplication.updateLastInteraction();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        NotesApplication.updateLastInteraction();
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        NotesApplication.updateLastInteraction();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivity(Intent intent) {
        NotesApplication.updateLastInteraction();
        super.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        NotesApplication.updateLastInteraction();
        super.startActivity(intent, options);
    }

    private void askToUnlock() {
        if (NotesApplication.isLocked()) {
            startActivity(new Intent(this, UnlockActivity.class));
        }
    }
}
