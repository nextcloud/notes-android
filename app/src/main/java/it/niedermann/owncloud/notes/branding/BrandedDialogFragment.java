/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public abstract class BrandedDialogFragment extends DialogFragment implements Branded {

    @Override
    public void onStart() {
        super.onStart();

        @Nullable final var context = requireContext();
        @ColorInt final int color = BrandingUtil.readBrandMainColor(context);
        applyBrand(color);
    }
}
