package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.model.EListType;

import static it.niedermann.android.markdown.MarkdownUtil.getEndOfLine;
import static it.niedermann.android.markdown.MarkdownUtil.getStartOfLine;

/**
 * Automatically lowers indention when pressing <kbd>Backspace</kbd> on lists and check lists
 */
public class LowerIndentionTextWatcher extends InterceptorTextWatcher {

    @NonNull
    private final MarkwonMarkdownEditor editText;

    private boolean backspacePressed = false;
    private int cursor = 0;

    public LowerIndentionTextWatcher(@NonNull TextWatcher originalWatcher, @NonNull MarkwonMarkdownEditor editText) {
        super(originalWatcher);
        this.editText = editText;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count == 0 && before == 1) {
            if (editText.getSelectionStart() == editText.getSelectionEnd()) {
                backspacePressed = true;
                cursor = start;
            }
        }
        originalWatcher.onTextChanged(s, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (backspacePressed) {
            if (!handleBackspace(editable, cursor)) {
                originalWatcher.afterTextChanged(editable);
            }
            backspacePressed = false;
        } else {
            originalWatcher.afterTextChanged(editable);
        }
        editText.setMarkdownStringModel(editable);
    }

    private boolean handleBackspace(@NonNull Editable editable, int cursor) {
        final int lineStart = getStartOfLine(editable, cursor);
        final int lineEnd = getEndOfLine(editable, cursor);

        // The cursor must be at the end of the line to automatically continue
        if (cursor != lineEnd) {
            return false;
        }

        final String line = editable.subSequence(lineStart, lineEnd).toString();
        final String trimmedLine = line.trim();

        // There must be no content in this list item to automatically continue
        if ((line.indexOf(trimmedLine) + trimmedLine.length()) < line.length()) {
            return false;
        }

        for (EListType listType : EListType.values()) {
            if (listType.listSymbol.equals(trimmedLine)) {
                if (trimmedLine.length() == EListType.DASH.listSymbol.length()) {
                    return lowerIndention(editable, line, lineStart, lineEnd);
                }
            } else if (listType.checkboxUnchecked.equals(trimmedLine) || listType.checkboxChecked.equals(trimmedLine)) {
                if (trimmedLine.length() == EListType.DASH.checkboxUnchecked.length()) {
                    return lowerIndention(editable, line, lineStart, lineEnd);
                }
            }
        }
        return false;
    }

    private boolean lowerIndention(@NonNull Editable editable, String line, int lineStart, int lineEnd) {
        if (line.startsWith("  ")) {
            editable.insert(lineEnd, " ");
            editable.replace(lineStart, lineStart + 2, "");
            return true;
        } else if (line.startsWith(" ")) {
            editable.insert(lineEnd, " ");
            editable.replace(lineStart, lineStart + 1, "");
            return true;
        }
        return false;
    }
}
