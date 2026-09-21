/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object NoteImagePreviewLoader {
    private val notesPaths = ConcurrentHashMap<String, String>()
    private val executor = Executors.newSingleThreadExecutor()

    @JvmStatic
    fun load(context: Context, content: String, imageView: ImageView): Boolean {
        val attachmentPath = imageReference(content) ?: run {
            clear(imageView)
            return false
        }
        val account = SingleAccountHelper.getCurrentSingleSignOnAccount(context) ?: run {
            clear(imageView)
            return false
        }
        val requestKey = "${account.name}:$attachmentPath"
        imageView.tag = requestKey
        imageView.visibility = View.GONE
        Glide.with(imageView).clear(imageView)

        executor.execute {
            val notesPath = notesPaths.getOrPut(account.name) {
                NoteImageHelper.getNotesPath(context.applicationContext, account, null)
            }
            val imageUrl = webDavUrl(account, notesPath, attachmentPath)
            imageView.post {
                if (imageView.tag != requestKey) return@post
                imageView.visibility = View.VISIBLE
                Glide.with(imageView)
                    .load(SingleSignOnUrl(account.name, imageUrl))
                    .centerCrop()
                    .into(imageView)
            }
        }
        return true
    }

    @JvmStatic
    fun hasImagePreview(content: String): Boolean = imageReference(content) != null

    private fun imageReference(content: String): String? {
        val pathStart = content.indexOf(ATTACHMENTS_PATH)
        if (pathStart < 0) {
            return null
        }

        var pathEnd = content.length
        for (index in pathStart until content.length) {
            if (content[index].isWhitespace() || content[index] == ')' || content[index] == '"' || content[index] == '\'') {
                pathEnd = index
                break
            }
        }
        return content.substring(pathStart, pathEnd)
            .takeIf { it.length > ATTACHMENTS_PATH.length }
    }

    private fun clear(imageView: ImageView) {
        imageView.tag = null
        imageView.visibility = View.GONE
        Glide.with(imageView).clear(imageView)
    }

    private fun webDavUrl(account: SingleSignOnAccount, notesPath: String, attachmentPath: String): String =
        Uri.parse(account.url).buildUpon()
            .appendPath(REMOTE_PATH)
            .appendPath(WEBDAV_PATH)
            .appendPathSegments(notesPath)
            .appendPathSegments(attachmentPath)
            .build()
            .toString()

    private fun Uri.Builder.appendPathSegments(path: String): Uri.Builder =
        apply {
            Uri.parse(path).pathSegments
                .filter { it.isNotEmpty() && it != CURRENT_DIRECTORY && it != PARENT_DIRECTORY }
                .forEach(::appendPath)
        }

    private const val ATTACHMENTS_PATH = "attachments/"
    private const val REMOTE_PATH = "remote.php"
    private const val WEBDAV_PATH = "webdav"
    private const val CURRENT_DIRECTORY = "."
    private const val PARENT_DIRECTORY = ".."
}
