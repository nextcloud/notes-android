/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public abstract class BrandedFragment extends Fragment implements Branded {

    @ColorInt
    protected int colorAccent;
    @ColorInt
    protected int colorPrimary;

    @Override
    public void onStart() {
        super.onStart();

        final var context = requireContext();
        final var typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        colorPrimary = typedValue.data;

        @ColorInt final int color = BrandingUtil.readBrandMainColor(context);
        applyBrand(color);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final var utils = BrandingUtil.of(colorAccent, requireContext());

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getIcon() != null) {
                utils.platform.colorToolbarMenuIcon(requireContext(), menu.getItem(i));
            }
        }
    }
}
