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
import it.niedermann.owncloud.notes.activities.adapter.model.Activity
import it.niedermann.owncloud.notes.activities.adapter.viewholder.ActivityViewHolder
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

    val calendar: Calendar = Calendar.getInstance()

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
            dateFormatter,
            calendar
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Apply brand
        val holder = (holder as ActivityViewHolder)
        brandColor?.apply { holder.applyBrand(this) }

        // Retrieve item and bind vh
        val item = items[position]
        if (lastActivity != null && (item as? ActivityListItem.ActivityItem)?.activity == lastActivity) {
            listener.lastActivityReached()
        }
        holder.bind(item, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun applyBrand(color: Int) {
        brandColor = color
    }

}