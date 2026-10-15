/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.content.Context
import android.util.Log
import androidx.annotation.UiThread
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@GlideModule
class CustomAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
    }

    companion object {
        private val TAG: String = CustomAppGlideModule::class.java.getSimpleName()
        private val clearDiskCacheExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        @JvmStatic
        @UiThread
        fun clearCache(context: Context) {
            Log.i(TAG, "Clearing Glide memory cache")
            Glide.get(context).clearMemory()
            clearDiskCacheExecutor.submit {
                Log.i(TAG, "Clearing Glide disk cache")
                Glide.get(context.applicationContext).clearDiskCache()
            }
        }
    }
}