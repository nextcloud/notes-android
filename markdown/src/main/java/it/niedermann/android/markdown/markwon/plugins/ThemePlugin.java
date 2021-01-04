package it.niedermann.android.markdown.markwon.plugins;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import it.niedermann.android.markdown.R;

public class ThemePlugin extends AbstractMarkwonPlugin {

    @NonNull
    Context context;

    private ThemePlugin(@NonNull Context context) {
        this.context = context;
    }

    public static MarkwonPlugin create(@NonNull Context context) {
        return new ThemePlugin(context);
    }

    @Override
    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
        super.configureTheme(builder);
        builder
                .headingBreakHeight(0)
                .codeBlockBackgroundColor(ContextCompat.getColor(context, R.color.bg_code))
                .headingTextSizeMultipliers(new float[]{1.45f, 1.35f, 1.25f, 1.15f, 1.1f, 1.05f})
                .bulletWidth(context.getResources().getDimensionPixelSize(R.dimen.bullet_point_width));
    }
}
