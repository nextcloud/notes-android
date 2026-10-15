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
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.databinding.ActivityListItemBinding

class ActivityListAdapter (
    private val context: Context,
    private val listener: OnActivityClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = arrayListOf<Activity>()

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

    fun addItems(it: List<Activity>) {
        val prevItems = itemCount
        this.items.addAll(it)
        notifyItemRangeInserted(prevItems, it.size)
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

        fun bind(activity: Activity, listener: OnActivityClickListener) {
            bindDateTime(activity)
            bindSubject(activity)
            bindMessage(activity)
            bindIcon(activity)
            binding.root.setOnClickListener { listener.onItemClick(activity) }
        }

        private fun bindDateTime(activity: Activity) {
            binding.datetime.apply {
                visibility = View.VISIBLE
                text = DateFormat.format(TIME_PATTERN, activity.datetime.time)
            }
        }

        private fun bindSubject(activity: Activity) {
            binding.subject.text = activity.subject
        }

        private fun bindMessage(activity: Activity) {
            binding.message.apply {
                when {
                    activity.message.isNullOrEmpty() -> visibility = View.GONE
                    else -> {
                        visibility = View.VISIBLE
                        text = activity.message
                    }
                }
            }
        }

        private fun bindIcon(activity: Activity) {
            if (activity.icon.isNullOrEmpty()) {
                return
            }

            Glide
                .with(binding.icon.context)
                .load(activity.icon.toUri())
                .placeholder( R.drawable.ic_activity)
                .error(R.drawable.ic_activity)
                .into(binding.icon)

            /*
            if (activity.icon.endsWith(COLORED_ICON_SUFFIX, ignoreCase = true)) {
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

}