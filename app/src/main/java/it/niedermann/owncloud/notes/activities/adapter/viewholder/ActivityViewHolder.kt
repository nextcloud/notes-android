package it.niedermann.owncloud.notes.activities.adapter.viewholder

import android.view.View
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.nextcloud.android.common.core.utils.DateFormatter
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.adapter.ActivityListItem
import it.niedermann.owncloud.notes.activities.adapter.OnActivityClickListener
import it.niedermann.owncloud.notes.branding.BrandedViewHolder
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityListItemBinding
import java.util.Calendar


class ActivityViewHolder(
    val binding: ActivityListItemBinding,
    val dateFormatter: DateFormatter,
    val calendar: Calendar
) : BrandedViewHolder(binding.root) {

    fun bind(item: ActivityListItem, listener: OnActivityClickListener) {
        bindDateTime(item)
        bindSubject(item)
        bindMessage(item)
        bindIcon(item)
        bindActionIcon(item)
        binding.root.setOnClickListener {
            when (item) {
                is ActivityListItem.ActivityItem -> listener.onActivityItemClick(item.activity)
                is ActivityListItem.FileVersionItem -> listener.onVersionItemClick(item.fileVersion)
            }
        }
    }

    private fun bindDateTime(activity: ActivityListItem) {
        binding.datetime.apply {
            visibility = View.VISIBLE
            text = dateFormatter.getConditionallyRelativeFormattedTimeSpan(
                calendar.apply {
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