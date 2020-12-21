package it.niedermann.android.markdown.rxmarkdown;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yydcdut.markdown.MarkdownEditText;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.edit.EditFactory;

import it.niedermann.android.markdown.MarkdownEditor;

import static androidx.lifecycle.Transformations.distinctUntilChanged;

@Deprecated
public class RxMarkdownEditor extends MarkdownEditText implements MarkdownEditor {

    private final MutableLiveData<CharSequence> unrenderedText$ = new MutableLiveData<>();
    private MarkdownProcessor markdownProcessor;

    public RxMarkdownEditor(Context context) {
        super(context);
        init(context);
    }

    public RxMarkdownEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RxMarkdownEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        markdownProcessor = new MarkdownProcessor(context);
        markdownProcessor.config(RxMarkdownUtil.getMarkDownConfiguration(context).build());
        markdownProcessor.factory(EditFactory.create());
        markdownProcessor.live(this);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                unrenderedText$.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        setText(markdownProcessor.parse(text));
    }

    @Override
    public LiveData<CharSequence> getMarkdownString() {
        return distinctUntilChanged(unrenderedText$);
    }
}
