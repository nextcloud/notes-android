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

/**
 * An abstract base [Fragment] implementation that provides common branding support for UI
 * components.
 *
 * This class reads and applies brand-specific colors (`colorPrimary`, `colorAccent`, etc.) when the
 * fragment starts, and adjusts UI elements such as toolbar menu icons accordingly.
 *
 * Subclasses can extend this to inherit branding behavior while implementing their specific logic.
 *
 * @see BrandingUtil for brand color resolution and application.
 * @see Branded for the interface definition related to branding behavior.
 */
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

    /**
     * Launches the given [block] of code in the [Dispatchers.IO] context using the [lifecycleScope].
     *
     * This is useful for running long-running or blocking operations (e.g., file or network I/O)
     * that should not block the main thread. The coroutine will be automatically canceled when
     * the lifecycle is destroyed.
     *
     * @param block The code block to be executed on the IO dispatcher.
     */
    fun lifecycleScopeIOJob(block: () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            block()
        }
    }

    /**
     * Executes the given [block] on the main (UI) thread.
     *
     * This is typically used to perform UI-related tasks such as updating views from a background
     * thread. Requires [activity] to be non-null; otherwise, the block will not be executed.
     *
     * @param block The code block to be executed on the main thread.
     */
    fun onMainThread(block: () -> Unit) {
        activity?.runOnUiThread {
            block()
        }
    }
}
