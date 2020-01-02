package it.niedermann.owncloud.notes.model;

import androidx.annotation.StringRes;

import it.niedermann.owncloud.notes.R;

public enum LoginStatus {
    OK(0),
    NO_NETWORK(R.string.error_no_network),
    JSON_FAILED(R.string.error_json),
    PROBLEM_WITH_FILES_APP(R.string.error_files_app),
    UNKNOWN_PROBLEM(R.string.error_unknown);

    @StringRes
    public final int str;

    LoginStatus(@StringRes int str) {
        this.str = str;
    }
}
