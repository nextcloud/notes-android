package it.niedermann.owncloud.notes.manageaccounts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedDeleteAlertDialogBuilder;
import it.niedermann.owncloud.notes.databinding.ActivityManageAccountsBinding;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

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

        adapter = new ManageAccountAdapter(this::selectAccount, this::deleteAccount);
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
                    if (unsynchronizedChangesCount != null && unsynchronizedChangesCount > 0) {
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

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.appBar, binding.toolbar);
    }
}
