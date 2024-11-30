/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit.category;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.DialogChangeCategoryBinding;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.shared.util.KeyboardUtils;

/**
 * This {@link DialogFragment} allows for the selection of a category.
 * It targetFragment is set it must implement the interface {@link CategoryDialogListener}.
 * The calling Activity must implement the interface {@link CategoryDialogListener}.
 */
public class CategoryDialogFragment extends BrandedDialogFragment {

    private static final String STATE_CATEGORY = "category";

    private CategoryViewModel viewModel;
    private DialogChangeCategoryBinding binding;

    private CategoryDialogListener listener;

    private CategoryAdapter adapter;

    private EditText editCategory;

    private LiveData<List<NavigationItem.CategoryNavigationItem>> categoryLiveData;

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, requireContext());
        util.material.colorTextInputLayout(binding.inputWrapper);
    }

    /**
     * Interface that must be implemented by the calling Activity.
     */
    public interface CategoryDialogListener {
        /**
         * This method is called after the user has chosen a category.
         *
         * @param category Name of the category which was chosen by the user.
         */
        void onCategoryChosen(String category);
    }

    public static final String PARAM_ACCOUNT_ID = "account_id";
    public static final String PARAM_CATEGORY = "category";

    private long accountId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getArguments() != null && requireArguments().containsKey(PARAM_ACCOUNT_ID)) {
            accountId = requireArguments().getLong(PARAM_ACCOUNT_ID);
        } else {
            throw new IllegalArgumentException("Provide at least \"" + PARAM_ACCOUNT_ID + "\"");
        }
        final var target = getTargetFragment();
        if (target instanceof CategoryDialogListener) {
            listener = (CategoryDialogListener) target;
        } else if (getActivity() instanceof CategoryDialogListener) {
            listener = (CategoryDialogListener) getActivity();
        } else {
            throw new IllegalArgumentException("Calling activity or target fragment must implement " + CategoryDialogListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final var dialogView = View.inflate(getContext(), R.layout.dialog_change_category, null);
        binding = DialogChangeCategoryBinding.bind(dialogView);
        this.editCategory = binding.search;

        if (savedInstanceState == null) {
            if (requireArguments().containsKey(PARAM_CATEGORY)) {
                editCategory.setText(requireArguments().getString(PARAM_CATEGORY));
            }
        } else if (savedInstanceState.containsKey(STATE_CATEGORY)) {
            editCategory.setText(savedInstanceState.getString(STATE_CATEGORY));
        }

        adapter = new CategoryAdapter(requireContext(), new CategoryAdapter.CategoryListener() {
            @Override
            public void onCategoryChosen(String category) {
                listener.onCategoryChosen(category);
                dismiss();
            }

            @Override
            public void onCategoryAdded() {
                listener.onCategoryChosen(editCategory.getText().toString());
                dismiss();
            }

            @Override
            public void onCategoryCleared() {
                listener.onCategoryChosen("");
                dismiss();
            }
        });

        binding.recyclerView.setAdapter(adapter);

        categoryLiveData = viewModel.getCategories(accountId);
        categoryLiveData.observe(requireActivity(), categories -> adapter.setCategoryList(categories, binding.search.getText().toString()));

        editCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do here...
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do here...
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.postSearchTerm(editCategory.getText().toString());
            }
        });

        final MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.change_category_title)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, (dialog, which) -> listener.onCategoryChosen(editCategory.getText().toString()))
                .setNegativeButton(R.string.simple_cancel, null);

        NotesApplication.brandingUtil().dialog.colorMaterialAlertDialogBackground(requireContext(), alertDialogBuilder);

        return alertDialogBuilder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CATEGORY, editCategory.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (editCategory.getText() == null || editCategory.getText().length() == 0) {
            KeyboardUtils.showKeyboardForEditText(editCategory);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (categoryLiveData != null) {
            categoryLiveData.removeObservers(requireActivity());
        }
    }

    public static DialogFragment newInstance(long accountId, String category) {
        final var categoryFragment = new CategoryDialogFragment();
        final var args = new Bundle();
        args.putString(CategoryDialogFragment.PARAM_CATEGORY, category);
        args.putLong(CategoryDialogFragment.PARAM_ACCOUNT_ID, accountId);
        categoryFragment.setArguments(args);
        return categoryFragment;
    }
}
