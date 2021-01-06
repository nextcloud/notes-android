package it.niedermann.android.markdown.markwon.format;

import android.text.Editable;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.model.EListType;
import it.niedermann.android.util.ClipboardUtil;

import static it.niedermann.android.markdown.MarkdownUtil.getEndOfLine;
import static it.niedermann.android.markdown.MarkdownUtil.getStartOfLine;
import static it.niedermann.android.markdown.MarkdownUtil.lineStartsWithCheckbox;

public class ContextBasedFormattingCallback implements ActionMode.Callback {

    private static final String TAG = ContextBasedFormattingCallback.class.getSimpleName();

    private final MarkwonMarkdownEditor editText;

    public ContextBasedFormattingCallback(MarkwonMarkdownEditor editText) {
        this.editText = editText;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.context_based_formatting, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        final CharSequence text = editText.getText();
        if (text != null) {
            final int cursorPosition = editText.getSelectionStart();
            if (cursorPosition >= 0 && cursorPosition <= text.length()) {
                final int startOfLine = getStartOfLine(text, cursorPosition);
                final int endOfLine = getEndOfLine(text, cursorPosition);
                final String line = text.subSequence(startOfLine, endOfLine).toString();
                if (lineStartsWithCheckbox(line)) {
                    menu.findItem(R.id.checkbox).setVisible(false);
                    Log.i(TAG, "Hide checkbox menu item because line starts already with checkbox");
                }
            } else {
                Log.e(TAG, "SelectionStart is " + cursorPosition + ". Expected to be between 0 and " + text.length());
            }
        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final Editable editable = editText.getText();
        if (editable != null) {
            final int itemId = item.getItemId();
            final int cursorPosition = editText.getSelectionStart();

            if (itemId == R.id.checkbox) {
                editable.insert(getStartOfLine(editable, cursorPosition), EListType.DASH.checkboxUncheckedWithTrailingSpace);
                editText.setMarkdownStringModel(editable);
                editText.setSelection(cursorPosition + EListType.DASH.checkboxUncheckedWithTrailingSpace.length());
                return true;
            } else if (itemId == R.id.link) {
                final int newSelection = MarkdownUtil.insertLink(editable, cursorPosition, cursorPosition, ClipboardUtil.INSTANCE.getClipboardURLorNull(editText.getContext()));
                editText.setMarkdownStringModel(editable);
                editText.setSelection(newSelection);
                return true;
            }
        } else {
            Log.e(TAG, "Editable is null");
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Nothing to do here...
    }
}
