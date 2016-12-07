package it.niedermann.owncloud.notes.util;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;

import com.yydcdut.rxmarkdown.RxMDConfiguration;

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
    public static RxMDConfiguration getMarkDownConfiguration(Context context) {
        return new RxMDConfiguration.Builder(context)
                .setLinkColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, null))
                .build();
    }
}
