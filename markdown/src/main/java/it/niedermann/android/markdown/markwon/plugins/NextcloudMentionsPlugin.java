package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.Map;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;

import static it.niedermann.android.markdown.MentionUtil.setupMentions;

public class NextcloudMentionsPlugin extends AbstractMarkwonPlugin {

    @NonNull
    private final Context context;
    @NonNull
    private final Map<String, String> mentions;

    private NextcloudMentionsPlugin(@NonNull Context context, @NonNull Map<String, String> mentions) {
        this.context = context.getApplicationContext();
        this.mentions = mentions;
    }

    public static MarkwonPlugin create(@NonNull Context context, @NonNull Map<String, String> mentions) {
        return new NextcloudMentionsPlugin(context, mentions);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        try {
            setupMentions(SingleAccountHelper.getCurrentSingleSignOnAccount(context), mentions, textView);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
    }
}
