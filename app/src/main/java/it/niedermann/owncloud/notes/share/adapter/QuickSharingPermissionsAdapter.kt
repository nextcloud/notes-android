/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import it.niedermann.owncloud.notes.branding.Branded
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ItemQuickSharePermissionsBinding
import it.niedermann.owncloud.notes.share.model.QuickPermissionModel

class QuickSharingPermissionsAdapter(
    private val quickPermissionList: MutableList<QuickPermissionModel>,
    private val onPermissionChangeListener: QuickSharingPermissionViewHolder.OnPermissionChangeListener,
    private var color: Int = 0
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Branded {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemQuickSharePermissionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        BrandingUtil.of(color, parent.context).platform.colorImageView(binding.tvQuickShareCheckIcon, ColorRole.PRIMARY)
        return QuickSharingPermissionViewHolder(
            binding,
            binding.root,
            onPermissionChangeListener,
            BrandingUtil.of(color, binding.root.context)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is QuickSharingPermissionViewHolder) {
            holder.bindData(quickPermissionList[position])
        }
    }

    override fun getItemCount(): Int {
        return quickPermissionList.size
    }

    override fun applyBrand(color: Int) {
        this.color = color
        notifyDataSetChanged()
    }

    class QuickSharingPermissionViewHolder(
        val binding: ItemQuickSharePermissionsBinding,
        itemView: View,
        val onPermissionChangeListener: OnPermissionChangeListener,
        val util: BrandingUtil,
    ) :
        RecyclerView
        .ViewHolder(itemView) {

        fun bindData(quickPermissionModel: QuickPermissionModel) {
            binding.tvQuickShareName.text = quickPermissionModel.permissionName
            if (quickPermissionModel.isSelected) {
                binding.tvQuickShareCheckIcon.visibility = View.VISIBLE
            } else {
                binding.tvQuickShareCheckIcon.visibility = View.INVISIBLE
            }

            itemView.setOnClickListener {
                // if user select different options then only update the permission
                if (!quickPermissionModel.isSelected) {
                    onPermissionChangeListener.onPermissionChanged(adapterPosition)
                } else {
                    // dismiss sheet on selection of same permission
                    onPermissionChangeListener.onDismissSheet()
                }
            }
        }

        interface OnPermissionChangeListener {
            fun onPermissionChanged(position: Int)
            fun onDismissSheet()
        }
    }
}
