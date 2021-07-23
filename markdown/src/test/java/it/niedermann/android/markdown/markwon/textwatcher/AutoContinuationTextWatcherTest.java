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
public class AutoContinuationTextWatcherTest extends TestCase {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EditText editText;

    @Before
    public void reset() {
        this.editText = new MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void shouldContinueSimpleLists() {
        for (EListType listType : EListType.values()) {
            this.editText.setText(listType.listSymbolWithTrailingSpace + "Test");
            pressEnter(6);
            assertText(listType.listSymbolWithTrailingSpace + "Test\n" + listType.listSymbolWithTrailingSpace, 9);
        }
        for (EListType listType : EListType.values()) {
            this.editText.setText(listType.checkboxUncheckedWithTrailingSpace + "Test");
            pressEnter(10);
            assertText(listType.checkboxUncheckedWithTrailingSpace + "Test\n" + listType.checkboxUncheckedWithTrailingSpace, 17);
        }
        for (EListType listType : EListType.values()) {
            this.editText.setText(listType.checkboxChecked + " Test");
            pressEnter(10);
            assertText(listType.checkboxChecked + " Test\n" + listType.checkboxUncheckedWithTrailingSpace, 17);
        }

        this.editText.setText("1. Test");
        pressEnter(7);
        assertText("1. Test\n2. ", 11);

        this.editText.setText("11. Test");
        pressEnter(8);
        assertText("11. Test\n12. ", 13);
    }

    @Test
    public void shouldContinueListsWithMultipleItems() {
        final CharSequence sample = "- [ ] Foo\n- [x] Bar\n- [ ] Baz\n\nQux";

        this.editText.setText(sample);
        pressEnter(0);
        assertText("\n- [ ] Foo\n- [x] Bar\n- [ ] Baz\n\nQux", 1);

        this.editText.setText(sample);
        pressEnter(9);
        assertText("- [ ] Foo\n- [ ] \n- [x] Bar\n- [ ] Baz\n\nQux", 16);

        this.editText.setText(sample);
        pressEnter(19);
        assertText("- [ ] Foo\n- [x] Bar\n- [ ] \n- [ ] Baz\n\nQux", 26);
    }

    @Test
    public void shouldSplitItemIfCursorIsNotOnEnd() {
        this.editText.setText("- [ ] Foo\n- [x] Bar");
        pressEnter(8);
        assertText("- [ ] Fo\n- [ ] o\n- [x] Bar", 15);
    }

    @Test
    public void shouldContinueNestedListsAtTheSameIndention() {
        this.editText.setText("- [ ] Parent\n  - [x] Child\n    - [ ] Third");
        pressEnter(26);
        assertText("- [ ] Parent\n  - [x] Child\n  - [ ] \n    - [ ] Third", 35);

        this.editText.setText("- [ ] Parent\n  - [x] Child\n    - [ ] Third");
        pressEnter(42);
        assertText("- [ ] Parent\n  - [x] Child\n    - [ ] Third\n    - [ ] ", 53);
    }

    @Test
    public void shouldRemoveAutoContinuedListItemWhenPressingEnterMultipleTimes() {
        this.editText.setText("- Foo");
        pressEnter(5);
        assertText("- Foo\n- ", 8);
        pressEnter(8);
        assertText("- Foo\n\n", 7);
        pressEnter(7);
        assertText("- Foo\n\n\n", 8);

        this.editText.setText("- Foo\n  - Bar");
        pressEnter(13);
        assertText("- Foo\n  - Bar\n  - ", 18);
        pressEnter(18);
        assertText("- Foo\n  - Bar\n\n", 15);
        pressEnter(15);
        assertText("- Foo\n  - Bar\n\n\n", 16);
    }

    @Test
    public void shouldNotContinueIfNoList() {
        this.editText.setText("Foo");
        pressEnter(3);
        assertText("Foo\n", 4);

        this.editText.setText(" Foo");
        pressEnter(4);
        assertText(" Foo\n", 5);
    }

    @Test
    public void shouldNotContinueIfBlank() {
        this.editText.setText("");
        pressEnter(0);
        assertText("\n", 1);
    }

    private void pressEnter(int atPosition) {
        this.editText.setSelection(atPosition);
        this.editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0));
    }

    private void assertText(String expected, int cursorPosition) {
        assertEquals(expected, this.editText.getText().toString());
        assertEquals(cursorPosition, this.editText.getSelectionStart());
        assertEquals(cursorPosition, this.editText.getSelectionEnd());
    }
}
