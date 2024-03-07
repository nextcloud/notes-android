/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@GlideModule
public class CustomAppGlideModule extends AppGlideModule {

    private static final String TAG = CustomAppGlideModule.class.getSimpleName();
    private static final ExecutorService clearDiskCacheExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
    }

    @UiThread
    public static void clearCache(@NonNull Context context) {
        Log.i(TAG, "Clearing Glide memory cache");
        Glide.get(context).clearMemory();
        clearDiskCacheExecutor.submit(() -> {
            Log.i(TAG, "Clearing Glide disk cache");
            Glide.get(context.getApplicationContext()).clearDiskCache();
        });
    }
}