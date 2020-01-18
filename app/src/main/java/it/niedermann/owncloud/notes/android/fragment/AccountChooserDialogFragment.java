package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.AccountChooserAdapter.AccountChooserListener;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class AccountChooserDialogFragment extends DialogFragment implements AccountChooserListener {
    private AccountChooserListener accountChooserListener;
    @BindView(R.id.accounts_list)
    RecyclerView accountRecyclerView;

    /**
     * Use newInstance()-Method
     */
    public AccountChooserDialogFragment() {
    }

    /*
     * This is used instead of onAttach(Context context) for supporting < API 23
     * TODO Switch to androidx for fragments
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AccountChooserListener) {
            this.accountChooserListener = (AccountChooserListener) activity;
        } else {
            throw new ClassCastException("Caller must implement " + AccountChooserListener.class.getCanonicalName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.choose_account, null);
        ButterKnife.bind(this, view);

        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getActivity());
        List<LocalAccount> accountsList = db.getAccounts();

        RecyclerView.Adapter adapter = new AccountChooserAdapter(accountsList, this, getActivity());
        accountRecyclerView.setAdapter(adapter);

        return new AlertDialog.Builder(getActivity(), R.style.ncAlertDialog)
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
