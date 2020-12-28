package it.niedermann.android.markdown;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownEditor;

public class MarkdownEditorImpl extends MarkwonMarkdownEditor {
    public MarkdownEditorImpl(@NonNull Context context) {
        super(context);
    }

    public MarkdownEditorImpl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkdownEditorImpl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
