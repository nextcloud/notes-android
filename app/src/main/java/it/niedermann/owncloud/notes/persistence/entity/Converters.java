/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

public class Converters {

    @TypeConverter
    public static DBStatus fromString(@Nullable String value) {
        for (DBStatus status : DBStatus.values()) {
            if (status.getTitle().equals(value)) {
                return status;
            }
        }
        return DBStatus.VOID;
    }

    @TypeConverter
    public static String dbStatusToString(@Nullable DBStatus status) {
        return status == null ? null : status.getTitle();
    }

    @TypeConverter
    @NonNull
    public static CategorySortingMethod categorySortingMethodFromString(@Nullable Integer value) {
        return value == null ? CategorySortingMethod.SORT_MODIFIED_DESC : CategorySortingMethod.findById(value);
    }

    @TypeConverter
    @Nullable
    public static Integer dbStatusToString(@Nullable CategorySortingMethod categorySortingMethod) {
        return categorySortingMethod == null ? null : categorySortingMethod.getId();
    }

    @TypeConverter
    public static Calendar calendarFromLong(Long value) {
        Calendar calendar = Calendar.getInstance();
        if (value == null) {
            calendar.setTimeInMillis(0);
        } else {
            calendar.setTimeInMillis(value);
        }
        return calendar;
    }

    @TypeConverter
    public static Long calendarToLong(Calendar calendar) {
        return calendar == null ? 0 : calendar.getTimeInMillis();
    }

}
