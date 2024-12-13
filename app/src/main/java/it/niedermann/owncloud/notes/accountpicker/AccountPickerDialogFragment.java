/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.databinding.DialogChooseAccountBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.account.AccountChooserAdapter;

/**
 * A {@link DialogFragment} which provides an {@link Account} chooser that hides the current {@link Account}.
 * This can be useful when one wants to pick e. g. a target for move a {@link Note} from one {@link Account} to another..
 */
public class AccountPickerDialogFragment extends BrandedDialogFragment {

    private static final String PARAM_TARGET_ACCOUNTS = "targetAccounts";
    private static final String PARAM_CURRENT_ACCOUNT_ID = "currentAccountId";

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
        final var args = requireArguments();
        if (!args.containsKey(PARAM_TARGET_ACCOUNTS)) {
            throw new IllegalArgumentException(PARAM_TARGET_ACCOUNTS + " is required.");
        }
        final var accounts = (Collection<?>) args.getSerializable(PARAM_TARGET_ACCOUNTS);
        if (accounts == null) {
            throw new IllegalArgumentException(PARAM_TARGET_ACCOUNTS + " is required.");
        }
        final long currentAccountId = requireArguments().getLong(PARAM_CURRENT_ACCOUNT_ID, -1L);
        targetAccounts = accounts
                .stream()
                .map(a -> (Account) a)
                .filter(a -> a.getId() != currentAccountId)
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.simple_move)
                .setNegativeButton(android.R.string.cancel, null);

        if (!targetAccounts.isEmpty()) {
            final var binding = DialogChooseAccountBinding.inflate(LayoutInflater.from(requireContext()));
            final var adapter = new AccountChooserAdapter(targetAccounts, (account -> {
                accountPickerListener.onAccountPicked(account);
                dismiss();
            }));
            binding.accountsList.setAdapter(adapter);
            dialogBuilder.setView(binding.getRoot());
        } else {
            dialogBuilder.setMessage(getString(R.string.no_other_accounts));
        }

        NotesApplication.brandingUtil().dialog.colorMaterialAlertDialogBackground(requireContext(), dialogBuilder);

        return dialogBuilder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(requireDialog().getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static DialogFragment newInstance(@NonNull ArrayList<Account> targetAccounts, long currentAccountId) {
        final var fragment = new AccountPickerDialogFragment();
        final var args = new Bundle();
        args.putSerializable(PARAM_TARGET_ACCOUNTS, targetAccounts);
        args.putLong(PARAM_CURRENT_ACCOUNT_ID, currentAccountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void applyBrand(int color) {
        // Nothing to do...
    }
}
