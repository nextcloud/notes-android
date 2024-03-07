/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.work.WorkManager;

import it.niedermann.owncloud.notes.shared.model.Capabilities;

public class Migration_12_13 extends Migration {
    @NonNull
    private final Context context;

    public Migration_12_13(@NonNull Context context) {
        super(12, 13);
        this.context = context;
    }

    /**
     * Adds a column to store the ETag of the server {@link Capabilities}
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN CAPABILITIES_ETAG TEXT");
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("it.niedermann.owncloud.notes.persistence.SyncWorker");
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("SyncWorker");
    }
}
