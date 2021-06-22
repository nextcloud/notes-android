package it.niedermann.owncloud.notes.edit.outline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.DialogEditTitleBinding;
import it.niedermann.owncloud.notes.databinding.DialogOutlineBinding;
import it.niedermann.owncloud.notes.edit.SearchableBaseNoteFragment;
import it.niedermann.owncloud.notes.edit.category.CategoryAdapter;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.edit.title.EditTitleDialogFragment;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OutlineDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutlineDialogFragment extends DialogFragment {


    private DialogOutlineBinding binding;

    private List<OutlineItem> outlinedata;
    private OutlineAdapter adapter;

    private OutlineAdapter.OutlineListener listener;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment target = getTargetFragment();
        if (target instanceof OutlineAdapter.OutlineListener) {
            listener = (OutlineAdapter.OutlineListener) target;
        } else if (getActivity() instanceof OutlineAdapter.OutlineListener) {
            listener = (OutlineAdapter.OutlineListener) getActivity();
        } else {
            throw new IllegalArgumentException("Calling activity or target fragment must implement " + CategoryDialogFragment.CategoryDialogListener.class.getSimpleName());
        }
    }



    public OutlineDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OutlineDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OutlineDialogFragment newInstance(String param1, String param2) {
        OutlineDialogFragment fragment = new OutlineDialogFragment();

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = View.inflate(getContext(), R.layout.dialog_outline, null);
        binding = DialogOutlineBinding.bind(dialogView);


        adapter = new OutlineAdapter(requireContext(), oi -> {
            listener.onOutlineChosen(oi);
            dismiss();
        });


        binding.recyclerView2.setAdapter(adapter);

        Fragment f = getTargetFragment();
        SearchableBaseNoteFragment sf = (SearchableBaseNoteFragment)f;


        adapter.setList(sf.outlinedata);


        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_outline)
                .setView(dialogView)
                .setCancelable(true)
                .setNegativeButton(R.string.simple_cancel, null)
                .create();
    }
}