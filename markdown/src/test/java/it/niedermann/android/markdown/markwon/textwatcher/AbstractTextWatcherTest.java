package it.niedermann.android.markdown.markwon.textwatcher;

import android.view.KeyEvent;
import android.widget.EditText;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

public class AbstractTextWatcherTest extends TestCase {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    protected EditText editText;

    @Before
    public void reset() {
        this.editText = new MarkwonMarkdownEditor(ApplicationProvider.getApplicationContext());
    }

    protected void assertText(String expected, int cursorPosition) {
        assertEquals(expected, this.editText.getText().toString());
        assertEquals(cursorPosition, this.editText.getSelectionStart());
        assertEquals(cursorPosition, this.editText.getSelectionEnd());
    }

    protected void pressBackspace(int atPosition) {
        this.editText.setSelection(atPosition);
        this.editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    protected void pressEnter(int atPosition) {
        this.editText.setSelection(atPosition);
        this.editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    protected void sendKey(int... keyCodes) {
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
