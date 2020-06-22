package it.niedermann.owncloud.notes.edit.category;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedAlertDialogBuilder;
import it.niedermann.owncloud.notes.branding.BrandedDialogFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.DialogChangeCategoryBinding;
import it.niedermann.owncloud.notes.main.NavigationAdapter;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

/**
 * This {@link DialogFragment} allows for the selection of a category.
 * It targetFragment is set it must implement the interface {@link CategoryDialogListener}.
 * The calling Activity must implement the interface {@link CategoryDialogListener}.
 */
public class CategoryDialogFragment extends BrandedDialogFragment {

    private static final String TAG = CategoryDialogFragment.class.getSimpleName();
    private static final String STATE_CATEGORY = "category";
    private DialogChangeCategoryBinding binding;

    private NotesDatabase db;
    private CategoryDialogListener listener;

    private EditText editCategory;

    @Override
    public void applyBrand(int mainColor, int textColor) {
        BrandingUtil.applyBrandToEditText(mainColor, textColor, binding.search);
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

    private CategoryAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getArguments() != null && requireArguments().containsKey(PARAM_ACCOUNT_ID)) {
            accountId = requireArguments().getLong(PARAM_ACCOUNT_ID);
        } else {
            throw new IllegalArgumentException("Provide at least \"" + PARAM_ACCOUNT_ID + "\"");
        }
        Fragment target = getTargetFragment();
        if (target instanceof CategoryDialogListener) {
            listener = (CategoryDialogListener) target;
        } else if (getActivity() instanceof CategoryDialogListener) {
            listener = (CategoryDialogListener) getActivity();
        } else {
            throw new IllegalArgumentException("Calling activity or target fragment must implement " + CategoryDialogListener.class.getSimpleName());
        }
        db = NotesDatabase.getInstance(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = View.inflate(getContext(), R.layout.dialog_change_category, null);
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
        new LoadCategoriesTask().execute("");
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
                new LoadCategoriesTask().execute(editCategory.getText().toString());
            }
        });

        return new BrandedAlertDialogBuilder(getActivity())
                .setTitle(R.string.change_category_title)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, (dialog, which) -> listener.onCategoryChosen(editCategory.getText().toString()))
                .setNegativeButton(R.string.simple_cancel, null)
                .create();
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
            editCategory.requestFocus();
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            } else {
                Log.w(TAG, "can not set SOFT_INPUT_STATE_ALWAYAS_VISIBLE because getWindow() == null");
            }
        }
    }


    private class LoadCategoriesTask extends AsyncTask<String, Void, List<NavigationAdapter.CategoryNavigationItem>> {
        String currentSearchString;

        @Override
        protected List<NavigationAdapter.CategoryNavigationItem> doInBackground(String... searchText) {
            currentSearchString = searchText[0];
            return db.searchCategories(accountId, currentSearchString);
        }

        @Override
        protected void onPostExecute(List<NavigationAdapter.CategoryNavigationItem> categories) {
            adapter.setCategoryList(categories, currentSearchString);
        }
    }
}
