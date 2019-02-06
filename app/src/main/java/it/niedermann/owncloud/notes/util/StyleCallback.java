package it.niedermann.owncloud.notes.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;

public class StyleCallback implements ActionMode.Callback {

    private TextView textView;

    public StyleCallback(TextView textView) {
        this.textView = textView;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.style, menu);
        menu.removeItem(android.R.id.selectAll);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int start = textView.getSelectionStart();
        int end = textView.getSelectionEnd();
        SpannableStringBuilder ssb = new SpannableStringBuilder(textView.getText());
        final String markdown;

        switch(item.getItemId()) {
            case R.id.bold:
                markdown = "**";
                ssb.insert(end, markdown);
                ssb.insert(start, markdown);
                textView.getText().charAt(start);
                textView.getText().charAt(start + 1);
                end += markdown.length() * 2;
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, 1);
                textView.setText(ssb);
            break;
            case R.id.italic:
                markdown = "*";
                ssb.insert(end, markdown);
                ssb.insert(start, markdown);
                end += markdown.length() * 2;
                ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 1);
                textView.setText(ssb);
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
