/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding

import androidx.annotation.ColorInt
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BrandedBottomSheetDialogFragment(contentLayoutId: Int) :
    BottomSheetDialogFragment(contentLayoutId), Branded {

    override fun onStart() {
        super.onStart()

        @ColorInt val color = BrandingUtil.readBrandMainColor(requireContext())
        applyBrand(color)
    }
}
