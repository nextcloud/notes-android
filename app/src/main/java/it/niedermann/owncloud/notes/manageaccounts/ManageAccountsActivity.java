package it.niedermann.owncloud.notes.manageaccounts;

import android.accounts.NetworkErrorException;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedAlertDialogBuilder;
import it.niedermann.owncloud.notes.branding.BrandedDeleteAlertDialogBuilder;
import it.niedermann.owncloud.notes.databinding.ActivityManageAccountsBinding;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;
import it.niedermann.owncloud.notes.shared.model.NotesSettings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

public class ManageAccountsActivity extends LockedActivity {

    private ActivityManageAccountsBinding binding;
    private ManageAccountsViewModel viewModel;
    private ManageAccountAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageAccountsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(ManageAccountsViewModel.class);

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        adapter = new ManageAccountAdapter(
                this::selectAccount,
                this::deleteAccount,
                this::onChangeNotesPath,
                this::onChangeFileSuffix
        );
        binding.accounts.setAdapter(adapter);

        viewModel.getAccounts$().observe(this, (accounts) -> {
            if (accounts == null || accounts.size() < 1) {
                finish();
                return;
            }
            this.adapter.setLocalAccounts(accounts);
            viewModel.getCurrentAccount(this, new IResponseCallback<Account>() {
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

    private void selectAccount(@NonNull Account accountToSelect) {
        viewModel.selectAccount(accountToSelect, this);
    }

    private void deleteAccount(@NonNull Account accountToDelete) {
        viewModel.countUnsynchronizedNotes(accountToDelete.getId(), new IResponseCallback<Long>() {
            @Override
            public void onSuccess(Long unsynchronizedChangesCount) {
                runOnUiThread(() -> {
                    if (unsynchronizedChangesCount > 0) {
                        new BrandedDeleteAlertDialogBuilder(ManageAccountsActivity.this)
                                .setTitle(getString(R.string.remove_account, accountToDelete.getUserName()))
                                .setMessage(getResources().getQuantityString(R.plurals.remove_account_message, (int) unsynchronizedChangesCount.longValue(), accountToDelete.getAccountName(), unsynchronizedChangesCount))
                                .setNeutralButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.simple_remove, (d, l) -> viewModel.deleteAccount(accountToDelete, ManageAccountsActivity.this))
                                .show();
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

    private void onChangeNotesPath(@NonNull Account localAccount) {
        final NotesRepository repository = NotesRepository.getInstance(getApplicationContext());
        final EditText editText = new EditText(this);
        editText.setEnabled(false);
        final View wrapper = createDialogViewWrapper(editText);
        final AlertDialog dialog = new BrandedAlertDialogBuilder(this)
                .setTitle(R.string.settings_notes_path)
                .setMessage("Folder to store your notes in your  Nextcloud")
                .setView(wrapper)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.action_edit_save, (v, d) -> new Thread(() -> {
                    try {
                        final Call<NotesSettings> putSettingsCall = repository.putServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), new NotesSettings(editText.getText().toString(), null), localAccount.getPreferredApiVersion());
                        putSettingsCall.enqueue(new Callback<NotesSettings>() {
                            @Override
                            public void onResponse(@NonNull Call<NotesSettings> call, @NonNull Response<NotesSettings> response) {
                                final NotesSettings body = response.body();
                                if (response.isSuccessful() && body != null) {
                                    Toast.makeText(ManageAccountsActivity.this, "New notes path: " + body.getNotesPath(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ManageAccountsActivity.this, "HTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
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
                }).start())
                .show();
        try {
            repository.getServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), localAccount.getPreferredApiVersion())
                    .enqueue(new Callback<NotesSettings>() {
                        @Override
                        public void onResponse(@NonNull Call<NotesSettings> call, @NonNull Response<NotesSettings> response) {
                            runOnUiThread(() -> {
                                final NotesSettings body = response.body();
                                if (response.isSuccessful() && body != null) {
                                    editText.setText(body.getNotesPath());
                                    editText.setEnabled(true);
                                } else {
                                    ExceptionDialogFragment.newInstance(new NetworkErrorException("HTTP status code: " + response.code())).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
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

    private void onChangeFileSuffix(@NonNull Account localAccount) {
        final NotesRepository repository = NotesRepository.getInstance(getApplicationContext());
        final Spinner spinner = new Spinner(this);
        spinner.setEnabled(false);
        final View wrapper = createDialogViewWrapper(spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.settings_file_suffixes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final AlertDialog dialog = new BrandedAlertDialogBuilder(this)
                .setTitle(R.string.settings_file_suffix)
                .setMessage("File extension for new notes in your Nextcloud")
                .setView(wrapper)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton("Save", (v, d) -> new Thread(() -> {
                    try {
                        final Call<NotesSettings> putSettingsCall = repository.putServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), new NotesSettings(null, spinner.getSelectedItem().toString()), localAccount.getPreferredApiVersion());
                        putSettingsCall.enqueue(new Callback<NotesSettings>() {
                            @Override
                            public void onResponse(@NonNull Call<NotesSettings> call, @NonNull Response<NotesSettings> response) {
                                final NotesSettings body = response.body();
                                if (response.isSuccessful() && body != null) {
                                    Toast.makeText(ManageAccountsActivity.this, "New file suffix: " + body.getNotesPath(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ManageAccountsActivity.this, "HTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<NotesSettings> call, @NonNull Throwable t) {
                                runOnUiThread(() -> ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()));
                            }
                        });
                    } catch (NextcloudFilesAppAccountNotFoundException e) {
                        runOnUiThread(() -> ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()));
                    }
                }).start())
                .show();
        try {
            repository.getServerSettings(AccountImporter.getSingleSignOnAccount(this, localAccount.getAccountName()), localAccount.getPreferredApiVersion())
                    .enqueue(new Callback<NotesSettings>() {
                        @Override
                        public void onResponse(@NonNull Call<NotesSettings> call, @NonNull Response<NotesSettings> response) {
                            final NotesSettings body = response.body();
                            runOnUiThread(() -> {
                                if (response.isSuccessful() && body != null) {
                                    for (int i = 0; i < adapter.getCount(); i++) {
                                        if (adapter.getItem(i).equals(body.getFileSuffix())) {
                                            spinner.setSelection(i);
                                            break;
                                        }
                                    }
                                    spinner.setEnabled(true);
                                } else {
                                    ExceptionDialogFragment.newInstance(new Exception("HTTP status code: " + response.code())).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
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

    @NonNull
    private View createDialogViewWrapper(@NonNull View view) {
        final FrameLayout wrapper = new FrameLayout(this);
        final int paddingVertical = getResources().getDimensionPixelSize(R.dimen.spacer_1x);
        final int paddingHorizontal = SDK_INT >= LOLLIPOP_MR1
                ? getDimensionFromAttribute(android.R.attr.dialogPreferredPadding)
                : getResources().getDimensionPixelSize(R.dimen.spacer_2x);
        wrapper.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        wrapper.addView(view);
        return wrapper;
    }

    @Px
    private int getDimensionFromAttribute(@SuppressWarnings("SameParameterValue") @AttrRes int attr) {
        final TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(attr, typedValue, true))
            return TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
        else {
            return 0;
        }
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.appBar, binding.toolbar);
    }
}
