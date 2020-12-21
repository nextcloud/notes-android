package it.niedermann.android.markdown.rxmarkdown;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.MarkdownTextView;
import com.yydcdut.markdown.syntax.text.TextFactory;

import java.util.Map;

import it.niedermann.android.markdown.MarkdownEditor;

import static it.niedermann.android.markdown.MentionUtil.setupMentions;

@Deprecated
public class RxMarkdownViewer extends MarkdownTextView implements MarkdownEditor {

    private MarkdownProcessor markdownProcessor;

    public RxMarkdownViewer(Context context) {
        super(context);
        init(context);
    }

    public RxMarkdownViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RxMarkdownViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        markdownProcessor = new MarkdownProcessor(context);
        markdownProcessor.config(RxMarkdownUtil.getMarkDownConfiguration(context).build());
        markdownProcessor.factory(TextFactory.create());
    }

    @Override
    public void setMarkdownString(CharSequence text) {
        setText(markdownProcessor.parse(text));
    }

    @Override
    public LiveData<CharSequence> getMarkdownString() {
        return new MutableLiveData<>();
    }

    @Override
    public void setMarkdownString(CharSequence text, @NonNull Map<String, String> mentions) {
        try {
            setMarkdownString(text);
            setupMentions(SingleAccountHelper.getCurrentSingleSignOnAccount(getContext()), mentions, this);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
    }
}
