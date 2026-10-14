package it.niedermann.owncloud.notes.activities.adapter

import it.niedermann.owncloud.notes.activities.model.Activity

interface OnActivityClickListener {
    fun onItemClick(activity: Activity)
    fun onEndOfListReached()
}