package it.niedermann.owncloud.notes.android.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import it.niedermann.owncloud.notes.R;

/**
 * This {@link DialogFragment} allows for the selection of a category.
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

    private EditText textCategory;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_category, null);
        textCategory = (EditText) dialogView.findViewById(R.id.editCategory);
        if(savedInstanceState==null) {
            textCategory.setText(getArguments().getString(PARAM_CATEGORY));
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.change_category_title)
                .setView(dialogView)
                .setMessage(R.string.change_category_message)
                .setCancelable(true)
                .setPositiveButton(R.string.action_edit_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CategoryDialogListener listener = (CategoryDialogListener) getActivity();
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
}
