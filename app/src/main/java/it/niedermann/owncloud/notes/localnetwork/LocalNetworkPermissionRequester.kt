/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.localnetwork

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedSnackbar
import kotlinx.coroutines.launch

class LocalNetworkPermissionRequester(private val activity: AppCompatActivity) {
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission(), ::onPermissionResult)

    fun requestIfNeeded(serverUrl: String) {
        if (!LocalNetworkPermission.isRequired(activity)) {
            return
        }

        requestForLocalServer(serverUrl)
    }

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    private fun requestForLocalServer(serverUrl: String) {
        if (LocalNetworkPermission.isGranted(activity)) {
            return
        }

        activity.lifecycleScope.launch {
            if (LocalNetworkAddress.isLocalServer(serverUrl)) {
                launchRequest()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    private fun launchRequest() {
        permissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
    }

    private fun onPermissionResult(isGranted: Boolean) {
        if (isGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) {
            return
        }

        showDeniedSnackbar()
    }

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    private fun showDeniedSnackbar() {
        val canShowSystemDialog =
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_LOCAL_NETWORK)
        val message = if (canShowSystemDialog) {
            R.string.local_network_permission_denied
        } else {
            R.string.local_network_permission_settings_path
        }
        val snackbar = BrandedSnackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)

        if (canShowSystemDialog) {
            snackbar.setAction(R.string.local_network_permission_allow) { launchRequest() }
        } else {
            snackbar.setAction(R.string.action_settings) { openAppSettings() }
        }

        snackbar.show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts(PACKAGE_SCHEME, activity.packageName, null)
        )
        activity.startActivity(intent)
    }

    companion object {
        private const val PACKAGE_SCHEME = "package"
    }
}
