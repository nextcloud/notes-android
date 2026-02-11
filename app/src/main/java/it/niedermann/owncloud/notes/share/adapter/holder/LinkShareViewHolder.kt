/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter.holder

import android.text.TextUtils
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedViewHolder
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ItemShareLinkShareBinding
import it.niedermann.owncloud.notes.share.helper.SharePermissionManager
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener
import it.niedermann.owncloud.notes.share.model.QuickPermissionType
import it.niedermann.owncloud.notes.util.remainingDownloadLimit

/**
 * ViewHolder responsible for displaying public and email link shares
 * inside the share list.
 */
class LinkShareViewHolder(
    private val binding: ItemShareLinkShareBinding
) : BrandedViewHolder(binding.root) {

    init {
        bindBranding()
    }

    fun bind(publicShare: OCShare, listener: ShareeListAdapterListener, position: Int) {
        val quickPermissionType = SharePermissionManager.getSelectedType(publicShare)

        setName(binding, publicShare, position)
        setSubline(binding, publicShare)
        setPermissionName(binding, quickPermissionType)
        setOnClickListeners(binding, listener, publicShare)
        configureCopyLink(binding, listener, publicShare)
    }

    @Suppress("ReturnCount")
    private fun setName(
        binding: ItemShareLinkShareBinding?,
        publicShare: OCShare,
        position: Int
    ) {
        val context = binding?.root?.context

        if (binding == null || context == null) {
            return
        }

        if (ShareType.PUBLIC_LINK == publicShare.shareType) {
            val label = publicShare.label
            binding.name.text = when {
                label.isNullOrBlank() && position == 0 ->
                    context.getString(R.string.share_link)

                label.isNullOrBlank() ->
                    context.getString(R.string.share_link_with_label, position.toString())

                else ->
                    context.getString(R.string.share_link_with_label, label)
            }
            return
        }

        if (ShareType.EMAIL == publicShare.shareType) {
            binding.name.text = publicShare.sharedWithDisplayName

            val emailDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_email, null)
            binding.icon.setImageDrawable(emailDrawable)
            binding.copyLink.visibility = View.GONE
            return
        }

        val label = publicShare.label
        if (!label.isNullOrEmpty()) {
            binding.name.text = context.getString(R.string.share_link_with_label, label)
        }
    }

    private fun setSubline(binding: ItemShareLinkShareBinding?, publicShare: OCShare) {
        val context = binding?.root?.context
        if (binding == null || context == null) {
            return
        }

        val downloadLimit = publicShare.fileDownloadLimit
        if (downloadLimit != null) {
            val remaining = publicShare.remainingDownloadLimit() ?: return
            val text = context.resources.getQuantityString(
                R.plurals.share_download_limit_description,
                remaining,
                remaining
            )

            binding.subline.text = text
            binding.subline.visibility = View.VISIBLE
            return
        }

        binding.subline.visibility = View.GONE
    }

    private fun setPermissionName(
        binding: ItemShareLinkShareBinding?,
        quickPermissionType: QuickPermissionType
    ) {
        val context = binding?.root?.context

        if (binding == null || context == null) {
            return
        }

        val permissionName = quickPermissionType.getText(context)

        if (TextUtils.isEmpty(permissionName)) {
            binding.permissionName.visibility = View.GONE
            return
        }

        binding.permissionName.text = permissionName
        binding.permissionName.visibility = View.VISIBLE
    }

    private fun setOnClickListeners(
        binding: ItemShareLinkShareBinding?,
        listener: ShareeListAdapterListener,
        publicShare: OCShare
    ) {
        if (binding == null) {
            return
        }

        binding.overflowMenu.setOnClickListener {
            listener.showSharingMenuActionSheet(publicShare)
        }
        binding.shareByLinkContainer.setOnClickListener {
            listener.showPermissionsDialog(publicShare)
        }
    }

    private fun configureCopyLink(
        binding: ItemShareLinkShareBinding?,
        listener: ShareeListAdapterListener,
        publicShare: OCShare
    ) {
        val context = binding?.root?.context

        if (binding == null || context == null) {
            return
        }

        binding.copyLink.setOnClickListener { listener.copyLink(publicShare) }
    }

    override fun applyBrand(color: Int) {
        val brandingUtil = BrandingUtil.of(color, binding.root.context)
        brandingUtil.androidx.colorPrimaryTextViewElement(binding.permissionName)
        brandingUtil.platform.colorTextView(binding.label, ColorRole.ON_SURFACE)
        brandingUtil.platform.colorImageViewBackgroundAndIcon(binding.icon)
        brandingUtil.platform.colorImageView(binding.expirationStatus, ColorRole.ON_PRIMARY_CONTAINER)
    }
}
