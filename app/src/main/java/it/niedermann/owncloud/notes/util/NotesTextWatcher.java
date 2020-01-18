package it.niedermann.owncloud.notes.util;

import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Implements auto-continuation for checked-lists
 */
public abstract class NotesTextWatcher implements TextWatcher {
    private static final String uncheckedMinusCheckbox = "- [ ] ";
    private static final String uncheckedStarCheckbox = "* [ ] ";
    private static final String checkedMinusCheckbox = "- [x] ";
    private static final String checkedStarCheckbox = "* [x] ";
    private static final int lengthCheckbox = 6;

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
        if (count == 1 && s.charAt(start) == '\n') { // 'Enter' was pressed
            // Find start of line
            int startOfLine = start;
            while (startOfLine > 0 && s.charAt(startOfLine - 1) != '\n') {
                startOfLine--;
            }
            String line = s.subSequence(startOfLine, start).toString();

            if (line.equals(uncheckedMinusCheckbox) || line.equals(uncheckedStarCheckbox)) {
                editText.setSelection(startOfLine + 1);
                setNewText(new StringBuilder(s).replace(startOfLine, startOfLine + lengthCheckbox + 1, "\n"), startOfLine + 1);
            } else if(lineStartsWithCheckbox(line, false)) {
                setNewText(new StringBuilder(s).insert(start + count, uncheckedMinusCheckbox), start + lengthCheckbox + 1);
            } else if(lineStartsWithCheckbox(line, true)) {
                setNewText(new StringBuilder(s).insert(start + count, uncheckedStarCheckbox), start + lengthCheckbox + 1);
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
