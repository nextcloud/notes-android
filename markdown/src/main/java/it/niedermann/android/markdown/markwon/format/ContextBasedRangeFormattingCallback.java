package it.niedermann.android.markdown.markwon.format;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.util.ClipboardUtil;

public class ContextBasedRangeFormattingCallback implements ActionMode.Callback {

    private static final String TAG = ContextBasedRangeFormattingCallback.class.getSimpleName();

    private final MarkwonMarkdownEditor editText;

    public ContextBasedRangeFormattingCallback(MarkwonMarkdownEditor editText) {
        this.editText = editText;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.context_based_range_formatting, menu);

        final SparseIntArray styleFormatMap = new SparseIntArray();
        styleFormatMap.append(R.id.bold, Typeface.BOLD);
        styleFormatMap.append(R.id.italic, Typeface.ITALIC);

        MenuItem item;
        CharSequence title;
        SpannableString spannableString;

        for (int i = 0; i < styleFormatMap.size(); i++) {
            item = menu.findItem(styleFormatMap.keyAt(i));
            title = item.getTitle();
            spannableString = new SpannableString(title);
            spannableString.setSpan(new StyleSpan(styleFormatMap.valueAt(i)), 0, title.length(), 0);
            item.setTitle(spannableString);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        final CharSequence text = editText.getText();
        if (text != null) {
            final int selectionStart = editText.getSelectionStart();
            final int selectionEnd = editText.getSelectionEnd();
            if (selectionStart >= 0 && selectionStart <= text.length()) {
                if (MarkdownUtil.selectionIsInLink(text, selectionStart, selectionEnd)) {
                    menu.findItem(R.id.link).setVisible(false);
                    Log.i(TAG, "Hide link menu item because the selection is already within a link.");
                }
            } else {
                Log.e(TAG, "SelectionStart is " + selectionStart + ". Expected to be between 0 and " + text.length());
            }
        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        final Editable editable = editText.getText();
        if (editable != null) {
            final int itemId = item.getItemId();
            final int start = editText.getSelectionStart();
            final int end = editText.getSelectionEnd();

            if (itemId == R.id.bold) {
                final int newSelection = MarkdownUtil.togglePunctuation(editable, start, end, "**");
                editText.setMarkdownStringModel(editable);
                editText.setSelection(newSelection);
                return true;
            } else if (itemId == R.id.italic) {
                final int newSelection = MarkdownUtil.togglePunctuation(editable, start, end, "*");
                editText.setMarkdownStringModel(editable);
                editText.setSelection(newSelection);
                return true;
            } else if (itemId == R.id.link) {
                final int newSelection = MarkdownUtil.insertLink(editable, start, end, ClipboardUtil.INSTANCE.getClipboardURLorNull(editText.getContext()));
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
