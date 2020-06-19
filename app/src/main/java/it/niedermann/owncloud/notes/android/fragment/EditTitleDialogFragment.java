package it.niedermann.owncloud.notes.android.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogEditTitleBinding;

public class EditTitleDialogFragment extends DialogFragment {

    static final String PARAM_OLD_TITLE = "old_title";

    private String oldTitle;
    private EditTitleListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Provide at least " + PARAM_OLD_TITLE);
        }
        oldTitle = args.getString(PARAM_OLD_TITLE);

        if (getTargetFragment() instanceof EditTitleListener) {
            listener = (EditTitleListener) getTargetFragment();
        } else if (getActivity() instanceof EditTitleListener) {
            listener = (EditTitleListener) getActivity();
        } else {
            throw new IllegalArgumentException("Calling activity or target fragment must implement " + EditTitleListener.class.getSimpleName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = View.inflate(getContext(), R.layout.dialog_edit_title, null);
        DialogEditTitleBinding binding = DialogEditTitleBinding.bind(dialogView);

        if (savedInstanceState == null) {
            if (requireArguments().containsKey(PARAM_OLD_TITLE)) {
                binding.title.setText(requireArguments().getString(PARAM_OLD_TITLE));
            }
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.change_note_title)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, (dialog, which) -> listener.onTitleEdited(binding.title.getText().toString()))
                .setNegativeButton(R.string.simple_cancel, null)
                .create();
    }

    public static DialogFragment newInstance(String title) {
        final DialogFragment fragment = new EditTitleDialogFragment();
        final Bundle args = new Bundle();
        args.putString(PARAM_OLD_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Interface that must be implemented by the calling Activity.
     */
    public interface EditTitleListener {
        /**
         * This method is called after the user has changed the title of a note manually.
         *
         * @param newTitle the new title that a user submitted
         */
        void onTitleEdited(String newTitle);
    }
}
