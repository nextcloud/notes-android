/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NotesTestingUtil {

    private static long currentLong = 1;

    private NotesTestingUtil() {
        // Util class
    }

    /**
     * @see <a href="https://gist.github.com/JoseAlcerreca/1e9ee05dcdd6a6a6fa1cbfc125559bba">Source</a>
     */
    public static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final var data = new Object[1];
        final var latch = new CountDownLatch(1);
        var observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("LiveData value was never set.");
        }
        //noinspection unchecked
        return (T) data[0];
    }

    /**
     * Pretends managing {@link SingleSignOnAccount}s by using own private {@link SharedPreferences}.
     *
     * @param ssoAccount this account will be added
     */
    public static void mockSingleSignOn(@NonNull SingleSignOnAccount ssoAccount) throws IOException {
        final var sharedPrefs = ApplicationProvider.getApplicationContext().getSharedPreferences("TEMP_SHARED_PREFS_" + currentLong++, Context.MODE_PRIVATE);
        sharedPrefs.edit().putString("PREF_ACCOUNT_STRING" + ssoAccount.name, SingleSignOnAccount.toString(ssoAccount)).commit();
        AccountImporter.setSharedPreferences(sharedPrefs);
    }
}
