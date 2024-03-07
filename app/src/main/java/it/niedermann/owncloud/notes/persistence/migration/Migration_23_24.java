/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Remove <code>textColor</code> property from {@link android.content.SharedPreferences} and the
 * database as it is no longer needed for theming.
 */
public class Migration_23_24 extends Migration {

    @NonNull
    private final Context context;

    public Migration_23_24(@NonNull Context context) {
        super(23, 24);
        this.context = context;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("branding_text").apply();
    }
}
