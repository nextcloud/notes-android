package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;

import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import it.niedermann.android.markdown.R;
import it.niedermann.android.markdown.markwon.glide.DownsampleWithMaxWidth;

/**
 * <ul>
 *      <li>Applies downscaling via {@link DownsampleWithMaxWidth} to avoid <a href="https://github.com/stefan-niedermann/nextcloud-notes/issues/1034">issues with large images</a></li>
 *      <li>Adds a placeholder while loading an image</li>
 *      <li>Adds a "broken image" placeholder in case of an error</li>
 *  </ul>
 */
public class CustomGlideStore implements GlideImagesPlugin.GlideStore {
    private final RequestManager requestManager;
    private final DownsampleWithMaxWidth downsampleWithMaxWidth;

    public CustomGlideStore(@NonNull Context context) {
        this.requestManager = Glide.with(context);
        downsampleWithMaxWidth = new DownsampleWithMaxWidth(context.getResources().getDisplayMetrics().widthPixels);
    }

    @NonNull
    @Override
    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
        return requestManager
                .load(drawable.getDestination())
                .downsample(downsampleWithMaxWidth)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_broken_image_24);
    }

    @Override
    public void cancel(@NonNull Target<?> target) {
        requestManager.clear(target);
    }
}
