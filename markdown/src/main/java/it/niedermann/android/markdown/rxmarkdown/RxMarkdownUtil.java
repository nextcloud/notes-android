package it.niedermann.android.markdown.rxmarkdown;

import android.content.Context;

import androidx.annotation.RestrictTo;

import com.yydcdut.rxmarkdown.RxMDConfiguration.Builder;

/**
 * Created by stefan on 07.12.16.
 */
@Deprecated
@RestrictTo(value = RestrictTo.Scope.LIBRARY)
public class RxMarkdownUtil {

    private RxMarkdownUtil() {
    }

    /**
     * Ensures every instance of RxMD uses the same configuration
     *
     * @param context Context
     * @return RxMDConfiguration
     */
    public static Builder getMarkDownConfiguration(Context context) {
        return new Builder(context)
                .setHeader2RelativeSize(1.35f)
                .setHeader3RelativeSize(1.25f)
                .setHeader4RelativeSize(1.15f)
                .setHeader5RelativeSize(1.1f)
                .setHeader6RelativeSize(1.05f)
                .setHorizontalRulesHeight(2);
    }

    public static Builder getMarkDownConfiguration(Context context, Boolean darkTheme) {
        return new Builder(context)
                .setHeader2RelativeSize(1.35f)
                .setHeader3RelativeSize(1.25f)
                .setHeader4RelativeSize(1.15f)
                .setHeader5RelativeSize(1.1f)
                .setHeader6RelativeSize(1.05f)
                .setHorizontalRulesHeight(2);
    }
}
