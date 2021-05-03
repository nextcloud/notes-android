package it.niedermann.owncloud.notes.importaccount;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.nextcloud.android.sso.exceptions.UnknownErrorException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityImportAccountBinding;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.persistence.ApiProvider;
import it.niedermann.owncloud.notes.persistence.CapabilitiesClient;
import it.niedermann.owncloud.notes.persistence.SSOClient;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

public class ImportAccountActivity extends AppCompatActivity {

    private static final String TAG = ImportAccountActivity.class.getSimpleName();
    public static final int REQUEST_CODE_IMPORT_ACCOUNT = 1;

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

                SingleAccountHelper.setCurrentAccount(getApplicationContext(), ssoAccount.name);
                new Thread(() -> {
                    Log.i(TAG, "Added account: " + "name:" + ssoAccount.name + ", " + ssoAccount.url + ", userId" + ssoAccount.userId);
                    try {
                        Log.i(TAG, "Loading capabilities for " + ssoAccount.name);
                        final Capabilities capabilities = CapabilitiesClient.getCapabilities(getApplicationContext(), ssoAccount, null);
                        importAccountViewModel.addAccount(ssoAccount.url, ssoAccount.userId, ssoAccount.name, capabilities, new IResponseCallback<Account>() {

                            /**
                             * Update syncing when adding account
                             * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
                             * @param account the account to add
                             */
                            @Override
                            public void onSuccess(Account account) {
                                Context context = getApplicationContext();
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                SyncWorker.update(context, !sharedPreferences.getString("backgroundSync", "on").equals("off"));
                                runOnUiThread(() -> {
                                    Log.i(TAG, capabilities.toString());
                                    BrandingUtil.saveBrandColors(ImportAccountActivity.this, capabilities.getColor(), capabilities.getTextColor());
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            }

                            @Override
                            public void onError(@NonNull Throwable t) {
                                runOnUiThread(() -> {
                                    binding.addButton.setEnabled(true);
                                    ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                                });
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                        SSOClient.invalidateAPICache(ssoAccount);
                        SingleAccountHelper.setCurrentAccount(this, null);
                        runOnUiThread(() -> {
                            restoreCleanState();
                            if (e instanceof UnknownErrorException && e.getMessage().contains("No address associated with hostname")) {
                                // https://github.com/stefan-niedermann/nextcloud-notes/issues/1014
                                binding.status.setText(R.string.you_have_to_be_connected_to_the_internet_in_order_to_add_an_account);
                                binding.status.setVisibility(View.VISIBLE);
                            } else {
                                ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                            }
                        });
                    }
                }).start();
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
        });
    }
}