package it.niedermann.owncloud.notes.persistence.entity;

import androidx.room.TypeConverter;

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

}
