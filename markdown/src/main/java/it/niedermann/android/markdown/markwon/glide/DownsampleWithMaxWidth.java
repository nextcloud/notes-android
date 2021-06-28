package it.niedermann.android.markdown.markwon.glide;

import androidx.annotation.Px;

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;

/**
 * @see <a href="https://github.com/noties/Markwon/issues/329#issuecomment-855220315">Source</a>
 */
public class DownsampleWithMaxWidth extends DownsampleStrategy {

    @Px
    private final int maxWidth;

    public DownsampleWithMaxWidth(@Px int maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth, int requestedHeight) {
        // do not scale down if fits requested dimension
        if (sourceWidth < maxWidth) {
            return 1F;
        }
        return (float) maxWidth / sourceWidth;
    }

    @Override
    public SampleSizeRounding getSampleSizeRounding(int sourceWidth, int sourceHeight, int requestedWidth, int requestedHeight) {
        // go figure
        return SampleSizeRounding.MEMORY;
    }
}