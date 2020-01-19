package it.niedermann.owncloud.notes.android.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.CategoryAdapter;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

/**
 * This {@link DialogFragment} allows for the selection of a category.
 * It targetFragment is set it must implement the interface {@link CategoryDialogListener}.
 * The calling Activity must implement the interface {@link CategoryDialogListener}.
 */
public class CategoryDialogFragment extends DialogFragment {

    private static final String TAG = CategoryDialogFragment.class.getSimpleName();

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

    static final String PARAM_ACCOUNT_ID = "account_id";
    static final String PARAM_CATEGORY = "category";

    private long accountId;

    @BindView(R.id.search)
    EditText editCategory;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private CategoryAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(getArguments() != null && getArguments().containsKey(PARAM_ACCOUNT_ID)) {
            accountId = getArguments().getLong(PARAM_ACCOUNT_ID);
        } else {
            throw new IllegalArgumentException("Provide at least \"" + PARAM_ACCOUNT_ID + "\"");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.dialog_change_category, null);
        ButterKnife.bind(this, dialogView);
        if (savedInstanceState == null) {
            if(getArguments() != null && getArguments().containsKey(PARAM_CATEGORY)) {
                editCategory.setText(getArguments().getString(PARAM_CATEGORY));
            }
        }
        adapter = new CategoryAdapter();
        recyclerView.setAdapter(adapter);
        new LoadCategoriesTask().execute();
        return new AlertDialog.Builder(getActivity(), R.style.ncAlertDialog)
                .setTitle(R.string.change_category_title)
                .setView(dialogView)
                .setCancelable(true)

//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            CategoryDialogListener listener;
//            Fragment target = getTargetFragment();
//            if (target instanceof CategoryDialogListener) {
//                listener = (CategoryDialogListener) target;
//            } else {
//                listener = (CategoryDialogListener) getActivity();
//            }
////                        listener.onCategoryChosen(textCategory.getText().toString());
//        }
                .setNegativeButton(R.string.simple_cancel, (dialog, which) -> {
                    // do nothing
                })
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            Log.w(TAG, "can not set SOFT_INPUT_STATE_ALWAYAS_VISIBLE because getWindow() == null");
        }
    }


    private class LoadCategoriesTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getActivity());
            List<NavigationAdapter.NavigationItem> items = db.getCategories(accountId);
            List<String> categories = new ArrayList<>();
            for (NavigationAdapter.NavigationItem item : items) {
                if (!item.label.isEmpty()) {
                    categories.add(item.label);
                }
            }
            return categories;
        }

        @Override
        protected void onPostExecute(List<String> categories) {
            adapter.setCategoryList(categories);
//TODO show creation entry
//            if (textCategory.getText().length() == 0) {
//                textCategory.showFullDropDown();
//            } else {
//                textCategory.dismissDropDown();
//            }
        }
    }
}
