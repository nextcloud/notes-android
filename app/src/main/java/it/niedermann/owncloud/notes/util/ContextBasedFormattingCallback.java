package it.niedermann.owncloud.notes.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import it.niedermann.owncloud.notes.R;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ContextBasedFormattingCallback implements ActionMode.Callback {

    private static final String TAG = ContextBasedFormattingCallback.class.getCanonicalName();

    private EditText editText;

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
        int startOfLine = originalCursorPosition;
        int endOfLine = originalCursorPosition;
        while (startOfLine > 0 && text.charAt(startOfLine - 1) != '\n') {
            startOfLine--;
        }
        if (endOfLine != startOfLine) {
            while (endOfLine < text.length() && text.charAt(endOfLine + 1) != '\n') {
                endOfLine--;
            }
        }
        String line = text.subSequence(startOfLine, endOfLine).toString();
        if (NotesTextWatcher.lineStartsWithCheckbox(line, true) || NotesTextWatcher.lineStartsWithCheckbox(line, false)) {
            menu.findItem(R.id.checkbox).setVisible(false);
            Log.i(TAG, "Hide checkbox menu item because line starts already with checkbox");
        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.checkbox: {
                // TODO only if line does not already start with a checkbox
                CharSequence text = editText.getText();
                int originalCursorPosition = editText.getSelectionStart();
                int startOfLine = originalCursorPosition;
                int endOfLine = originalCursorPosition;
                while (startOfLine > 0 && text.charAt(startOfLine - 1) != '\n') {
                    startOfLine--;
                }
                if (endOfLine != startOfLine) {
                    while (endOfLine < text.length() && text.charAt(endOfLine + 1) != '\n') {
                        endOfLine--;
                    }
                }
                CharSequence part1 = text.subSequence(0, startOfLine);
                CharSequence part2 = text.subSequence(startOfLine, text.length());
                editText.setText(TextUtils.concat(part1, "- [ ] ", part2));
                editText.setSelection(originalCursorPosition + 6);
                return true;
            }
            case R.id.link: {
                SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
                int start = editText.getText().length();
                int end = start;
                boolean textToFormatIsLink = TextUtils.indexOf(editText.getText().subSequence(start, end), "http") == 0;
                if (textToFormatIsLink) {
                    ssb.insert(end, ")");
                    ssb.insert(start, "[](");
                } else {
                    String clipboardURL = getClipboardURLorNull(editText.getContext());
                    if (clipboardURL != null) {
                        ssb.insert(end, "](" + clipboardURL + ")");
                        end += clipboardURL.length();
                    } else {
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
                return true;
            }
        }
        return false;
    }

    private static String getClipboardURLorNull(Context context) {
        String clipboardURL = null;
        ClipData clipboardData = Objects.requireNonNull(((ClipboardManager) Objects.requireNonNull(context.getSystemService(CLIPBOARD_SERVICE))).getPrimaryClip());
        if (clipboardData.getItemCount() > 0) {
            try {
                clipboardURL = new URL(clipboardData.getItemAt(0).getText().toString()).toString();
            } catch (MalformedURLException e) {
                Log.d(TAG, "Clipboard does not contain a valid URL: " + clipboardURL);
            }
        }
        return clipboardURL;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Nothing to do here...
    }
}
