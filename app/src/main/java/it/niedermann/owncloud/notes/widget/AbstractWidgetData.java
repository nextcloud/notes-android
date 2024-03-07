/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget;

import androidx.annotation.IntRange;
import androidx.room.PrimaryKey;

public abstract class AbstractWidgetData {

    @PrimaryKey
    private int id;
    private long accountId;
    @IntRange(from = 0, to = 2)
    private int themeMode;

    protected AbstractWidgetData() {
        // Default constructor
    }

    protected AbstractWidgetData(int id, long accountId, @IntRange(from = 0, to = 2) int themeMode) {
        this.id = id;
        this.accountId = accountId;
        this.themeMode = themeMode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @IntRange(from = 0, to = 2)
    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(@IntRange(from = 0, to = 2) int themeMode) {
        this.themeMode = themeMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractWidgetData that)) return false;

        if (id != that.id) return false;
        if (accountId != that.accountId) return false;
        return themeMode == that.themeMode;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + themeMode;
        return result;
    }
}
