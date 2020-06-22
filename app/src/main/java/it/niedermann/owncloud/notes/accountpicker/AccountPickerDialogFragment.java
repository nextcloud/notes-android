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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedAlertDialogBuilder;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.databinding.DialogChooseAccountBinding;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.shared.account.AccountChooserAdapter;
import it.niedermann.owncloud.notes.shared.account.AccountChooserViewHolder;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;

/**
 * A {@link DialogFragment} which provides an {@link LocalAccount} chooser that hides the given {@link LocalAccount}.
 * This can be useful when one wants to pick e. g. a target for move a note from one {@link LocalAccount} to another..
 */
public class AccountPickerDialogFragment extends BrandedDialogFragment {

    private AccountPickerListener accountPickerListener;
    private static final String PARAM_ACCOUNT_ID_TO_EXCLUDE = "account_id_to_exclude";
    private long accountIdToExclude;

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
        accountIdToExclude = requireArguments().getLong(PARAM_ACCOUNT_ID_TO_EXCLUDE, -1L);
        if (accountIdToExclude < 0) {
            throw new IllegalArgumentException(PARAM_ACCOUNT_ID_TO_EXCLUDE + " must be greater 0");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.dialog_choose_account, null);
        DialogChooseAccountBinding binding = DialogChooseAccountBinding.bind(view);

        NotesDatabase db = NotesDatabase.getInstance(getActivity());
        List<LocalAccount> accountsList = db.getAccounts();

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).getId() == accountIdToExclude) {
                accountsList.remove(i);
                break;
            }
        }

        RecyclerView.Adapter<AccountChooserViewHolder> adapter = new AccountChooserAdapter(accountsList, (account -> {
            accountPickerListener.onAccountPicked(account);
            dismiss();
        }));
        binding.accountsList.setAdapter(adapter);

        return new BrandedAlertDialogBuilder(requireActivity())
                .setView(binding.getRoot())
                .setTitle(R.string.simple_move)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(requireDialog().getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static DialogFragment newInstance(long accountIdToExclude) {
        final DialogFragment fragment = new AccountPickerDialogFragment();
        final Bundle args = new Bundle();
        args.putLong(PARAM_ACCOUNT_ID_TO_EXCLUDE, accountIdToExclude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        // Nothing to do...
    }
}
