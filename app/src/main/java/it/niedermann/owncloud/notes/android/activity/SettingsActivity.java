package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import at.bitfire.cert4android.CustomCertManager;
import at.bitfire.cert4android.IOnCertificateDecision;
import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.util.ExceptionHandler;
import it.niedermann.owncloud.notes.util.NotesClientUtil;
import it.niedermann.owncloud.notes.util.NotesClientUtil.LoginStatus;

/**
 * Allows to set Settings like URL, Username and Password for Server-Synchronization
 * Created by stefan on 22.09.15.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_URL = "settingsUrl";
    public static final String SETTINGS_USERNAME = "settingsUsername";
    public static final String SETTINGS_PASSWORD = "settingsPassword";
    public static final String SETTINGS_KEY_ETAG = "notes_last_etag";
    public static final String SETTINGS_KEY_LAST_MODIFIED = "notes_last_modified";
    public static final String DEFAULT_SETTINGS = "";
    public static final int CREDENTIALS_CHANGED = 3;

    public static final String LOGIN_URL_DATA_KEY_VALUE_SEPARATOR = ":";
    public static final String WEBDAV_PATH_4_0_AND_LATER = "/remote.php/webdav";

    private SharedPreferences preferences = null;

    @BindView(R.id.settings_url)
    EditText field_url;
    @BindView(R.id.settings_username_wrapper)
    TextInputLayout username_wrapper;
    @BindView(R.id.settings_username)
    EditText field_username;
    @BindView(R.id.settings_password)
    EditText field_password;
    @BindView(R.id.settings_password_wrapper)
    TextInputLayout password_wrapper;
    @BindView(R.id.settings_submit)
    Button btn_submit;
    @BindView(R.id.settings_url_warn_http)
    View urlWarnHttp;
    private String old_password = "";

    private WebView webView;

    private boolean first_run = false;
    private boolean useWebLogin = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if (!NoteServerSyncHelper.isConfigured(this)) {
            first_run = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }

        setupListener();

        // Load current Preferences
        field_url.setText(preferences.getString(SETTINGS_URL, DEFAULT_SETTINGS));
        field_username.setText(preferences.getString(SETTINGS_USERNAME, DEFAULT_SETTINGS));
        old_password = preferences.getString(SETTINGS_PASSWORD, DEFAULT_SETTINGS);

        field_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login();
                return true;
            }
        });
        field_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setPasswordHint(hasFocus);
            }
        });
        setPasswordHint(false);

        handleSubmitButtonEnabled();
    }

    private void setupListener() {
        field_url.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new URLValidatorAsyncTask().execute(NotesClientUtil.formatURL(field_url.getText().toString()));
            }
        });
        field_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = NotesClientUtil.formatURL(field_url.getText().toString());

                if (NotesClientUtil.isHttp(url)) {
                    urlWarnHttp.setVisibility(View.VISIBLE);
                } else {
                    urlWarnHttp.setVisibility(View.GONE);
                }

                handleSubmitButtonEnabled();
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
                handleSubmitButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void setPasswordHint(boolean hasFocus) {
        boolean unchangedHint = !hasFocus && field_password.getText().toString().isEmpty() && !old_password.isEmpty();
        password_wrapper.setHint(getString(unchangedHint ? R.string.settings_password_unchanged : R.string.settings_password));
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Occurs in this scenario: User opens the app but doesn't configure the server settings, they then add the Create Note widget to home screen and configure
        // server settings there. The stale SettingsActivity is then displayed hence finish() here to close it down.
        if ((first_run) && (NoteServerSyncHelper.isConfigured(this))) {
            finish();
        }
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

    private void legacyLogin() {
        String url = field_url.getText().toString().trim();
        String username = field_username.getText().toString();
        String password = field_password.getText().toString();

        if (password.isEmpty()) {
            password = old_password;
        }

        url = NotesClientUtil.formatURL(url);

        new LoginValidatorAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, username, password);
    }

    private void login() {
        if (useWebLogin) {
            webLogin();
        } else {
            legacyLogin();
        }
    }

    /**
     * Obtain the X509Certificate from SslError
     *
     * @param error SslError
     * @return X509Certificate from error
     */
    public static X509Certificate getX509CertificateFromError(SslError error) {
        Bundle bundle = SslCertificate.saveState(error.getCertificate());
        X509Certificate x509Certificate;
        byte[] bytes = bundle.getByteArray("x509-certificate");
        if (bytes == null) {
            x509Certificate = null;
        } else {
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(bytes));
                x509Certificate = (X509Certificate) cert;
            } catch (CertificateException e) {
                x509Certificate = null;
            }
        }
        return x509Certificate;
    }

    private void webLogin() {
        setContentView(R.layout.activity_settings_webview);
        webView = findViewById(R.id.login_webview);
        webView.setVisibility(View.GONE);

        final ProgressBar progressBar = findViewById(R.id.login_webview_progress_bar);

        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccess(false);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString(getWebLoginUserAgent());
        settings.setSaveFormData(false);
        settings.setSavePassword(false);

        Map<String, String> headers = new HashMap<>();
        headers.put("OCS-APIREQUEST", "true");


        webView.loadUrl(normalizeUrlSuffix(NotesClientUtil.formatURL(field_url.getText().toString())) + "index.php/login/flow", headers);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("nc://login/")) {
                    parseAndLoginFromWebView(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                X509Certificate cert = getX509CertificateFromError(error);

                try {
                    final boolean[] accepted = new boolean[1];
                    NoteServerSyncHelper.getInstance(NoteSQLiteOpenHelper.getInstance(getApplicationContext()))
                            .checkCertificate(cert.getEncoded(), true, new IOnCertificateDecision.Stub() {
                                @Override
                                public void accept() {
                                    Log.d("Note", "cert accepted");
                                    handler.proceed();
                                    accepted[0] = true;
                                }

                                @Override
                                public void reject() {
                                    Log.d("Note", "cert rejected");
                                    handler.cancel();
                                }
                            });
                } catch (Exception e) {
                    Log.e("Note", "Cert could not be verified");
                    handler.proceed();
                }
            }

        });

        // show snackbar after 60s to switch back to old login method
        new Handler().postDelayed(() -> {
            Snackbar.make(webView, R.string.fallback_weblogin_text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.fallback_weblogin_back, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initLegacyLogin(field_url.getText().toString());
                    }
                }).show();
        }, 45 * 1000);
    }

    private String getWebLoginUserAgent() {
        return Build.MANUFACTURER.substring(0, 1).toUpperCase(Locale.getDefault()) +
                Build.MANUFACTURER.substring(1).toLowerCase(Locale.getDefault()) + " " + Build.MODEL;
    }

    private void parseAndLoginFromWebView(String dataString) {
        String prefix = "nc://login/";
        LoginUrlInfo loginUrlInfo = parseLoginDataUrl(prefix, dataString);

        if (loginUrlInfo != null) {
            String url = normalizeUrlSuffix(loginUrlInfo.serverAddress);

            new LoginValidatorAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, loginUrlInfo.username,
                    loginUrlInfo.password);
        }
    }

    /**
     * parses a URI string and returns a login data object with the information from the URI string.
     *
     * @param prefix     URI beginning, e.g. cloud://login/
     * @param dataString the complete URI
     * @return login data
     * @throws IllegalArgumentException when
     */
    private LoginUrlInfo parseLoginDataUrl(String prefix, String dataString) throws IllegalArgumentException {
        if (dataString.length() < prefix.length()) {
            throw new IllegalArgumentException("Invalid login URL detected");
        }
        LoginUrlInfo loginUrlInfo = new LoginUrlInfo();

        // format is basically xxx://login/server:xxx&user:xxx&password while all variables are optional
        String data = dataString.substring(prefix.length());

        // parse data
        String[] values = data.split("&");

        if (values.length < 1 || values.length > 3) {
            // error illegal number of URL elements detected
            throw new IllegalArgumentException("Illegal number of login URL elements detected: " + values.length);
        }

        for (String value : values) {
            if (value.startsWith("user" + LOGIN_URL_DATA_KEY_VALUE_SEPARATOR)) {
                loginUrlInfo.username = URLDecoder.decode(
                        value.substring(("user" + LOGIN_URL_DATA_KEY_VALUE_SEPARATOR).length()));
            } else if (value.startsWith("password" + LOGIN_URL_DATA_KEY_VALUE_SEPARATOR)) {
                loginUrlInfo.password = URLDecoder.decode(
                        value.substring(("password" + LOGIN_URL_DATA_KEY_VALUE_SEPARATOR).length()));
            } else if (value.startsWith("server" + LOGIN_URL_DATA_KEY_VALUE_SEPARATOR)) {
                loginUrlInfo.serverAddress = URLDecoder.decode(
                        value.substring(("server" + LOGIN_URL_DATA_KEY_VALUE_SEPARATOR).length()));
            } else {
                // error illegal URL element detected
                throw new IllegalArgumentException("Illegal magic login URL element detected: " + value);
            }
        }

        return loginUrlInfo;
    }

    private String normalizeUrlSuffix(String url) {
        if (url.toLowerCase(Locale.ROOT).endsWith(WEBDAV_PATH_4_0_AND_LATER)) {
            return url.substring(0, url.length() - WEBDAV_PATH_4_0_AND_LATER.length());
        }

        if (!url.endsWith("/")) {
            return url + "/";
        }

        return url;
    }

    private void initLegacyLogin(String oldUrl) {
        useWebLogin = false;
        new URLValidatorAsyncTask().execute(NotesClientUtil.formatURL(field_url.getText().toString()));

        webView.setVisibility(View.INVISIBLE);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);
        setupListener();

        field_url.setText(oldUrl);
        username_wrapper.setVisibility(View.VISIBLE);
        password_wrapper.setVisibility(View.VISIBLE);
    }

    private void handleSubmitButtonEnabled() {
        // drawable[2] is not null if url is valid, see URLValidatorAsyncTask::onPostExecute
        if (useWebLogin || field_url.getCompoundDrawables()[2] != null && (username_wrapper.getVisibility() == View.GONE ||
                (username_wrapper.getVisibility() == View.VISIBLE && field_username.getText().length() > 0))) {
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
            btn_submit.setEnabled(false);
            field_url.setCompoundDrawables(null, null, null, null);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            CustomCertManager ccm = NoteServerSyncHelper.getInstance(NoteSQLiteOpenHelper.getInstance(getApplicationContext())).getCustomCertManager();
            return NotesClientUtil.isValidURL(ccm, params[0]);
        }

        @Override
        protected void onPostExecute(Boolean o) {
            if (o) {
                Drawable actionDoneDark = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_check_grey600_24dp);
                actionDoneDark.setBounds(0, 0, actionDoneDark.getIntrinsicWidth(), actionDoneDark.getIntrinsicHeight());
                field_url.setCompoundDrawables(null, null, actionDoneDark, null);
            } else {
                field_url.setCompoundDrawables(null, null, null, null);
            }
            handleSubmitButtonEnabled();
        }
    }

    /**
     * If Log-In-Credentials are correct, save Credentials to Shared Preferences and finish First Run Wizard.
     */
    private class LoginValidatorAsyncTask extends AsyncTask<String, Void, LoginStatus> {
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
        protected LoginStatus doInBackground(String... params) {
            url = params[0];
            username = params[1];
            password = params[2];
            CustomCertManager ccm = NoteServerSyncHelper.getInstance(NoteSQLiteOpenHelper.getInstance(getApplicationContext())).getCustomCertManager();
            return NotesClientUtil.isValidLogin(ccm, url, username, password);
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            if (LoginStatus.OK.equals(status)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SETTINGS_URL, url);
                editor.putString(SETTINGS_USERNAME, username);
                editor.putString(SETTINGS_PASSWORD, password);
                editor.remove(SETTINGS_KEY_ETAG);
                editor.remove(SETTINGS_KEY_LAST_MODIFIED);
                editor.apply();

                final Intent data = new Intent();
                data.putExtra(NotesListViewActivity.CREDENTIALS_CHANGED, CREDENTIALS_CHANGED);
                setResult(RESULT_OK, data);
                finish();
            } else {
                Log.e("Note", "invalid login");
                btn_submit.setText(R.string.settings_submit);
                setInputsEnabled(true);
                Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_login, getString(status.str)), Toast.LENGTH_LONG).show();
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

    /**
     * Data object holding the login url fields.
     */
    public class LoginUrlInfo {
        String serverAddress;
        String username;
        String password;
    }
}
