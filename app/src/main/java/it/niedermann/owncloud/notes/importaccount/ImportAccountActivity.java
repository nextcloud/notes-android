/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.importaccount;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.UnknownErrorException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityImportAccountBinding;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.persistence.ApiProvider;
import it.niedermann.owncloud.notes.persistence.CapabilitiesClient;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

public class ImportAccountActivity extends AppCompatActivity {

    private static final String TAG = ImportAccountActivity.class.getSimpleName();
    public static final int REQUEST_CODE_IMPORT_ACCOUNT = 1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ImportAccountViewModel importAccountViewModel;
    private ActivityImportAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

        binding = ActivityImportAccountBinding.inflate(getLayoutInflater());
        importAccountViewModel = new ViewModelProvider(this).get(ImportAccountViewModel.class);

        setContentView(binding.getRoot());

        binding.welcomeText.setText(getString(R.string.welcome_text, getString(R.string.app_name)));
        binding.addButton.setOnClickListener((v) -> {
            binding.addButton.setEnabled(false);
            binding.status.setVisibility(View.GONE);
            try {
                AccountImporter.pickNewAccount(this);
            } catch (NextcloudFilesAppNotInstalledException e) {
                UiExceptionManager.showDialogForException(this, e);
                Log.w(TAG, "=============================================================");
                Log.w(TAG, "Nextcloud app is not installed. Cannot choose account");
                e.printStackTrace();
            } catch (AndroidGetAccountsPermissionNotGranted e) {
                binding.addButton.setEnabled(true);
                AccountImporter.requestAndroidAccountPermissionsAndPickAccount(this);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        setResult(RESULT_CANCELED);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, ImportAccountActivity.this, ssoAccount -> {
                runOnUiThread(() -> binding.progressCircular.setVisibility(View.VISIBLE));

                SingleAccountHelper.commitCurrentAccount(getApplicationContext(), ssoAccount.name);
                executor.submit(() -> {
                    Log.i(TAG, "Added account: " + "name:" + ssoAccount.name + ", " + ssoAccount.url + ", userId" + ssoAccount.userId);
                    try {
                        Log.i(TAG, "Loading capabilities for " + ssoAccount.name);
                        final var capabilities = CapabilitiesClient.getCapabilities(getApplicationContext(), ssoAccount, null, ApiProvider.getInstance());
                        final String displayName = CapabilitiesClient.getDisplayName(getApplicationContext(), ssoAccount, ApiProvider.getInstance());
                        final var status$ = importAccountViewModel.addAccount(ssoAccount.url, ssoAccount.userId, ssoAccount.name, capabilities, displayName, new IResponseCallback<>() {

                            /**
                             * Update syncing when adding account
                             * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
                             * @param account the account to add
                             */
                            @Override
                            public void onSuccess(Account account) {
                                runOnUiThread(() -> {
                                    Log.i(TAG, capabilities.toString());
                                    BrandingUtil.saveBrandColor(ImportAccountActivity.this, capabilities.getColor());
                                    setResult(RESULT_OK);
                                    finish();
                                });
                                SyncWorker.update(ImportAccountActivity.this, PreferenceManager.getDefaultSharedPreferences(ImportAccountActivity.this)
                                        .getBoolean(getString(R.string.pref_key_background_sync), true));
                            }

                            @Override
                            public void onError(@NonNull Throwable t) {
                                runOnUiThread(() -> {
                                    restoreCleanState();
                                    ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                                });
                            }
                        });
                        runOnUiThread(() -> status$.observe(ImportAccountActivity.this, (status) -> {
                            binding.progressText.setVisibility(View.VISIBLE);
                            Log.v(TAG, "Status: " + status.count + " of " + status.total);
                            if(status.count > 0) {
                                binding.progressCircular.setIndeterminate(false);
                            }
                            binding.progressText.setText(getString(R.string.progress_import, status.count + 1, status.total));
                            binding.progressCircular.setProgress(status.count);
                            binding.progressCircular.setMax(status.total);
                        }));
                    } catch (Throwable t) {
                        t.printStackTrace();
                        ApiProvider.getInstance().invalidateAPICache(ssoAccount);
                        SingleAccountHelper.commitCurrentAccount(this, null);
                        runOnUiThread(() -> {
                            restoreCleanState();
                            if (t instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) t).getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                                binding.status.setText(R.string.error_maintenance_mode);
                                binding.status.setVisibility(View.VISIBLE);
                            } else if (t instanceof NetworkErrorException) {
                                binding.status.setText(getString(R.string.error_sync, getString(R.string.error_no_network)));
                                binding.status.setVisibility(View.VISIBLE);
                            } else if (t instanceof UnknownErrorException && t.getMessage() != null && t.getMessage().contains("No address associated with hostname")) {
                                // https://github.com/nextcloud/notes-android/issues/1014
                                binding.status.setText(R.string.you_have_to_be_connected_to_the_internet_in_order_to_add_an_account);
                                binding.status.setVisibility(View.VISIBLE);
                            } else {
                                ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                            }
                        });
                    }
                });
            });
        } catch (AccountImportCancelledException e) {
            restoreCleanState();
            Log.i(TAG, "Account import has been canceled.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void restoreCleanState() {
        runOnUiThread(() -> {
            binding.addButton.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            binding.progressText.setVisibility(View.GONE);
        });
    }
}