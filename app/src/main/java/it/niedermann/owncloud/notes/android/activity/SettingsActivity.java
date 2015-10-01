package it.niedermann.owncloud.notes.android.activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.NotesClientUtil;

/**
 * Allows to set Settings like URL, Username and Password for Server-Synchronization
 * Created by stefan on 22.09.15.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SETTINGS_FIRST_RUN = "firstRun";
    public static final String SETTINGS_URL = "settingsUrl";
    public static final String SETTINGS_USERNAME = "settingsUsername";
    public static final String SETTINGS_PASSWORD = "settingsPassword";
    public static final String DEFAULT_SETTINGS = "";

    private SharedPreferences preferences = null;
    private EditText field_url = null;
    private EditText field_username = null;
    private EditText field_password = null;
    private Button btn_submit = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        field_url = (EditText) findViewById(R.id.settings_url);
        field_username = (EditText) findViewById(R.id.settings_username);
        field_password = (EditText) findViewById(R.id.settings_password);
        btn_submit = (Button) findViewById(R.id.settings_submit);

        field_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = ((EditText) findViewById(R.id.settings_url)).getText().toString();
                new URLValidatorAsyncTask().execute(url);

                if (NotesClientUtil.isHttp(url)) {
                    findViewById(R.id.settings_url_warn_http).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.settings_url_warn_http).setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load current Preferences
        field_url.setText(preferences.getString(SETTINGS_URL, DEFAULT_SETTINGS));
        field_username.setText(preferences.getString(SETTINGS_USERNAME, DEFAULT_SETTINGS));
        field_password.setText(preferences.getString(SETTINGS_PASSWORD, DEFAULT_SETTINGS));

        btn_submit.setOnClickListener(this);
    }

    /**
     * Handle Submit Button Click
     * Checks and Writes the new Preferences into the SharedPreferences Object.
     *
     * @param v View
     */
    @Override
    public void onClick(View v) {
        String url = field_url.getText().toString();
        String username = field_username.getText().toString();
        String password = field_password.getText().toString();

        if (!url.endsWith("/")) {
            url += "/";
        }

        new LoginValidatorAsyncTask().execute(url, username, password);
    }

    /************************************ Async Tasks ************************************/

    /**
     * Checks if the given URL returns a valid status code and sets the Check next to the URL-Input Field to visible.
     * Created by stefan on 23.09.15.
     */
    private class URLValidatorAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.settings_url_check).setVisibility(View.INVISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return NotesClientUtil.isValidURL(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean o) {
            Log.v("Note", "Set Visible: " + o);
            if (o) {
                findViewById(R.id.settings_url_check).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.settings_url_check).setVisibility(View.INVISIBLE);
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
            btn_submit.setEnabled(false);
        }

        /**
         * @param params url, username and password
         * @return isValidLogin
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
            if(isValidLogin) {
                Log.v("Note", "Valid Credentials.");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SETTINGS_URL, url);
                editor.putString(SETTINGS_USERNAME, username);
                editor.putString(SETTINGS_PASSWORD, password);

                // Now it is no more First Run
                Log.v("Note", "set First_Run to false.");
                editor.putBoolean(SETTINGS_FIRST_RUN, false);

                editor.apply();

                NoteSQLiteOpenHelper db = new NoteSQLiteOpenHelper(getApplicationContext());
                db.synchronizeWithServer();

                finish();
            } else {
                Log.v("Note", "Invalid Credentials!");
                btn_submit.setEnabled(true);
                //TODO Show Error Message
            }
        }
    }
}