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
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.AccountChooserAdapter.MoveAccountListener;
import it.niedermann.owncloud.notes.branding.BrandedAlertDialogBuilder;
import it.niedermann.owncloud.notes.databinding.DialogChooseAccountBinding;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

import static it.niedermann.owncloud.notes.android.fragment.AccountChooserAdapter.AccountChooserViewHolder;

public class MoveAccountDialogFragment extends AppCompatDialogFragment implements MoveAccountListener {
    private MoveAccountListener moveAccountListener;

    /**
     * Use newInstance()-Method
     */
    public MoveAccountDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MoveAccountListener) {
            this.moveAccountListener = (MoveAccountListener) context;
        } else {
            throw new ClassCastException("Caller must implement " + MoveAccountListener.class.getSimpleName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.dialog_choose_account, null);
        DialogChooseAccountBinding binding = DialogChooseAccountBinding.bind(view);

        NotesDatabase db = NotesDatabase.getInstance(getActivity());
        List<LocalAccount> accountsList = db.getAccounts();

        RecyclerView.Adapter<AccountChooserViewHolder> adapter = new AccountChooserAdapter(accountsList, this);
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

    public static MoveAccountDialogFragment newInstance() {
        return new MoveAccountDialogFragment();
    }

    @Override
    public void moveToAccount(LocalAccount account) {
        moveAccountListener.moveToAccount(account);
        dismiss();
    }
}
