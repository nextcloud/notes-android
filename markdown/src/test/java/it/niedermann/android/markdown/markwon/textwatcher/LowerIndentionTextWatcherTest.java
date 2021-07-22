package it.niedermann.android.markdown.markwon.textwatcher;

import android.view.KeyEvent;
import android.widget.EditText;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;
import it.niedermann.android.markdown.model.EListType;

@RunWith(RobolectricTestRunner.class)
public class LowerIndentionTextWatcherTest extends TestCase {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EditText editText;

    @Before
    public void reset() {
        this.editText = new MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void shouldLowerIndentionByTwoWhenPressingBackspaceOnAnIndentedList() {
        this.editText.setText("  - ");
        pressBackspace(4);
        assertText("- ", 2);

        this.editText.setText("- [ ] Foo\n  - [ ] ");
        pressBackspace(18);
        assertText("- [ ] Foo\n- [ ] ", 16);
    }

    @Test
    public void shouldLowerIndentionByOneWhenPressingBackspaceOnAListWhichIsIndentedByOneSpace() {
        this.editText.setText(" - ");
        pressBackspace(3);
        assertText("- ", 2);
    }

    @Test
    public void shouldNotLowerIndentionByOneWhenCursorIsNotAtTheEnd() {
        this.editText.setText(" - ");
        pressBackspace(2);
        assertText("  ", 1);

        this.editText.setText("  - ");
        pressBackspace(0);
        assertText("  - ", 0);

        this.editText.setText("  - ");
        pressBackspace(3);
        assertText("   ", 2);

        this.editText.setText("  - ");
        pressBackspace(2);
        assertText(" - ", 1);

        this.editText.setText("- Foo\n  - ");
        pressBackspace(9);
        assertText("- Foo\n   ", 8);
    }

    @Test
    public void shouldDeleteLastCharacterWhenPressingBackspace() {
        this.editText.setText("");
        pressBackspace(0);
        assertText("", 0);

        this.editText.setText("- [ ] ");
        pressBackspace(6);
        assertText("- [ ]", 5);

        this.editText.setText("- Foo");
        pressBackspace(5);
        assertText("- Fo", 4);

        this.editText.setText("- [ ] Foo");
        pressBackspace(9);
        assertText("- [ ] Fo", 8);
    }

    private void pressBackspace(int atPosition) {
        this.editText.setSelection(atPosition);
        this.editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0));
    }

    private void assertText(String expected, int cursorPosition) {
        assertEquals(expected, this.editText.getText().toString());
        assertEquals(cursorPosition, this.editText.getSelectionStart());
        assertEquals(cursorPosition, this.editText.getSelectionEnd());
    }
}
