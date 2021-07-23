package it.niedermann.android.markdown.markwon.textwatcher;

import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
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

import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_B;
import static android.view.KeyEvent.KEYCODE_C;
import static android.view.KeyEvent.KEYCODE_CTRL_LEFT;
import static android.view.KeyEvent.KEYCODE_F;
import static android.view.KeyEvent.KEYCODE_LEFT_BRACKET;
import static android.view.KeyEvent.KEYCODE_MINUS;
import static android.view.KeyEvent.KEYCODE_O;
import static android.view.KeyEvent.KEYCODE_PERIOD;
import static android.view.KeyEvent.KEYCODE_Q;
import static android.view.KeyEvent.KEYCODE_R;
import static android.view.KeyEvent.KEYCODE_RIGHT_BRACKET;
import static android.view.KeyEvent.KEYCODE_SPACE;
import static android.view.KeyEvent.KEYCODE_STAR;
import static android.view.KeyEvent.KEYCODE_U;
import static android.view.KeyEvent.KEYCODE_X;

@RunWith(RobolectricTestRunner.class)
public class TextWatcherIntegrationTest extends TestCase {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EditText editText;

    @Before
    public void reset() {
        this.editText = new MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void shouldSuccessfullyHandleMultipleTextEdits() {
        this.editText.setText("");
        assertEquals("");

        sendKeys(KEYCODE_F, KEYCODE_O, KEYCODE_O);
        assertEquals("foo");

        pressEnter();
        assertEquals("foo\n");

        sendCheckboxKeys();
        assertEquals("foo\n- [ ] ");

        pressEnter();
        assertEquals("foo\n\n");

        sendCheckboxKeys();
        sendKeys(KEYCODE_B, KEYCODE_A, KEYCODE_R);
        assertEquals("foo\n\n- [ ] bar");

        pressEnter();
        assertEquals("foo\n\n- [ ] bar\n- [ ] ");

        pressEnter();
        assertEquals("foo\n\n- [ ] bar\n\n");

        pressBackspace();
        assertEquals("foo\n\n- [ ] bar\n");

        sendKeys(KEYCODE_SPACE, KEYCODE_SPACE);
        sendCheckboxKeys();
        sendKeys(KEYCODE_B, KEYCODE_A, KEYCODE_R);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar");

        pressEnter();
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  - [ ] ");

        pressBackspace();
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n- [ ] ");

        pressBackspace(6, 33);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n");

        sendKeys(KEYCODE_SPACE, KEYCODE_SPACE, KEYCODE_1, KEYCODE_PERIOD, KEYCODE_SPACE, KEYCODE_Q, KEYCODE_U, KEYCODE_X);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  1. qux");

        pressEnter();
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  1. qux\n  2. ");

        pressEnter(14);
        pressBackspace(6, 21);
        assertEquals("foo\n\n- [ ] bar\n\n  - [ ] bar\n  1. qux\n  2. ");

        sendKeys(KEYCODE_SPACE, KEYCODE_SPACE, KEYCODE_STAR);
        pressBackspace();
        sendKeys(KEYCODE_STAR, KEYCODE_SPACE, KEYCODE_A, KEYCODE_B, KEYCODE_C);
        assertEquals("foo\n\n- [ ] bar\n  * abc\n  - [ ] bar\n  1. qux\n  2. ");
    }


    // Convenience methods

    private void sendCheckboxKeys() {
        sendKeys(KEYCODE_MINUS, KEYCODE_SPACE, KEYCODE_CTRL_LEFT, KEYCODE_LEFT_BRACKET, KEYCODE_SPACE, KEYCODE_RIGHT_BRACKET, KEYCODE_SPACE);
    }

    private void assertEquals(@NonNull CharSequence expected) {
        assertEquals(expected.toString(), editText.getText().toString());
    }

    private void pressEnter(@SuppressWarnings("SameParameterValue") int atPosition) {
        this.editText.setSelection(atPosition);
        pressEnter();
    }

    private void pressEnter() {
        this.editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    private void pressBackspace(@SuppressWarnings("SameParameterValue") int times, int startPosition) {
        if (times > startPosition) {
            throw new IllegalArgumentException("startPosition must be bigger or equal to times");
        }
        while (times > 0) {
            times--;
            pressBackspace(startPosition--);
        }
    }

    private void pressBackspace(int atPosition) {
        this.editText.setSelection(atPosition);
        pressBackspace();
    }

    private void pressBackspace() {
        this.editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    private void sendKeys(int... keyCodes) {
        int release = -1;
        for (int k : keyCodes) {
            if (release != -1) {
                this.editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, release));
                release = -1;
            }
            switch (k) {
                case KeyEvent.KEYCODE_CTRL_LEFT:
                case KeyEvent.KEYCODE_CTRL_RIGHT: {
                    release = k;
                }
            }
            this.editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, k));
        }
    }
}
