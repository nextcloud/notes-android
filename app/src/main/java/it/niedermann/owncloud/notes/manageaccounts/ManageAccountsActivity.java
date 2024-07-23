/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.manageaccounts;

import static it.niedermann.owncloud.notes.branding.BrandingUtil.readBrandMainColorLiveData;
import static it.niedermann.owncloud.notes.shared.util.ApiVersionUtil.getPreferredApiVersion;

import android.accounts.NetworkErrorException;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.branding.DeleteAlertDialogBuilder;
import it.niedermann.owncloud.notes.databinding.ActivityManageAccountsBinding;
import it.niedermann.owncloud.notes.databinding.DialogEditSettingBinding;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;
import it.niedermann.owncloud.notes.shared.model.NotesSettings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageAccountsActivity extends LockedActivity implements IManageAccountsCallback {

    private ActivityManageAccountsBinding binding;
    private ManageAccountsViewModel viewModel;
    private ManageAccountAdapter adapter;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageAccountsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ManageAccountsViewModel.class);

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        adapter = new ManageAccountAdapter(this);
        binding.accounts.setAdapter(adapter);

        viewModel.getAccounts$().observe(this, (accounts) -> {
            if (accounts == null || accounts.size() < 1) {
                finish();
                return;
            }
            this.adapter.setLocalAccounts(accounts);
            viewModel.getCurrentAccount(this, new IResponseCallback<>() {
                @Override
                public void onSuccess(Account result) {
                    runOnUiThread(() -> adapter.setCurrentLocalAccount(result));
                }

                @Override
                public void onError(@NonNull Throwable t) {
                    runOnUiThread(() -> adapter.setCurrentLocalAccount(null));
                    t.printStackTrace();
                }
            });
        });
    }

    public void onSelect(@NonNull Account accountToSelect) {
        adapter.setCurrentLocalAccount(accountToSelect);
        viewModel.selectAccount(accountToSelect, this);
    }

    public void onDelete(@NonNull Account accountToDelete) {
        viewModel.countUnsynchronizedNotes(accountToDelete.getId(), new IResponseCallback<>() {
            @Override
            public void onSuccess(Long unsynchronizedChangesCount) {
                runOnUiThread(() -> {
                    if (unsynchronizedChangesCount > 0) {
                        showRemoveAccountAlertDialog(accountToDelete, unsynchronizedChangesCount);
                    } else {
                        viewModel.deleteAccount(accountToDelete, ManageAccountsActivity.this);
                    }
                });
            }

            @Override
            public void onError(@NonNull Throwable t) {
                ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
            }
        });
    }

    private void showRemoveAccountAlertDialog(@NonNull Account accountToDelete, Long unsynchronizedChangesCount) {
        final MaterialAlertDialogBuilder alertDialogBuilder = new DeleteAlertDialogBuilder(ManageAccountsActivity.this)
                .setTitle(getString(R.string.remove_account, accountToDelete.getUserName()))
                .setMessage(getResources().getQuantityString(R.plurals.remove_account_message, (int) unsynchronizedChangesCount.longValue(), accountToDelete.getAccountName(), unsynchronizedChangesCount))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.simple_remove, (d, l) -> viewModel.deleteAccount(accountToDelete, ManageAccountsActivity.this));

        NotesApplication.brandingUtil().dialog.colorMaterialAlertDialogBackground(this, alertDialogBuilder);

        alertDialogBuilder.show();
    }

    public void onChangeNotesPath(@NonNull Account localAccount) {
        changeAccountSetting(localAccount,
                R.string.settings_notes_path,
                R.string.settings_notes_path_description,
                R.string.settings_notes_path_success,
                NotesSettings::getNotesPath,
                property -> new NotesSettings(property, null)
        );
    }

    public void onChangeFileSuffix(@NonNull Account localAccount) {
        changeAccountSetting(localAccount,
                R.string.settings_file_suffix,
                R.string.settings_file_suffix_description,
                R.string.settings_file_suffix_success,
                NotesSettings::getFileSuffix,
                property -> new NotesSettings(null, property)
        );
    }

    private void changeAccountSetting(@NonNull Account localAccount, @StringRes int title, @StringRes int message, @StringRes int successMessage, @NonNull Function<NotesSettings, String> propertyExtractor, @NonNull Function<String, NotesSettings> settingsFactory) {
        final var repository = NotesRepository.getInstance(getApplicationContext());
        final var binding = DialogEditSettingBinding.inflate(getLayoutInflater());
        final var mainColor$ = readBrandMainColorLiveData(this);
        mainColor$.observe(this, color -> {
            mainColor$.removeObservers(this);
            final var util = BrandingUtil.of(color, this);
            util.material.colorTextInputLayout(binding.inputWrapper);
            util.material.colorProgressBar(binding.progress);
        });

        binding.inputWrapper.setHint(title);

        final MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setView(binding.getRoot())
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.action_edit_save, (v, d) -> {
                    final var property = binding.property.getText().toString();
                    executor.execute(() -> {
                        try {
                            final var putSettingsCall = repository.putServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), settingsFactory.apply(property), getPreferredApiVersion(localAccount.getApiVersion()));
                            putSettingsCall.enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<NotesSettings> call, @NonNull Response<NotesSettings> response) {
                                    final var body = response.body();
                                    if (response.isSuccessful() && body != null) {
                                        runOnUiThread(() -> Toast.makeText(ManageAccountsActivity.this, getString(successMessage, propertyExtractor.apply(body)), Toast.LENGTH_LONG).show());
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(ManageAccountsActivity.this, getString(R.string.http_status_code, response.code()), Toast.LENGTH_LONG).show());
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<NotesSettings> call, @NonNull Throwable t) {
                                    runOnUiThread(() -> ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()));
                                }
                            });
                        } catch (NextcloudFilesAppAccountNotFoundException e) {
                            ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                        }
                    });
                });

        NotesApplication.brandingUtil().dialog.colorMaterialAlertDialogBackground(this, alertDialogBuilder);

        var dialog = alertDialogBuilder.create();
        alertDialogBuilder.show();

        try {
            repository.getServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), getPreferredApiVersion(localAccount.getApiVersion()))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<NotesSettings> call, @NonNull Response<NotesSettings> response) {
                            runOnUiThread(() -> {
                                final var body = response.body();
                                if (response.isSuccessful() && body != null) {
                                    binding.getRoot().removeView(binding.progress);
                                    binding.property.setText(propertyExtractor.apply(body));
                                    binding.inputWrapper.setVisibility(View.VISIBLE);
                                } else {
                                    dialog.dismiss();
                                    ExceptionDialogFragment.newInstance(new NetworkErrorException(getString(R.string.http_status_code, response.code()))).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NonNull Call<NotesSettings> call, @NonNull Throwable t) {
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                            });
                        }
                    });
        } catch (NextcloudFilesAppAccountNotFoundException e) {
            dialog.dismiss();
            ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
        }
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.platform.themeStatusBar(this);
        util.material.themeToolbar(binding.toolbar);
    }
}
