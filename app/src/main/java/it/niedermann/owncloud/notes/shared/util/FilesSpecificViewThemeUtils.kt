/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.material.card.MaterialCardView
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.util.PlatformThemeUtil
import com.nextcloud.android.common.ui.util.extensions.toColorScheme
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.ShareType
import dynamiccolor.DynamicScheme
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandingUtil

object FilesSpecificViewThemeUtils {

    const val TAG = "FilesSpecificViewThemeUtils"

    private object AvatarPadding {
        @Px
        const val SMALL = 4

        @Px
        const val LARGE = 8
    }

    private fun getSchemeInternal(context: Context, color: Int): DynamicScheme {
        val scheme = MaterialSchemes.Companion.fromColor(color)
        return when {
            PlatformThemeUtil.isDarkMode(context) -> scheme.darkScheme
            else -> scheme.lightScheme
        }
    }

    private fun <R> withScheme(
        view: View,
        color: Int,
        block: (DynamicScheme) -> R
    ): R = block(getSchemeInternal(view.context, color))

    fun themeStatusCardView(cardView: MaterialCardView, color: Int) {
        withScheme(cardView, color) { scheme ->
            cardView.backgroundTintList =
                ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        scheme.surfaceContainerHighest,
                        scheme.surface
                    )
                )
            cardView.setStrokeColor(
                ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        scheme.onSecondaryContainer,
                        scheme.outlineVariant
                    )
                )
            )
        }
    }

    fun createAvatar(type: ShareType?, avatar: ImageView, context: Context) {
        fun createAvatarBase(@DrawableRes icon: Int, padding: Int = AvatarPadding.SMALL) {
            avatar.setImageResource(icon)
            avatar.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.round_bgnd,
                null
            )
            avatar.cropToPadding = true
            avatar.setPadding(padding, padding, padding, padding)
        }

        val androidViewThemeUtils = BrandingUtil.getInstance(context).platform

        when (type) {
            ShareType.GROUP -> {
                createAvatarBase(R.drawable.ic_group)
                androidViewThemeUtils.colorImageViewBackgroundAndIcon(avatar)
            }
            ShareType.ROOM -> {
                createAvatarBase(R.drawable.first_run_talk, AvatarPadding.LARGE)
                androidViewThemeUtils.colorImageViewBackgroundAndIcon(avatar)
            }
            ShareType.CIRCLE -> {
                createAvatarBase(R.drawable.ic_circles)

                val backgroundColor = ContextCompat.getColor(context, R.color.nc_grey)
                avatar.background.colorFilter =
                    BlendModeColorFilterCompat
                        .createBlendModeColorFilterCompat(backgroundColor, BlendModeCompat.SRC_IN)

                val foregroundColor = ContextCompat.getColor(context, R.color.icon_on_nc_grey)
                avatar.drawable.mutate().colorFilter =
                    BlendModeColorFilterCompat
                        .createBlendModeColorFilterCompat(foregroundColor, BlendModeCompat.SRC_IN)
            }
            ShareType.EMAIL -> {
                createAvatarBase(R.drawable.ic_email, AvatarPadding.LARGE)
                androidViewThemeUtils.colorImageViewBackgroundAndIcon(avatar)
            }
            else -> Log_OC.d(TAG, "Unknown share type")
        }
    }
}
