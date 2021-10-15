package it.niedermann.android.markdown.markwon.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.model.EListType;

import static it.niedermann.android.markdown.MarkdownUtil.getEndOfLine;
import static it.niedermann.android.markdown.MarkdownUtil.getListItemIfIsEmpty;
import static it.niedermann.android.markdown.MarkdownUtil.getOrderedListNumber;
import static it.niedermann.android.markdown.MarkdownUtil.getStartOfLine;
import static it.niedermann.android.markdown.MarkdownUtil.lineStartsWithCheckbox;

/**
 * Automatically continues lists and checkbox lists when pressing enter
 */
public class AutoContinuationTextWatcher extends InterceptorTextWatcher {

    @NonNull
    private final MarkwonMarkdownEditor editText;

    private CharSequence customText = null;
    private CharSequence oldText = null;
    private boolean isInsert = true;
    private int sequenceStart = 0;
    private static final Pattern REGEX_WHITESPACES = Pattern.compile("^\\s*");

    public AutoContinuationTextWatcher(@NonNull TextWatcher originalWatcher, @NonNull MarkwonMarkdownEditor editText) {
        super(originalWatcher);
        this.editText = editText;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count > 0) {
            final var inserted = getInsertedString(s, start, before, count);
            if (inserted.length() > 0 && inserted.charAt(inserted.length() - 1) == '\n') {
                handleNewlineInserted(s, start, count);
            }
        }
        oldText = s.toString();
        originalWatcher.onTextChanged(s, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (customText != null) {
            final var customText = this.customText;
            this.customText = null;
            if (isInsert) {
                insertCustomText(s, customText);
            } else {
                deleteCustomText(s, customText);
            }
        } else {
            originalWatcher.afterTextChanged(s);
        }
        editText.setMarkdownStringModel(s);
    }

    private CharSequence getInsertedString(CharSequence newText, int start, int before, int count) {
        if (newText != null && newText.length() > (oldText == null ? 0 : oldText.length())) {
            // character added
            final int position = start + before;
            return newText.subSequence(position, position + count - before);
        }
        return "";
    }

    private void deleteCustomText(Editable s, CharSequence customText) {
        s.replace(sequenceStart, Math.min(sequenceStart + customText.length() + 1, s.length()), "\n");
        editText.setSelection(sequenceStart + 1);
    }

    private void insertCustomText(Editable s, CharSequence customText) {
        s.insert(sequenceStart, customText);
    }

    private void handleNewlineInserted(CharSequence originalSequence, int start, int count) {
        final var s = originalSequence.subSequence(0, originalSequence.length());
        final int startOfLine = getStartOfLine(s, start);
        final String line = s.subSequence(startOfLine, getEndOfLine(s, start)).toString();

        final var emptyListString = getListItemIfIsEmpty(line);
        if (emptyListString.isPresent()) {
            customText = emptyListString.get();
            isInsert = false;
            sequenceStart = startOfLine;
        } else {
            final var builder = new StringBuilder();
            final var matcher = REGEX_WHITESPACES.matcher(line);
            if (matcher.find()) {
                builder.append(matcher.group());
            }
            final String trimmedLine = line.trim();
            for (final var listType : EListType.values()) {
                final boolean isCheckboxList = lineStartsWithCheckbox(trimmedLine, listType);
                final boolean isPlainList = !isCheckboxList && trimmedLine.startsWith(listType.listSymbolWithTrailingSpace);
                if (isPlainList || isCheckboxList) {
                    builder.append(isPlainList ? listType.listSymbolWithTrailingSpace : listType.checkboxUncheckedWithTrailingSpace);
                    customText = builder;
                    isInsert = true;
                    sequenceStart = start + count;
                    return;
                }
            }

            final var orderedListNumber = getOrderedListNumber(trimmedLine);
            if (orderedListNumber.isPresent()) {
                customText = builder.append(orderedListNumber.get() + 1).append(". ");
                isInsert = true;
                sequenceStart = start + count;
            }
        }
    }
}
