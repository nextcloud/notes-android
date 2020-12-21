package it.niedermann.android.markdown;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.android.markdown.markwon.MarkwonMarkdownViewer;

public class MarkdownViewerImpl extends MarkwonMarkdownViewer {

    public MarkdownViewerImpl(@NonNull Context context) {
        super(context);
    }

    public MarkdownViewerImpl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkdownViewerImpl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
