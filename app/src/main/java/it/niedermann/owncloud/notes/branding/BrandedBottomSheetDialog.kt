/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding

import android.content.Context
import androidx.annotation.ColorInt
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class BrandedBottomSheetDialog(context: Context) : BottomSheetDialog(context), Branded {

    override fun onStart() {
        super.onStart()

        @ColorInt val color = BrandingUtil.readBrandMainColor(context)
        applyBrand(color)
    }
}
