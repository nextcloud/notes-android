/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.nextcloud.android.common.ui.theme.utils.ColorRole;

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
            final var util = BrandingUtil.of(BrandingUtil.readBrandMainColor(context), context);

            util.platform.colorTextView((TextView) view, ColorRole.ON_PRIMARY_CONTAINER);
        }
    }
}
