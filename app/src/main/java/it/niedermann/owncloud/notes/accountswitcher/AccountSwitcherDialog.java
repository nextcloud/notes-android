package it.niedermann.owncloud.notes.accountswitcher;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.databinding.DialogAccountSwitcherBinding;
import it.niedermann.owncloud.notes.manageaccounts.ManageAccountsActivity;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;

import static it.niedermann.owncloud.notes.branding.BrandingUtil.applyBrandToLayerDrawable;

/**
 * Displays all available {@link Account} entries and provides basic operations for them, like adding or switching
 */
public class AccountSwitcherDialog extends BrandedDialogFragment {

    private static final String KEY_CURRENT_ACCOUNT_ID = "current_account_id";

    private NotesRepository repo;
    private DialogAccountSwitcherBinding binding;
    private AccountSwitcherListener accountSwitcherListener;
    private long currentAccountId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AccountSwitcherListener) {
            this.accountSwitcherListener = (AccountSwitcherListener) context;
        } else {
            throw new ClassCastException("Caller must implement " + AccountSwitcherListener.class.getSimpleName());
        }

        final Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_CURRENT_ACCOUNT_ID)) {
            throw new IllegalArgumentException("Please provide at least " + KEY_CURRENT_ACCOUNT_ID);
        } else {
            this.currentAccountId = args.getLong(KEY_CURRENT_ACCOUNT_ID);
        }

        repo = NotesRepository.getInstance(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAccountSwitcherBinding.inflate(requireActivity().getLayoutInflater());

        final LiveData<Account> account$ = repo.getAccountById$(currentAccountId);
        account$.observe(requireActivity(), (currentLocalAccount) -> {
            account$.removeObservers(requireActivity());

            binding.accountName.setText(currentLocalAccount.getDisplayName());
            binding.accountHost.setText(Uri.parse(currentLocalAccount.getUrl()).getHost());
            Glide.with(requireContext())
                    .load(currentLocalAccount.getUrl() + "/index.php/avatar/" + Uri.encode(currentLocalAccount.getUserName()) + "/64")
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.currentAccountItemAvatar);
            binding.accountLayout.setOnClickListener((v) -> dismiss());

            final AccountSwitcherAdapter adapter = new AccountSwitcherAdapter((localAccount -> {
                accountSwitcherListener.onAccountChosen(localAccount);
                dismiss();
            }));
            binding.accountsList.setAdapter(adapter);
            final LiveData<List<Account>> localAccounts$ = repo.getAccounts$();
            localAccounts$.observe(requireActivity(), (localAccounts) -> {
                localAccounts$.removeObservers(requireActivity());
                for (Account localAccount : localAccounts) {
                    if (localAccount.getId() == currentLocalAccount.getId()) {
                        localAccounts.remove(localAccount);
                        break;
                    }
                }
                adapter.setLocalAccounts(localAccounts);
            });
        });

        binding.addAccount.setOnClickListener((v) -> {
            accountSwitcherListener.addAccount();
            dismiss();
        });

        binding.manageAccounts.setOnClickListener((v) -> {
            requireActivity().startActivity(new Intent(requireContext(), ManageAccountsActivity.class));
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    public static DialogFragment newInstance(long currentAccountId) {
        DialogFragment dialog = new AccountSwitcherDialog();

        Bundle args = new Bundle();
        args.putLong(KEY_CURRENT_ACCOUNT_ID, currentAccountId);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToLayerDrawable((LayerDrawable) binding.check.getDrawable(), R.id.area, mainColor);
    }
}
