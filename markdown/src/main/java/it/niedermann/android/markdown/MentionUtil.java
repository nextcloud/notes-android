package it.niedermann.android.markdown;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MentionUtil {

    private MentionUtil() {
        // Util class
    }

    /**
     * Replaces all mentions in the textView with an avatar and the display name
     *
     * @param account  {@link SingleSignOnAccount} where the users of those mentions belong to
     * @param mentions {@link Map} of all mentions that should be substituted, the key is the user id and the value the display name
     * @param target   target {@link TextView}
     */
    public static void setupMentions(@NonNull SingleSignOnAccount account, @NonNull Map<String, String> mentions, @NonNull TextView target) {
        final Context context = target.getContext();

        // Step 1
        // Add avatar icons and display names
        final SpannableStringBuilder messageBuilder = replaceAtMentionsWithImagePlaceholderAndDisplayName(context, mentions, target.getText());

        // Step 2
        // Replace avatar icons with real avatars
        final MentionSpan[] list = messageBuilder.getSpans(0, messageBuilder.length(), MentionSpan.class);
        for (MentionSpan span : list) {
            final int spanStart = messageBuilder.getSpanStart(span);
            final int spanEnd = messageBuilder.getSpanEnd(span);
            Glide.with(context)
                    .asBitmap()
                    .placeholder(R.drawable.ic_person_grey600_24dp)
                    .load(account.url + "/index.php/avatar/" + messageBuilder.subSequence(spanStart + 1, spanEnd).toString() + "/" + span.getDrawable().getIntrinsicHeight())
                    .apply(RequestOptions.circleCropTransform())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            messageBuilder.removeSpan(span);
                            messageBuilder.setSpan(new MentionSpan(context, resource), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // silence is gold
                        }
                    });
        }
        target.setText(messageBuilder);
    }

    private static SpannableStringBuilder replaceAtMentionsWithImagePlaceholderAndDisplayName(@NonNull Context context, @NonNull Map<String, String> mentions, @NonNull CharSequence text) {
        final SpannableStringBuilder messageBuilder = new SpannableStringBuilder(text);
        for (String userId : mentions.keySet()) {
            final String mentionId = "@" + userId;
            final String mentionDisplayName = " " + mentions.get(userId);
            int index = messageBuilder.toString().lastIndexOf(mentionId);
            while (index >= 0) {
                messageBuilder.setSpan(new MentionSpan(context, R.drawable.ic_person_grey600_24dp), index, index + mentionId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageBuilder.insert(index + mentionId.length(), mentionDisplayName);
                index = messageBuilder.toString().substring(0, index).lastIndexOf(mentionId);
            }
        }
        return messageBuilder;
    }

    private static class MentionSpan extends ImageSpan {
        private MentionSpan(@NonNull Context context, int resourceId) {
            super(context, resourceId);
        }

        private MentionSpan(@NonNull Context context, @NonNull Bitmap bitmap) {
            super(context, bitmap);
        }
    }
}
