/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.title;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.DialogEditTitleBinding;
import it.niedermann.owncloud.notes.shared.util.KeyboardUtils;

public class EditTitleDialogFragment extends BrandedDialogFragment {

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

        final MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.change_note_title)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, (dialog, which) -> listener.onTitleEdited(binding.title.getText().toString()))
                .setNegativeButton(R.string.simple_cancel, null);

        NotesApplication.brandingUtil().dialog.colorMaterialAlertDialogBackground(requireContext(), alertDialogBuilder);

        return alertDialogBuilder.create();
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
