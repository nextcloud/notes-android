package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.NotesClientUtil;

/**
 * Allows to set Settings like URL, Username and Password for Server-Synchronization
 * Created by stefan on 22.09.15.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_FIRST_RUN = "firstRun";
    public static final String SETTINGS_URL = "settingsUrl";
    public static final String SETTINGS_USERNAME = "settingsUsername";
    public static final String SETTINGS_PASSWORD = "settingsPassword";
    public static final String DEFAULT_SETTINGS = "";
    public static final int CREDENTIALS_CHANGED = 3;

    private SharedPreferences preferences = null;
    private EditText field_url = null;
    private EditText field_username = null;
    private EditText field_password = null;
    private Button btn_submit = null;
    private boolean first_run = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if (preferences.getBoolean(SettingsActivity.SETTINGS_FIRST_RUN, true)) {
            first_run = true;
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        field_url = (EditText) findViewById(R.id.settings_url);
        field_username = (EditText) findViewById(R.id.settings_username);
        field_password = (EditText) findViewById(R.id.settings_password);
        btn_submit = (Button) findViewById(R.id.settings_submit);

        field_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = field_url.getText().toString();

                if (!url.endsWith("/")) {
                    url += "/";
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                new URLValidatorAsyncTask().execute(url);

                if (NotesClientUtil.isHttp(url)) {
                    findViewById(R.id.settings_url_warn_http).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.settings_url_warn_http).setVisibility(View.GONE);
                }

                handleSubmitButtonEnabled(field_url.getText(), field_username.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        field_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleSubmitButtonEnabled(field_url.getText(), field_username.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Load current Preferences
        field_url.setText(preferences.getString(SETTINGS_URL, DEFAULT_SETTINGS));
        field_username.setText(preferences.getString(SETTINGS_USERNAME, DEFAULT_SETTINGS));
        field_password.setText(preferences.getString(SETTINGS_PASSWORD, DEFAULT_SETTINGS));

        field_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login();
                return true;
            }
        });

        btn_submit.setEnabled(false);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    /**
     * Prevent pressing back button on first run
     */
    @Override
    public void onBackPressed() {
        if (!first_run) {
            super.onBackPressed();
        }
    }

    private void login() {
        String url = field_url.getText().toString();
        String username = field_username.getText().toString();
        String password = field_password.getText().toString();

        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        new LoginValidatorAsyncTask().execute(url, username, password);
    }

    private void handleSubmitButtonEnabled(Editable url, Editable username) {
        if (field_username.getText().length() > 0 && field_url.getText().length() > 0) {
            btn_submit.setEnabled(true);
        } else {
            btn_submit.setEnabled(false);
        }
    }

    /************************************ Async Tasks ************************************/

    /**
     * Checks if the given URL returns a valid status code and sets the Check next to the URL-Input Field to visible.
     * Created by stefan on 23.09.15.
     */
    private class URLValidatorAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            ((EditText) findViewById(R.id.settings_url)).setCompoundDrawables(null, null, null, null);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return NotesClientUtil.isValidURL(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean o) {
            if (o) {
                Drawable actionDoneDark = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_done_dark);
                actionDoneDark.setBounds(0, 0, 50, 50);
                ((EditText) findViewById(R.id.settings_url)).setCompoundDrawables(null, null, actionDoneDark, null);
            } else {
                ((EditText) findViewById(R.id.settings_url)).setCompoundDrawables(null, null, null, null);
            }
        }
    }

    /**
     * If Log-In-Credentials are correct, save Credentials to Shared Preferences and finish First Run Wizard.
     */
    private class LoginValidatorAsyncTask extends AsyncTask<String, Void, Boolean> {
        String url, username, password;

        @Override
        protected void onPreExecute() {
            setInputsEnabled(false);
            btn_submit.setText(R.string.settings_submitting);
        }

        /**
         * @param params url, username and password
         * @return isValidLogin Boolean
         */
        @Override
        protected Boolean doInBackground(String... params) {
            url = params[0];
            username = params[1];
            password = params[2];
            return NotesClientUtil.isValidLogin(url, username, password);
        }

        @Override
        protected void onPostExecute(Boolean isValidLogin) {
            if (isValidLogin) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SETTINGS_URL, url);
                editor.putString(SETTINGS_USERNAME, username);
                editor.putString(SETTINGS_PASSWORD, password);
                // Now it is no more First Run
                editor.putBoolean(SETTINGS_FIRST_RUN, false);
                editor.apply();

                NoteSQLiteOpenHelper db = new NoteSQLiteOpenHelper(getApplicationContext());
                db.synchronizeWithServer();

                final Intent data = new Intent();
                //FIXME send correct note back to NotesListView
                data.putExtra(NotesListViewActivity.CREDENTIALS_CHANGED, CREDENTIALS_CHANGED);
                setResult(RESULT_OK, data);
                finish();
            } else {
                Log.e("Note", "invalid login");
                btn_submit.setText(R.string.settings_submit);
                setInputsEnabled(true);
                Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_login), Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Sets all Input-Fields and Buttons to enabled or disabled depending on the given boolean.
         *
         * @param enabled - boolean
         */
        private void setInputsEnabled(boolean enabled) {
            btn_submit.setEnabled(enabled);
            field_url.setEnabled(enabled);
            field_username.setEnabled(enabled);
            field_password.setEnabled(enabled);
        }
    }
}
