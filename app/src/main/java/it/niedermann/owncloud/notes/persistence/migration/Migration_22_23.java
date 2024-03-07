/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.OnConflictStrategy;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import it.niedermann.owncloud.notes.persistence.ApiProvider;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;

/**
 * Add <code>displayName</code> property to {@link Account}.
 * <p>
 * See: <a href="https://github.com/nextcloud/notes-android/issues/1079">#1079 Show DisplayName instead of uid attribute for LDAP users</a>
 * <p>
 * Sanitizes the stored API versions in the database.
 */
public class Migration_22_23 extends Migration {

    public Migration_22_23() {
        super(22, 23);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        addDisplayNameToAccounts(db);
        sanitizeAccounts(db);
    }

    private static void addDisplayNameToAccounts(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE Account ADD COLUMN displayName TEXT");
    }

    private static void sanitizeAccounts(@NonNull SupportSQLiteDatabase db) {
        final var cursor = db.query("SELECT id, apiVersion FROM ACCOUNT", null);
        final var values = new ContentValues(1);

        final int COLUMN_POSITION_ID = cursor.getColumnIndex("id");
        final int COLUMN_POSITION_API_VERSION = cursor.getColumnIndex("apiVersion");

        while (cursor.moveToNext()) {
            values.put("APIVERSION", sanitizeApiVersion(cursor.getString(COLUMN_POSITION_API_VERSION)));
            db.update("ACCOUNT", OnConflictStrategy.REPLACE, values, "ID = ?", new String[]{String.valueOf(cursor.getLong(COLUMN_POSITION_ID))});
        }
        cursor.close();
        ApiProvider.getInstance().invalidateAPICache();
    }

    @Nullable
    public static String sanitizeApiVersion(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }

        JSONArray a;
        try {
            a = new JSONArray(raw);
        } catch (JSONException e) {
            try {
                a = new JSONArray("[" + raw + "]");
            } catch (JSONException e1) {
                return null;
            }
        }

        final var result = new ArrayList<ApiVersion>();
        for (int i = 0; i < a.length(); i++) {
            try {
                final var version = ApiVersion.of(a.getString(i));
                if (version.getMajor() != 0 || version.getMinor() != 0) {
                    result.add(version);
                }
            } catch (Exception ignored) {
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        return "[" +
                result
                        .stream()
                        .filter(Objects::nonNull)
                        .map(v -> v.getMajor() + "." + v.getMinor())
                        .collect(Collectors.joining(","))
                + "]";
    }
}
