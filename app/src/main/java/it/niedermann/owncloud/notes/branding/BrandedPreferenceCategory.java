package it.niedermann.owncloud.notes.branding;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

public class BrandedPreferenceCategory extends PreferenceCategory {

    public BrandedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public BrandedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BrandedPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrandedPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final var view = holder.itemView.findViewById(android.R.id.title);
        @Nullable final var context = getContext();
        if (view instanceof TextView) {
            final var util = BrandingUtil.of(BrandingUtil.readBrandMainColor(context), context);;
            ((TextView) view).setTextColor(util.notes.getOnPrimaryContainer(context));
        }
    }
}
