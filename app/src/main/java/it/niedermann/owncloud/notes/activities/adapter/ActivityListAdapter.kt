/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nextcloud.android.common.core.utils.DateFormatter
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.branding.Branded
import it.niedermann.owncloud.notes.branding.BrandedViewHolder
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityListItemBinding
import java.util.Calendar

class ActivityListAdapter (
    private val context: Context,
    private val listener: OnActivityClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Branded {

    val items = arrayListOf<ActivityListItem>()
    var lastActivity: Activity? = null
    private var brandColor: Int? = null
    private val dateFormatter = DateFormatter(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        ActivityViewHolder(
            ActivityListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            dateFormatter
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Apply brand
        val holder = (holder as ActivityViewHolder)
        brandColor?.apply { holder.applyBrand(this) }

        // Retrieve item and bind vh
        val item = items[position]
        if (lastActivity != null && item.activity == lastActivity) {
            listener.lastActivityReached()
        }
        holder.bind(item, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun applyBrand(color: Int) {
        brandColor = color
    }

    private class ActivityViewHolder(
        val binding: ActivityListItemBinding,
        val dateFormatter: DateFormatter
    ) : BrandedViewHolder(binding.root) {

        fun bind(item: ActivityListItem, listener: OnActivityClickListener) {
            bindDateTime(item)
            bindSubject(item)
            bindMessage(item)
            bindIcon(item)
            bindActionIcon(item)
            binding.root.setOnClickListener {
                when {
                    item.activity != null -> listener.onActivityItemClick(item.activity)
                    item.fileVersion != null -> listener.onVersionItemClick(item.fileVersion)
                }
            }
        }

        private fun bindDateTime(activity: ActivityListItem) {
            binding.datetime.apply {
                visibility = View.VISIBLE
                text = dateFormatter.getConditionallyRelativeFormattedTimeSpan(
                    Calendar.getInstance().apply {
                        timeInMillis = activity.getTimestamp()
                    }
                )
            }
        }

        private fun bindSubject(activity: ActivityListItem) {
            binding.subject.text = activity.getTitle()
        }

        private fun bindMessage(activity: ActivityListItem) {
            binding.message.apply {
                when {
                    activity.getMessage().isNullOrEmpty() -> visibility = View.GONE
                    else -> {
                        visibility = View.VISIBLE
                        text = activity.getMessage()
                    }
                }
            }
        }

        private fun bindIcon(activity: ActivityListItem) {
            val iconUrl = activity.getIconUrl()
            if (iconUrl.isNullOrEmpty()) {
                binding.icon.setImageResource(R.drawable.ic_activity)
                return
            }

            Glide
                .with(binding.icon.context)
                .load(iconUrl.toUri())
                .placeholder( R.drawable.ic_activity)
                .error(R.drawable.ic_activity)
                .into(binding.icon)
        }

        private fun bindActionIcon(activity: ActivityListItem) {
            binding.actionIcon.visibility =
                if (activity.canBeRestored()) View.VISIBLE
                else View.GONE
        }

        override fun applyBrand(color: Int) {
            val util = BrandingUtil.of(color, binding.root.context)
            util.platform.colorViewBackground(binding.root)
            util.platform.colorImageView(binding.icon, ColorRole.ON_SURFACE_VARIANT)
        }

    }

}