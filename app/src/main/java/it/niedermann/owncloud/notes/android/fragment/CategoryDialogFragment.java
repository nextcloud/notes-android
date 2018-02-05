package it.niedermann.owncloud.notes.android.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.AlwaysAutoCompleteTextView;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

/**
 * This {@link DialogFragment} allows for the selection of a category.
 * It targetFragment is set it must implement the interface {@link CategoryDialogListener}.
 * The calling Activity must implement the interface {@link CategoryDialogListener}.
 */
public class CategoryDialogFragment extends DialogFragment {

    /**
     * Interface that must be implemented by the calling Activity.
     */
    public interface CategoryDialogListener {
        /**
         * This method is called after the user has chosen a category.
         * @param category Name of the category which was chosen by the user.
         */
        void onCategoryChosen(String category);
    }

    public static final String PARAM_CATEGORY = "category";

    private AlwaysAutoCompleteTextView textCategory;
    private FolderArrayAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_category, null);
        textCategory = dialogView.findViewById(R.id.editCategory);
        if(savedInstanceState==null) {
            textCategory.setText(getArguments().getString(PARAM_CATEGORY));
        }
        adapter = new FolderArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
        textCategory.setAdapter(adapter);
        new LoadCategoriesTask().execute();
        return new AlertDialog.Builder(getActivity(), R.style.ocAlertDialog)
                .setTitle(R.string.change_category_title)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CategoryDialogListener listener;
                        Fragment target = getTargetFragment();
                        if(target != null && target instanceof CategoryDialogListener) {
                            listener = (CategoryDialogListener) target;
                        } else {
                            listener = (CategoryDialogListener) getActivity();
                        }
                        listener.onCategoryChosen(textCategory.getText().toString());
                    }
                })
                .setNegativeButton(R.string.action_edit_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    private class LoadCategoriesTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(getActivity());
            List<NavigationAdapter.NavigationItem> items = db.getCategories();
            List<String> categories = new ArrayList<>();
            for(NavigationAdapter.NavigationItem item : items) {
                if(!item.label.isEmpty()) {
                    categories.add(item.label);
                }
            }
            return categories;
        }

        @Override
        protected void onPostExecute(List<String> categories) {
            adapter.setData(categories);
            if(textCategory.getText().length()==0) {
                textCategory.showFullDropDown();
            } else {
                textCategory.dismissDropDown();
            }
        }
    }


    private static class FolderArrayAdapter extends ArrayAdapter<String> {

        private List<String> originalData = new ArrayList<>();
        private Filter filter;

        private FolderArrayAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        public void setData(List<String> data) {
            originalData = data;
            clear();
            addAll(data);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            if(filter==null) {
                filter = new FolderFilter();
            }
            return filter;
        }

        /* This implementation is based on ArrayAdapter.ArrayFilter */
        private class FolderFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                final FilterResults results = new FilterResults();

                if (prefix == null || prefix.length() == 0) {
                    final ArrayList<String> list = new ArrayList<>(originalData);
                    results.values = list;
                    results.count = list.size();
                } else {
                    final String prefixString = prefix.toString().toLowerCase();
                    final int count = originalData.size();
                    final ArrayList<String> newValues = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        final String value = originalData.get(i);
                        final String valueText = value.toLowerCase();

                        // First match against the whole, non-splitted value
                        if (valueText.startsWith(prefixString)) {
                            newValues.add(value);
                        } else {
                            final String[] words = valueText.split("/");
                            for (String word : words) {
                                if (word.startsWith(prefixString)) {
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                addAll((List<String>) results.values);
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}
