/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.shared.model.Capabilities

data class UpdateShareRequest(
    @Expose
    @SerializedName("permissions")
    val permissions: Int? = null,

    @Expose
    @SerializedName("password")
    val password: String? = null,

    @Expose
    @SerializedName("note")
    val note: String? = null,

    @Expose
    @SerializedName("label")
    val label: String? = null,

    @Expose
    @SerializedName("expireDate")
    val expireDate: String? = null,

    @Expose
    @SerializedName("hideDownload")
    val hideDownload: String? = null,

    @Expose
    @SerializedName("attributes")
    val attributes: String? = null
) {
    companion object {
        fun createAttributes(capabilities: Capabilities, allowDownloadAndSync: Boolean, type: ShareType): String {
            if (type != ShareType.INTERNAL && type != ShareType.USER) {
                return "[]"
            }

            val shouldUseShareAttributesV2 = (capabilities.nextcloudMajorVersion?.toInt() ?: 0) >= 30

            val shareAttributes = arrayOf(
                if (shouldUseShareAttributesV2) {
                    ShareAttributesV2(
                        scope = "permissions",
                        key = "download",
                        value = allowDownloadAndSync
                    )
                } else {
                    ShareAttributesV1(
                        scope = "permissions",
                        key = "download",
                        enabled = allowDownloadAndSync
                    )
                }
            )

            return Gson().toJson(shareAttributes)
        }
    }
}

data class ShareAttributesV2(
    var scope: String,
    var key: String,
    var value: Boolean
) {
    companion object {
        fun getAttributes(json: String): List<ShareAttributesV2> {
            val type = object : TypeToken<List<ShareAttributesV2>>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}

data class ShareAttributesV1(
    var scope: String,
    var key: String,
    var enabled: Boolean
) {
    companion object {
        fun getAttributes(json: String): List<ShareAttributesV1> {
            val typeV1 = object : TypeToken<List<ShareAttributesV1>>() {}.type
            return Gson().fromJson(json, typeV1)
        }
    }
}
