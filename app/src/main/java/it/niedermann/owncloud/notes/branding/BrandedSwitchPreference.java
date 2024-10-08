/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

import it.niedermann.owncloud.notes.R;

public class BrandedSwitchPreference extends SwitchPreferenceCompat implements Branded {

    @ColorInt
    private Integer mainColor = null;

    @Nullable
    private MaterialSwitch switchView;

    public BrandedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    public BrandedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    public BrandedSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    public BrandedSwitchPreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (holder.itemView instanceof ViewGroup) {
            switchView = findSwitchWidget(holder.itemView);
            if (mainColor != null) {
                applyBrand();
            }
        }
    }

    @Override
    public void applyBrand(@ColorInt int color) {
        this.mainColor = color;
        // onBindViewHolder is called after applyBrand, therefore we have to store the given values and apply them later.
        applyBrand();
    }

    private void applyBrand() {
        if (switchView != null) {
            final var util = BrandingUtil.of(mainColor, getContext());
            util.material.colorMaterialSwitch(switchView);
        }
    }

    /**
     * Recursively go through view tree until we find an android.widget.Switch
     *
     * @param view Root view to start searching
     * @return A Switch class or null
     * @see <a href="https://gist.github.com/marchold/45e22839eb94aa14dfb5">Source</a>
     */
    private MaterialSwitch findSwitchWidget(View view) {
        if (view instanceof MaterialSwitch) {
            return (MaterialSwitch) view;
        }
        if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                final var child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup) {
                    final var result = findSwitchWidget(child);
                    if (result != null) return result;
                }
                if (child instanceof MaterialSwitch) {
                    return (MaterialSwitch) child;
                }
            }
        }
        return null;
    }
}
