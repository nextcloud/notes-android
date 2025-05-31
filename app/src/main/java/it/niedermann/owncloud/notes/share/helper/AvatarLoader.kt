/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.helper

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.persistence.entity.Account

object AvatarLoader {

    fun load(context: Context, imageView: ImageView, account: Account) {
        load(context, imageView, SingleSignOnUrl(account.accountName, account.avatarUrl))
    }

    fun load(context: Context, imageView: ImageView, account: Account, username: String) {
        load(context, imageView, SingleSignOnUrl(account.accountName, getUrl(account.url, username)))
    }

    fun load(context: Context, imageView: ImageView, ssoURL: Any) {
        Glide
            .with(context)
            .load(ssoURL)
            .placeholder(R.drawable.ic_account_circle_grey_24dp)
            .error(R.drawable.ic_account_circle_grey_24dp)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }

    private fun getUrl(url: String, username: String): String =
        url + "/index.php/avatar/" + Uri.encode(username) + "/64"
}
