/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nextcloud.android.common.ui.util.extensions.adjustUIForAPILevel35
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BrandedFragment : Fragment(), Branded {
    @JvmField
    @ColorInt
    protected var colorAccent: Int = 0

    @JvmField
    @ColorInt
    protected var colorPrimary: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        if (activity is AppCompatActivity) {
            val appCompatActivity = activity as AppCompatActivity
            appCompatActivity.adjustUIForAPILevel35()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        val context = requireContext()
        val typedValue = TypedValue()

        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorAccent,
            typedValue,
            true
        )
        colorAccent = typedValue.data

        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary,
            typedValue,
            true
        )
        colorPrimary = typedValue.data

        @ColorInt
        val color = BrandingUtil.readBrandMainColor(context)
        applyBrand(color)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val utils = BrandingUtil.of(colorAccent, requireContext())

        menu.forEach { menu ->
            menu.icon?.let { icon ->
                utils.platform.colorToolbarMenuIcon(requireContext(), menu)
            }
        }
    }

    fun lifecycleScopeIOJob(block: () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            block()
        }
    }

    fun onMainThread(block: () -> Unit) {
        activity?.runOnUiThread {
            block()
        }
    }
}
