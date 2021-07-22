package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.model.EListType;

import static it.niedermann.android.markdown.MarkdownUtil.getEndOfLine;
import static it.niedermann.android.markdown.MarkdownUtil.getStartOfLine;

public class LowerIndentionKeyListener implements View.OnKeyListener {

    private final MarkwonMarkdownEditor editor;

    public LowerIndentionKeyListener(@NonNull MarkwonMarkdownEditor editor) {
        this.editor = editor;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            if (editor.getSelectionStart() == editor.getSelectionEnd()) {
                final int cursor = editor.getSelectionStart();
                final Editable text = editor.getEditableText();
                final int lineStart = getStartOfLine(text, cursor);
                final int lineEnd = getEndOfLine(text, cursor);
                if (cursor != lineEnd) {
                    return false;
                }
                final String line = text.subSequence(lineStart, lineEnd).toString();
                final String trimmedLine = line.trim();
                for (EListType listType : EListType.values()) {
                    if (listType.listSymbol.equals(trimmedLine)) {
                        if (line.trim().length() == EListType.DASH.listSymbol.length()) {
                            return lowerIndention(line, lineStart, text);
                        }
                    } else if (listType.checkboxUnchecked.equals(trimmedLine) || listType.checkboxChecked.equals(trimmedLine)) {
                        if (line.trim().length() == EListType.DASH.checkboxUnchecked.length()) {
                            return lowerIndention(line, lineStart, text);
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean lowerIndention(String line, int lineStart, @NonNull Editable text) {
        if (line.startsWith("  ")) {
            text.replace(lineStart, lineStart + 2, "");
            editor.setMarkdownStringModel(text);
            return true;
        } else if (line.startsWith(" ")) {
            text.replace(lineStart, lineStart + 1, "");
            editor.setMarkdownStringModel(text);
            return true;
        }
        return false;
    }
}
