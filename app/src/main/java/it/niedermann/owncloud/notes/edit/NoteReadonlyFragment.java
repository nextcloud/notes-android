package it.niedermann.owncloud.notes.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;

public class NoteReadonlyFragment extends NotePreviewFragment {

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_edit).setVisible(false);
        menu.findItem(R.id.menu_preview).setVisible(false);
        menu.findItem(R.id.menu_cancel).setVisible(false);
        menu.findItem(R.id.menu_delete).setVisible(false);
        menu.findItem(R.id.menu_move).setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding.singleNoteContent.setEnabled(false);
        binding.swiperefreshlayout.setEnabled(false);
        return binding.getRoot();
    }

    @Override
    protected void registerInternalNoteLinkHandler() {
        // Do nothing
    }

    @Override
    public void onCloseNote() {
        // Do nothing
    }

    @Override
    protected void saveNote(@Nullable ISyncCallback callback) {
        // Do nothing
    }

    public static BaseNoteFragment newInstance(String content) {
        final BaseNoteFragment fragment = new NoteReadonlyFragment();
        final Bundle args = new Bundle();
        args.putString(PARAM_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }
}
