package it.niedermann.android.markdown.markwon.textwatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LowerIndentionTextWatcherTest extends AbstractTextWatcherTest {

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
    public void shouldNotLowerIndentionIfThereIsAnyContentAfterTheList() {
        this.editText.setText("  -  ");
        pressBackspace(4);
        assertText("  - ", 3);

        this.editText.setText("  -  ");
        pressBackspace(5);
        assertText("  - ", 4);
    }

    @Test
    public void shouldNotLowerIndentionIfBackspaceWasPressedInTheNextLine() {
        this.editText.setText("  - \nFoo");
        pressBackspace(5);
        assertText("  - Foo", 4);
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
}
