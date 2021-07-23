package it.niedermann.android.markdown.markwon.textwatcher;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_B;
import static android.view.KeyEvent.KEYCODE_C;
import static android.view.KeyEvent.KEYCODE_CTRL_LEFT;
import static android.view.KeyEvent.KEYCODE_F;
import static android.view.KeyEvent.KEYCODE_LEFT_BRACKET;
import static android.view.KeyEvent.KEYCODE_MINUS;
import static android.view.KeyEvent.KEYCODE_NUMPAD_DOT;
import static android.view.KeyEvent.KEYCODE_O;
import static android.view.KeyEvent.KEYCODE_PERIOD;
import static android.view.KeyEvent.KEYCODE_Q;
import static android.view.KeyEvent.KEYCODE_R;
import static android.view.KeyEvent.KEYCODE_RIGHT_BRACKET;
import static android.view.KeyEvent.KEYCODE_S;
import static android.view.KeyEvent.KEYCODE_SPACE;
import static android.view.KeyEvent.KEYCODE_STAR;
import static android.view.KeyEvent.KEYCODE_U;
import static android.view.KeyEvent.KEYCODE_X;
import static android.view.KeyEvent.KEYCODE_Z;

@RunWith(RobolectricTestRunner.class)
public class TextWatcherIntegrationTest extends AbstractTextWatcherTest {

    @Test
    public void shouldSuccessfullyHandleMultipleTextEdits() {
        this.editText.setText("");
        assertEquals("");

        sendKey(KEYCODE_F, KEYCODE_O, KEYCODE_O);
        assertEquals("foo");

        pressEnter(3);
        assertEquals("foo\n");

        sendCheckboxKeys();
        assertEquals("foo\n- [ ] ");

        pressEnter(10);
        assertEquals("foo\n\n");

        sendCheckboxKeys();
        sendKey(KEYCODE_B, KEYCODE_A, KEYCODE_R);
        assertEquals("foo\n\n- [ ] bar");

        pressEnter(14);
        assertEquals("foo\n\n- [ ] bar\n- [ ] ");

        pressEnter(21);
        assertEquals("foo\n\n- [ ] bar\n\n");

        pressBackspace(16);
        assertEquals("foo\n\n- [ ] bar\n");

        sendKey(KEYCODE_SPACE, KEYCODE_SPACE);
        sendCheckboxKeys();
        sendKey(KEYCODE_B, KEYCODE_A, KEYCODE_R);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar");

        pressEnter(26);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  - [ ] ");

        pressBackspace(35);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n- [ ] ");

        pressBackspace(33);
        pressBackspace(32);
        pressBackspace(31);
        pressBackspace(30);
        pressBackspace(29);
        pressBackspace(28);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n");

        sendKey(KEYCODE_SPACE, KEYCODE_SPACE, KEYCODE_1, KEYCODE_PERIOD, KEYCODE_SPACE, KEYCODE_Q, KEYCODE_U, KEYCODE_X);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  1. qux");

        pressEnter(35);
        assertEquals("foo\n\n- [ ] bar\n  - [ ] bar\n  1. qux\n  2. ");

        editText.setSelection(14);
        pressEnter(14);
        pressBackspace(21);
        pressBackspace(20);
        pressBackspace(19);
        pressBackspace(18);
        pressBackspace(17);
        pressBackspace(16);
        assertEquals("foo\n\n- [ ] bar\n\n  - [ ] bar\n  1. qux\n  2. ");

        sendKey(KEYCODE_SPACE, KEYCODE_SPACE, KEYCODE_STAR);
        pressBackspace(18);
        sendKey(KEYCODE_STAR, KEYCODE_SPACE, KEYCODE_A, KEYCODE_B, KEYCODE_C);
        assertEquals("foo\n\n- [ ] bar\n  * abc\n  - [ ] bar\n  1. qux\n  2. ");
    }

    private void sendCheckboxKeys() {
        sendKey(KEYCODE_MINUS, KEYCODE_SPACE, KEYCODE_CTRL_LEFT, KEYCODE_LEFT_BRACKET, KEYCODE_SPACE, KEYCODE_RIGHT_BRACKET, KEYCODE_SPACE);
    }

    private void assertEquals(@NonNull CharSequence expected) {
        assertEquals(expected.toString(), editText.getText().toString());
    }
}
