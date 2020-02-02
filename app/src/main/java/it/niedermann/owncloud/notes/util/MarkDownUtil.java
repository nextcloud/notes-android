package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.graphics.Color;
import android.text.Spanned;

import androidx.core.content.res.ResourcesCompat;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownConfiguration.Builder;
import com.yydcdut.markdown.span.MDImageSpan;
import com.yydcdut.markdown.theme.ThemeDefault;
import com.yydcdut.markdown.theme.ThemeSonsOfObsidian;

import it.niedermann.owncloud.notes.R;

/**
 * Created by stefan on 07.12.16.
 */

public class MarkDownUtil {

    /**
     * Ensures every instance of RxMD uses the same configuration
     *
     * @param context Context
     * @return RxMDConfiguration
     */
    public static Builder getMarkDownConfiguration(Context context) {
        return getMarkDownConfiguration(context, Notes.getAppTheme(context));
    }

    public static Builder getMarkDownConfiguration(Context context, Boolean darkTheme) {
        return new MarkdownConfiguration.Builder(context)
                .setUnOrderListColor(ResourcesCompat.getColor(context.getResources(),
                        darkTheme ? R.color.widget_fg_dark_theme : R.color.widget_fg_default, null))
                .setHeader2RelativeSize(1.35f)
                .setHeader3RelativeSize(1.25f)
                .setHeader4RelativeSize(1.15f)
                .setHeader5RelativeSize(1.1f)
                .setHeader6RelativeSize(1.05f)
                .setHorizontalRulesHeight(2)
                .setCodeBgColor(darkTheme ? ResourcesCompat.getColor(context.getResources(), R.color.fg_default_high, null) : Color.LTGRAY)
                .setTheme(darkTheme ? new ThemeSonsOfObsidian() : new ThemeDefault())
                .setTodoColor(ResourcesCompat.getColor(context.getResources(),
                        darkTheme ? R.color.widget_fg_dark_theme : R.color.widget_fg_default, null))
                .setTodoDoneColor(ResourcesCompat.getColor(context.getResources(),
                        darkTheme ? R.color.widget_fg_dark_theme : R.color.widget_fg_default, null))
                .setLinkFontColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null))
                .setRxMDImageLoader(new NotesImageLoader(context))
                .setDefaultImageSize(400, 300);
    }

    public static boolean containsImageSpan(CharSequence text) {
        return ((Spanned) text).getSpans(0, text.length(), MDImageSpan.class).length > 0;
    }
}

