package it.niedermann.owncloud.notes.android.fragment;

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
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.AccountChooserAdapter.AccountChooserListener;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class AccountChooserDialogFragment extends AppCompatDialogFragment implements AccountChooserListener {
    private AccountChooserListener accountChooserListener;
    @BindView(R.id.accounts_list)
    RecyclerView accountRecyclerView;

    /**
     * Use newInstance()-Method
     */
    public AccountChooserDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AccountChooserListener) {
            this.accountChooserListener = (AccountChooserListener) context;
        } else {
            throw new ClassCastException("Caller must implement " + AccountChooserListener.class.getCanonicalName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.dialog_choose_account, null);
        ButterKnife.bind(this, view);

        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getActivity());
        List<LocalAccount> accountsList = db.getAccounts();

        RecyclerView.Adapter adapter = new AccountChooserAdapter(accountsList, this, getActivity());
        accountRecyclerView.setAdapter(adapter);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.simple_move)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static AccountChooserDialogFragment newInstance() {
        return new AccountChooserDialogFragment();
    }

    @Override
    public void onAccountChosen(LocalAccount account) {
        accountChooserListener.onAccountChosen(account);
        dismiss();
    }
}
