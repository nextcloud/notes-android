package it.niedermann.owncloud.notes.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * Implements auto-continuation for checked-lists
 */
public abstract class NotesTextWatcher implements TextWatcher {

    private static final String TAG = NotesTextWatcher.class.getCanonicalName();

    private static final String codeBlock = "```";

    private static final String uncheckedMinusCheckbox = "- [ ] ";
    private static final String uncheckedStarCheckbox = "* [ ] ";
    private static final String checkedMinusCheckbox = "- [x] ";
    private static final String checkedStarCheckbox = "* [x] ";
    private static final int lengthCheckbox = 6;

    private int resetSelectionTo = -1;

    private EditText editText;

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
        if (resetSelectionTo >= 0) {
            Log.v(TAG, "Resetting selection to " + resetSelectionTo);
            int clone = resetSelectionTo; // Needs a clone because setNewText triggers this method again -> endless loop
            resetSelectionTo = -1;
            setNewText(new StringBuilder(s), clone);
        }
    }

    private void autoContinueCheckboxListsOnEnter(CharSequence s, int start, int count) {
        // Find start of line
        int startOfLine = getStartOfLine(s, start);
        String line = s.subSequence(startOfLine, start).toString();

        if (line.equals(uncheckedMinusCheckbox) || line.equals(uncheckedStarCheckbox)) {
            editText.setSelection(startOfLine + 1);
            setNewText(new StringBuilder(s).replace(startOfLine, startOfLine + lengthCheckbox + 1, "\n"), startOfLine + 1);
        } else if (lineStartsWithCheckbox(line, false)) {
            setNewText(new StringBuilder(s).insert(start + count, uncheckedMinusCheckbox), start + lengthCheckbox + 1);
        } else if (lineStartsWithCheckbox(line, true)) {
            setNewText(new StringBuilder(s).insert(start + count, uncheckedStarCheckbox), start + lengthCheckbox + 1);
        }
    }

    private static int getStartOfLine(CharSequence s, int start) {
        int startOfLine = start;
        while (startOfLine > 0 && s.charAt(startOfLine - 1) != '\n') {
            startOfLine--;
        }
        return startOfLine;
    }

    private void preventCursorJumpToTopWithinCodeBlock(CharSequence s, int start, int count) {
        // Find start of line
        int startOfLine = getStartOfLine(s, start);
        String line = s.subSequence(startOfLine, start).toString();
        if (line.startsWith(codeBlock)) {
            // "start" is the direct sibling of the codeBlock
            if (start - startOfLine == codeBlock.length()) {
                if (resetSelectionTo == -1) {
                    resetSelectionTo = editText.getSelectionEnd();
                    Log.v(TAG, "Entered a character directly behind a codeBlock - prepare selection reset to " + resetSelectionTo);
                }
            }
        } else if (s.subSequence(startOfLine, start + count).toString().startsWith(codeBlock)) {
            // FIXME If starting codeblock is completed with the third `-character, the application gets an endless loop
            if (resetSelectionTo == -1) {
                resetSelectionTo = editText.getSelectionEnd();
                Log.v(TAG, "One completed a ``-codeBlock with the third `-character - prepare selection reset to " + resetSelectionTo);
            }
        }
    }

    private static boolean lineStartsWithCheckbox(String line, boolean starAsLeadingCharacter) {
        return starAsLeadingCharacter
                ? line.startsWith(uncheckedStarCheckbox) || line.startsWith(checkedStarCheckbox)
                : line.startsWith(uncheckedMinusCheckbox) || line.startsWith(checkedMinusCheckbox);
    }

    private void setNewText(StringBuilder newText, int selection) {
        editText.setText(newText);
        editText.setSelection(selection);
    }
}
