package it.niedermann.owncloud.notes.edit;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.shared.util.MarkDownUtil;

import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.CHECKBOX_UNCHECKED_MINUS_TRAILING_SPACE;
import static it.niedermann.owncloud.notes.shared.util.MarkDownUtil.CHECKBOX_UNCHECKED_STAR_TRAILING_SPACE;

/**
 * Implements auto-continuation for checked-lists
 */
public abstract class NotesTextWatcher implements TextWatcher {

    private static final String TAG = NotesTextWatcher.class.getSimpleName();

    private static final String codeBlock = "```";

    private static final int lengthCheckbox = 6;

    private boolean resetSelection = false;
    private boolean afterTextChangedHandeled = false;
    private int resetSelectionTo = -1;

    private final EditText editText;

    protected NotesTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing to do here...
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // https://github.com/stefan-niedermann/nextcloud-notes/issues/608
        if (count == 1 && s.charAt(start) == '\n') { // 'Enter' was pressed
            autoContinueCheckboxListsOnEnter(s, start, count);
        }
        // https://github.com/stefan-niedermann/nextcloud-notes/issues/558
        if (s.toString().contains(codeBlock)) {
            preventCursorJumpToTopWithinCodeBlock(s, start, count);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (resetSelection && !afterTextChangedHandeled) {
            Log.v(TAG, "Resetting selection to " + resetSelectionTo);
            afterTextChangedHandeled = true;
            setNewText(new StringBuilder(s), resetSelectionTo);
            afterTextChangedHandeled = false;
            resetSelection = false;
            resetSelectionTo = -1;
        }
    }

    private void autoContinueCheckboxListsOnEnter(@NonNull CharSequence s, int start, int count) {
        // Find start of line
        int startOfLine = MarkDownUtil.getStartOfLine(s, start);
        String line = s.subSequence(startOfLine, start).toString();

        if (line.equals(CHECKBOX_UNCHECKED_MINUS_TRAILING_SPACE) || line.equals(CHECKBOX_UNCHECKED_STAR_TRAILING_SPACE)) {
            editText.setSelection(startOfLine + 1);
            setNewText(new StringBuilder(s).replace(startOfLine, startOfLine + lengthCheckbox + 1, "\n"), startOfLine + 1);
        } else if (MarkDownUtil.lineStartsWithCheckbox(line, false)) {
            setNewText(new StringBuilder(s).insert(start + count, CHECKBOX_UNCHECKED_MINUS_TRAILING_SPACE), start + lengthCheckbox + 1);
        } else if (MarkDownUtil.lineStartsWithCheckbox(line, true)) {
            setNewText(new StringBuilder(s).insert(start + count, CHECKBOX_UNCHECKED_STAR_TRAILING_SPACE), start + lengthCheckbox + 1);
        }
    }

    private void preventCursorJumpToTopWithinCodeBlock(@NonNull CharSequence s, int start, int count) {
        // Find start of line
        int startOfLine = MarkDownUtil.getStartOfLine(s, start);
        String line = s.subSequence(startOfLine, start).toString();
        // "start" is the direct sibling of the codeBlock
        if (line.startsWith(codeBlock) && start - startOfLine == codeBlock.length() && !resetSelection) {
            resetSelectionTo = editText.getSelectionEnd();
            resetSelection = true;
            Log.v(TAG, "Entered a character directly behind a codeBlock - prepare selection reset to " + resetSelectionTo);
        } else if (s.subSequence(startOfLine, start + count).toString().startsWith(codeBlock) && !resetSelection) {
            resetSelectionTo = editText.getSelectionEnd();
            resetSelection = true;
            Log.v(TAG, "One completed a ``-codeBlock with the third `-character - prepare selection reset to " + resetSelectionTo);
        }
    }

    private void setNewText(@NonNull StringBuilder newText, int selection) {
        editText.setText(newText);
        editText.setSelection(selection);
    }
}
