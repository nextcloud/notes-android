package it.niedermann.owncloud.notes.util;

import androidx.annotation.StringRes;

import it.niedermann.owncloud.notes.R;

/**
 * Utils for Validation etc
 * Created by stefan on 25.09.15.
 */
public class NotesClientUtil {

    public static final String SETTINGS_KEY_ETAG = "notes_last_etag";
    public static final String SETTINGS_KEY_LAST_MODIFIED = "notes_last_modified";

    public enum LoginStatus {
        OK(0),
        CONNECTION_FAILED(R.string.error_io),
        NO_NETWORK(R.string.error_no_network),
        JSON_FAILED(R.string.error_json);

        @StringRes
        public final int str;

        LoginStatus(@StringRes int str) {
            this.str = str;
        }
    }

}