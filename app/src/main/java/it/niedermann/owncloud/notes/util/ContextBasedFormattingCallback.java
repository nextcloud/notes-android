package it.niedermann.owncloud.notes.util;

import android.view.ContextMenu;
import android.view.View;
import android.widget.EditText;

public class ContextBasedFormattingCallback implements View.OnCreateContextMenuListener {

    private static final String TAG = ContextBasedFormattingCallback.class.getCanonicalName();

    private EditText editText;

    public ContextBasedFormattingCallback(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }
}
