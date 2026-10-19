package it.niedermann.owncloud.notes.activities.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.databinding.ActivityListItemBinding

class ActivityListAdapter (
    private val context: Context,
    private val listener: OnActivityClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = arrayListOf<ActivityListItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        ActivityViewHolder(
            ActivityListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == itemCount - 1) {
            listener.onEndOfListReached()
        }
        (holder as ActivityViewHolder).bind(items[position], listener)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun addActivities(items: List<Activity>) {
        this.items.addAll(
            items.map { ActivityListItem(context, it) }
        )
        this.items.sort()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addVersions(items: List<FileVersion>) {
        this.items.addAll(
            items.map { ActivityListItem(context, it) }
        )
        this.items.sort()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        this.items.clear()
        notifyDataSetChanged()
    }

    private class ActivityViewHolder(
        val binding: ActivityListItemBinding,
        //val brandingUtil: BrandingUtil
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActivityListItem, listener: OnActivityClickListener) {
            bindDateTime(item)
            bindSubject(item)
            bindMessage(item)
            bindIcon(item)
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
                text = DateFormat.format(TIME_PATTERN, activity.getTimestamp())
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

            /*
            if (iconUrl.endsWith(COLORED_ICON_SUFFIX, ignoreCase = true)) {
                binding.icon.imageTintList = null
            } else {
                brandingUtil.platform.colorImageView(binding.icon, ColorRole.ON_SURFACE_VARIANT)
            }
             */

        }

        companion object {
            private const val TIME_PATTERN = "HH:mm"
            private const val COLORED_ICON_SUFFIX = "-color.svg"
        }

    }


    class ActivityListItem : Comparable<ActivityListItem> {
        val context: Context
        val activity: Activity?
        val fileVersion: FileVersion?

        constructor(context: Context, activity: Activity) {
            this.context = context
            this.activity = activity
            this.fileVersion = null
        }

        constructor(context: Context, fileVersion: FileVersion) {
            this.context = context
            this.activity = null
            this.fileVersion = fileVersion
        }

        fun getTimestamp() : Long =
            when {
                activity != null -> activity.datetime.time
                fileVersion != null -> fileVersion.modifiedTimestamp
                else -> 0
            }

        fun getTitle() : String? =
            when {
                activity != null -> activity.subject
                fileVersion != null -> context.getString(R.string.new_version_was_created)
                else -> null
            }

        fun getMessage() : String? =
            when {
                activity != null -> activity.message
                fileVersion != null -> "${fileVersion.fileLength} B"
                else -> null
            }

        fun getIconUrl() : String? = activity?.icon

        override fun compareTo(other: ActivityListItem): Int =
            getTimestamp().compareTo(other.getTimestamp())
    }
}