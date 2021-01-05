package it.niedermann.android.markdown.markwon.span;

import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.function.Function;

public class InterceptedURLSpan extends URLSpan {

    private static final String TAG = InterceptedURLSpan.class.getSimpleName();
    @NonNull
    private final Collection<Function<String, Boolean>> onLinkClickCallbacks;

    public InterceptedURLSpan(@NonNull Collection<Function<String, Boolean>> onLinkClickCallbacks, String url) {
        super(url);
        this.onLinkClickCallbacks = onLinkClickCallbacks;
    }

    @Override
    public void onClick(View widget) {
        if (onLinkClickCallbacks.size() > 0) {
            new Thread(() -> {
                for (Function<String, Boolean> callback : onLinkClickCallbacks) {
                    try {
                        if (callback.apply(getURL())) {
                            return;
                        }
                    } catch (Throwable t) {
                        Log.w(TAG, t.getMessage(), t);
                    }
                }
                super.onClick(widget);
            }).start();
        } else {
            super.onClick(widget);
        }
    }
}