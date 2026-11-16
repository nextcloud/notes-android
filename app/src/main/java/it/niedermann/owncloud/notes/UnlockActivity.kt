/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes

import android.app.KeyguardManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.branding.BrandedActivity

class UnlockActivity : BrandedActivity() {

    private lateinit var contentView: FrameLayout
    private lateinit var unlockButton: MaterialButton

    private val credentialLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            NotesApplication.updateLastInteraction()
            NotesApplication.unlock()
            finish()
        } else {
            moveTaskToBack(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = createContentView()
        setContentView(contentView)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_prevent_screen_capture), false)
        ) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        if (NotesApplication.isLocked()) {
            requestDeviceCredentials()
        } else {
            finish()
        }
    }

    override fun applyBrand(color: Int) {
        val brandingUtil = BrandingUtil.of(color, this)
        brandingUtil.platform.themeStatusBar(this)
        brandingUtil.platform.colorViewBackground(window.decorView)
        brandingUtil.platform.colorViewBackground(contentView)
        brandingUtil.material.colorMaterialButtonPrimaryFilled(unlockButton)
    }

    private fun createContentView(): FrameLayout = FrameLayout(this).apply {
        val padding = resources.getDimensionPixelSize(R.dimen.spacer_activity_sides)
        setPadding(padding, padding, padding, padding)

        unlockButton = MaterialButton(this@UnlockActivity).apply {
            text = getString(R.string.unlock_notes)
            setIconResource(R.drawable.ic_lock_grey600_24dp)
            setOnClickListener { requestDeviceCredentials() }
        }
        addView(
            unlockButton,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
    }

    private fun requestDeviceCredentials() {
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        if (keyguardManager?.isKeyguardSecure != true) {
            NotesApplication.updateLastInteraction()
            NotesApplication.unlock()
            finish()
            return
        }

        val intent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.unlock_notes), null)
            ?: return
        credentialLauncher.launch(intent)
    }
}