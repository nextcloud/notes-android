package it.niedermann.owncloud.notes.persistence.entity;

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
    public static CategorySortingMethod categorySortingMethodFromString(Integer value) {
        return value == null ? CategorySortingMethod.SORT_MODIFIED_DESC : CategorySortingMethod.getCSM(value);
    }

    @TypeConverter
    public static Integer dbStatusToString(CategorySortingMethod categorySortingMethod) {
        return categorySortingMethod == null ? null : categorySortingMethod.getCSMID();
    }

    @TypeConverter
    public static Calendar calendarFromLong(Long value) {
        Calendar calendar = Calendar.getInstance();
        if (value == null) {
            calendar.setTimeInMillis(0);
        } else {
            calendar.setTimeInMillis(value * 1000);
        }
        return calendar;
    }

    @TypeConverter
    public static Long calendarToLong(Calendar calendar) {
        return calendar == null ? 0 : calendar.getTimeInMillis() / 1000;
    }

}
