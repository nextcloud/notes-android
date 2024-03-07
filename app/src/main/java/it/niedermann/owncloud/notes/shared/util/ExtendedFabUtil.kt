/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

object ExtendedFabUtil {
    @JvmStatic
    fun setExtendedFabVisibility(
        extendedFab: ExtendedFloatingActionButton,
        visibility: Boolean,
    ) {
        if (visibility) {
            extendedFab.show()
        } else {
            if (extendedFab.isExtended) {
                extendedFab.hide()
            } else {
                if (extendedFab.animation == null) {
                    val animation = AnimationUtils.loadAnimation(
                        extendedFab.context,
                        R.anim.abc_shrink_fade_out_from_bottom,
                    )
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            extendedFab.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    extendedFab.startAnimation(animation)
                }
            }
        }
    }

    @JvmStatic
    fun toggleExtendedOnLongClick(extendedFab: ExtendedFloatingActionButton) {
        extendedFab.setOnLongClickListener { v: View? ->
            if (extendedFab.isExtended) {
                extendedFab.shrink()
            } else {
                extendedFab.extend()
            }
            true
        }
    }

    @JvmStatic
    fun toggleVisibilityOnScroll(
        extendedFab: ExtendedFloatingActionButton,
        scrollY: Int,
        oldScrollY: Int,
    ) {
        @Suppress("ConvertTwoComparisonsToRangeCheck")
        if (oldScrollY > 0 && scrollY > oldScrollY && extendedFab.isShown) {
            setExtendedFabVisibility(extendedFab, false)
        } else if (scrollY < oldScrollY && !extendedFab.isShown) {
            setExtendedFabVisibility(extendedFab, true)
        }
    }
}
