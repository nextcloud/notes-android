package it.niedermann.owncloud.notes.edit.title;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.DialogEditTitleBinding;
import it.niedermann.owncloud.notes.shared.util.KeyboardUtils;

public class EditTitleDialogFragment extends BrandedDialogFragment {

    private static final String TAG = EditTitleDialogFragment.class.getSimpleName();
    static final String PARAM_OLD_TITLE = "old_title";
    private DialogEditTitleBinding binding;

    private String oldTitle;
    private EditTitleListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final var args = getArguments();
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
        final var dialogView = View.inflate(getContext(), R.layout.dialog_edit_title, null);
        binding = DialogEditTitleBinding.bind(dialogView);

        if (savedInstanceState == null) {
            binding.title.setText(oldTitle);
        }

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.change_note_title)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(R.string.action_edit_save, (dialog, which) -> {
                    hideKeyboard(dialogView.getWindowToken());
                    listener.onTitleEdited(binding.title.getText().toString());
                })
                .setNegativeButton(R.string.simple_cancel, (dialog, which) -> hideKeyboard(dialogView.getWindowToken()))
                .create();
    }

    private void hideKeyboard(IBinder windowToken) {
        final InputMethodManager inputManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        KeyboardUtils.showKeyboardForEditText(binding.title);
    }

    public static DialogFragment newInstance(String title) {
        final var fragment = new EditTitleDialogFragment();
        final var args = new Bundle();
        args.putString(PARAM_OLD_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, requireContext());
        util.material.colorTextInputLayout(binding.inputWrapper);
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
