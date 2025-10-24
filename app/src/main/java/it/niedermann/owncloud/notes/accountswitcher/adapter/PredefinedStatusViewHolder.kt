/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2020 Nextcloud GmbH
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package it.niedermann.owncloud.notes.accountswitcher.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.accountswitcher.model.ExposedPredefinedStatus
import it.niedermann.owncloud.notes.databinding.PredefinedStatusBinding
import it.niedermann.owncloud.notes.shared.util.DisplayUtils

private const val ONE_SECOND_IN_MILLIS = 1000

class PredefinedStatusViewHolder(private val binding: PredefinedStatusBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(status: ExposedPredefinedStatus, clickListener: PredefinedStatusClickListener, context: Context) {
        binding.root.setOnClickListener { clickListener.onClick(status) }
        binding.icon.text = status.icon
        binding.name.text = status.message

        if (status.exposedClearAt == null) {
            binding.clearAt.text = context.getString(R.string.dontClear)
        } else {
            val clearAt = status.exposedClearAt
            if (clearAt.type == "period") {
                binding.clearAt.text = DisplayUtils.getRelativeTimestamp(
                    context,
                    System.currentTimeMillis() + clearAt.time.toInt() * ONE_SECOND_IN_MILLIS,
                    true
                )
            } else {
                // end-of
                if (clearAt.time == "day") {
                    binding.clearAt.text = context.getString(R.string.today)
                }
            }
        }
    }
}
