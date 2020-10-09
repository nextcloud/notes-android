package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.TypeConverter;

import java.util.Calendar;

import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

public class Converters {

    @TypeConverter
    public static DBStatus fromString(String value) {
        return value == null ? null : DBStatus.parse(value);
    }

    @TypeConverter
    public static String dbStatusToString(DBStatus status) {
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
            calendar.setTimeInMillis(value);
        }
        return calendar;
    }

    @TypeConverter
    public static Long calendarToLong(Calendar calendar) {
        return calendar == null ? 0 : calendar.getTimeInMillis();
    }

}
