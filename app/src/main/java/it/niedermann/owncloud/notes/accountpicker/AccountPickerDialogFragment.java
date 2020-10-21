package it.niedermann.owncloud.notes.accountpicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedAlertDialogBuilder;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.databinding.DialogChooseAccountBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.account.AccountChooserAdapter;
import it.niedermann.owncloud.notes.shared.account.AccountChooserViewHolder;

/**
 * A {@link DialogFragment} which provides an {@link Account} chooser that hides the given {@link Account}.
 * This can be useful when one wants to pick e. g. a target for move a note from one {@link Account} to another..
 */
public class AccountPickerDialogFragment extends BrandedDialogFragment {

    private static final String PARAM_TARGET_ACCOUNTS = "targetAccounts";
    private static final String PARAM_ACCOUNT_ID_TO_EXCLUDE = "accountIdToExclude";

    private AccountPickerListener accountPickerListener;

    private List<Account> targetAccounts;

    /**
     * Use newInstance()-Method
     */
    public AccountPickerDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AccountPickerListener) {
            this.accountPickerListener = (AccountPickerListener) context;
        } else {
            throw new ClassCastException("Caller must implement " + AccountPickerListener.class.getSimpleName());
        }
        final Bundle args = requireArguments();
        final Collection<?> accounts;
        if (!args.containsKey(PARAM_TARGET_ACCOUNTS)) {
            throw new IllegalArgumentException(PARAM_TARGET_ACCOUNTS + " is required.");
        }
        accounts = (Collection<?>) args.getSerializable(PARAM_TARGET_ACCOUNTS);
        if (accounts == null) {
            throw new IllegalArgumentException(PARAM_TARGET_ACCOUNTS + " is required.");
        }
        long accountIdToExclude = requireArguments().getLong(PARAM_ACCOUNT_ID_TO_EXCLUDE, -1L);
        if (accountIdToExclude < 0) {
            throw new IllegalArgumentException(PARAM_ACCOUNT_ID_TO_EXCLUDE + " must be greater 0");
        }
        targetAccounts = accounts
                .stream()
                .map(a -> (Account) a)
                .filter(a -> a.getId() != accountIdToExclude)
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new BrandedAlertDialogBuilder(requireActivity())
                .setTitle(R.string.simple_move)
                .setNegativeButton(android.R.string.cancel, null);

        if (targetAccounts.size() > 0) {
            final DialogChooseAccountBinding binding = DialogChooseAccountBinding.inflate(LayoutInflater.from(requireContext()));
            RecyclerView.Adapter<AccountChooserViewHolder> adapter = new AccountChooserAdapter(targetAccounts, (account -> {
                accountPickerListener.onAccountPicked(account);
                dismiss();
            }));
            binding.accountsList.setAdapter(adapter);
            dialogBuilder.setView(binding.getRoot());
        } else {
            dialogBuilder.setMessage(getString(R.string.no_other_accounts));
        }

        return dialogBuilder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(requireDialog().getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static DialogFragment newInstance(ArrayList<Account> targetAccounts, long accountIdToExclude) {
        final DialogFragment fragment = new AccountPickerDialogFragment();
        final Bundle args = new Bundle();
        args.putSerializable(PARAM_TARGET_ACCOUNTS, targetAccounts);
        args.putLong(PARAM_ACCOUNT_ID_TO_EXCLUDE, accountIdToExclude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        // Nothing to do...
    }
}
