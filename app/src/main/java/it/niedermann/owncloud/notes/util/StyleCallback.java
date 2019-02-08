package it.niedermann.owncloud.notes.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import it.niedermann.owncloud.notes.R;

public class StyleCallback implements ActionMode.Callback {

    private EditText editText;

    public StyleCallback(EditText editText) {
        this.editText = editText;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.style, menu);
        menu.removeItem(android.R.id.selectAll);

        SparseIntArray styleFormatMap = new SparseIntArray();
        styleFormatMap.append(R.id.bold, Typeface.BOLD);
        styleFormatMap.append(R.id.italic, Typeface.ITALIC);

        MenuItem item;
        CharSequence title;
        SpannableStringBuilder ssb;

        for (int i = 0; i < styleFormatMap.size(); i++) {
            item = menu.findItem(styleFormatMap.keyAt(i));
            title = item.getTitle();
            ssb = new SpannableStringBuilder(title);
            ssb.setSpan(new StyleSpan(styleFormatMap.valueAt(i)), 0, title.length(), 0);
            item.setTitle(ssb);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        SpannableStringBuilder ssb = new SpannableStringBuilder(editText.getText());
        final String markdown;


        switch (item.getItemId()) {
            case R.id.bold:
                markdown = "**";
                if (hasAlreadyMarkdown(start, end, markdown)) {
                    this.removeMarkdown(ssb, start, end, markdown);
                } else {
                    this.addMarkdown(ssb, start, end, markdown, Typeface.BOLD);
                }
                editText.setText(ssb);
                editText.setSelection(end + markdown.length() * 2);
                break;
            case R.id.italic:
                markdown = "*";
                if (hasAlreadyMarkdown(start, end, markdown)) {
                    this.removeMarkdown(ssb, start, end, markdown);
                } else {
                    this.addMarkdown(ssb, start, end, markdown, Typeface.ITALIC);
                }
                editText.setText(ssb);
                editText.setSelection(end + markdown.length() * 2);
                break;
            case R.id.link:
                boolean textToFormatIsLink = TextUtils.indexOf(editText.getText().subSequence(start, end), "http") == 0;
                if(textToFormatIsLink) {
                    ssb.insert(end, ")");
                    ssb.insert(start, "[](");
                } else {
                    ssb.insert(end, "]()");
                    ssb.insert(start, "[");
                }
                end++;
                ssb.setSpan(new StyleSpan(Typeface.NORMAL), start, end, 1);
                editText.setText(ssb);
                if(textToFormatIsLink) {
                    editText.setSelection(start + 1);
                } else {
                    editText.setSelection(end + 2); // after <end>](
                }
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    private boolean hasAlreadyMarkdown(int start, int end, String markdown) {
        return start > markdown.length() && markdown.contentEquals(editText.getText().subSequence(start - markdown.length(), start)) &&
                editText.getText().length() > end + markdown.length() && markdown.contentEquals(editText.getText().subSequence(end, end + markdown.length()));
    }

    private void removeMarkdown(SpannableStringBuilder ssb, int start, int end, String markdown) {
        // FIXME disabled, because it does not work properly and might cause data loss
        // ssb.delete(start - markdown.length(), start);
        // ssb.delete(end - markdown.length(), end);
        // ssb.setSpan(new StyleSpan(Typeface.NORMAL), start, end, 1);
    }

    private void addMarkdown(SpannableStringBuilder ssb, int start, int end, String markdown, int typeface) {
        ssb.insert(end, markdown);
        ssb.insert(start, markdown);
        editText.getText().charAt(start);
        editText.getText().charAt(start + 1);
        end += markdown.length() * 2;
        ssb.setSpan(new StyleSpan(typeface), start, end, 1);
    }
}
