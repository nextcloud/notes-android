package it.niedermann.owncloud.notes.activities.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityListItemBinding

class ActivityListAdapter (
    protected val context: Context,
    private val listener: OnActivityClickListener,
    //private val brandingUtil: BrandingUtil
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        (holder as ActivityViewHolder).bind(items[position], listener)

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<Activity>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    private class ActivityViewHolder(val binding: ActivityListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: Activity, listener: OnActivityClickListener) {
            bindDateTime(activity)
            bindSubject(activity)
            bindMessage(activity)
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

        companion object {
            private const val TIME_PATTERN = "HH:mm"

        }

    }

}