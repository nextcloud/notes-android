package it.niedermann.owncloud.notes.util;

import androidx.annotation.StringRes;

import it.niedermann.owncloud.notes.R;

/**
 * Utils for Validation etc
 * Created by stefan on 25.09.15.
 */
public class NotesClientUtil {

    public enum LoginStatus {
        OK(0),
        NO_NETWORK(R.string.error_no_network),
        JSON_FAILED(R.string.error_json);

        @StringRes
        public final int str;

        LoginStatus(@StringRes int str) {
            this.str = str;
        }
    }

}