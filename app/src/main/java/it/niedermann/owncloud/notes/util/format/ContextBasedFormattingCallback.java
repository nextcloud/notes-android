package it.niedermann.owncloud.notes.util.format;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.MarkDownUtil;

import static it.niedermann.owncloud.notes.util.ClipboardUtil.getClipboardURLorNull;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.CHECKBOX_UNCHECKED_MINUS_TRAILING_SPACE;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.getEndOfLine;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.getStartOfLine;

public class ContextBasedFormattingCallback implements ActionMode.Callback {

    private static final String TAG = ContextBasedFormattingCallback.class.getCanonicalName();

    private final EditText editText;

    public ContextBasedFormattingCallback(EditText editText) {
        this.editText = editText;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.context_based_formatting, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        CharSequence text = editText.getText();
        int originalCursorPosition = editText.getSelectionStart();
        if (originalCursorPosition >= 0 && originalCursorPosition <= text.length()) {
            int startOfLine = getStartOfLine(text, originalCursorPosition);
            int endOfLine = getEndOfLine(text, startOfLine);
            String line = text.subSequence(startOfLine, endOfLine).toString();
            if (MarkDownUtil.lineStartsWithCheckbox(line)) {
                menu.findItem(R.id.checkbox).setVisible(false);
                Log.i(TAG, "Hide checkbox menu item because line starts already with checkbox");
            }
        } else {
            Log.e(TAG, "SelectionStart is " + originalCursorPosition + ". Expected to be between 0 and " + text.length());
        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.checkbox:
                insertCheckbox();
                return true;
            case R.id.link:
                insertLink();
                return true;
            default:
                return false;
        }
    }

    private void insertCheckbox() {
        CharSequence text = editText.getText();
        int originalCursorPosition = editText.getSelectionStart();
        int startOfLine = getStartOfLine(text, originalCursorPosition);
        Log.i(TAG, "Inserting checkbox at position " + startOfLine);
        CharSequence part1 = text.subSequence(0, startOfLine);
        CharSequence part2 = text.subSequence(startOfLine, text.length());
        editText.setText(TextUtils.concat(part1, CHECKBOX_UNCHECKED_MINUS_TRAILING_SPACE, part2));
        editText.setSelection(originalCursorPosition + CHECKBOX_UNCHECKED_MINUS_TRAILING_SPACE.length());
    }

    private void insertLink() {
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
        int start = editText.getText().length();
        int end = start;
        boolean textToFormatIsLink = TextUtils.indexOf(editText.getText().subSequence(start, end), "http") == 0;
        if (textToFormatIsLink) {
            Log.i(TAG, "Inserting link description for position " + start + " to " + end);
            ssb.insert(end, ")");
            ssb.insert(start, "[](");
        } else {
            String clipboardURL = getClipboardURLorNull(editText.getContext());
            if (clipboardURL != null) {
                Log.i(TAG, "Inserting link from clipboard at position " + start + " to " + end + ": " + clipboardURL);
                ssb.insert(end, "](" + clipboardURL + ")");
                end += clipboardURL.length();
            } else {
                Log.i(TAG, "Inserting empty link for position " + start + " to " + end);
                ssb.insert(end, "]()");
            }
            ssb.insert(start, "[");
        }
        end++;
        ssb.setSpan(new StyleSpan(Typeface.NORMAL), start, end, 1);
        editText.setText(ssb);
        if (textToFormatIsLink) {
            editText.setSelection(start + 1);
        } else {
            editText.setSelection(end + 2); // after <end>](
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Nothing to do here...
    }
}
