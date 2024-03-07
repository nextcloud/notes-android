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

import it.niedermann.owncloud.notes.persistence.CapabilitiesWorker;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;

public class Migration_11_12 extends Migration {
    @NonNull
    private final Context context;

    public Migration_11_12(@NonNull Context context) {
        super(11, 12);
        this.context = context;
    }

    /**
     * Adds columns to store the {@link ApiVersion} and the theme colors
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN API_VERSION TEXT");
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN COLOR VARCHAR(6) NOT NULL DEFAULT '000000'");
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN TEXT_COLOR VARCHAR(6) NOT NULL DEFAULT '0082C9'");
        CapabilitiesWorker.update(context);
    }
}
