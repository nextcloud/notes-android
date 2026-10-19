package it.niedermann.owncloud.notes.activities.adapter

import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.activities.model.Activity

interface OnActivityClickListener {
    fun onActivityItemClick(activity: Activity)
    fun onVersionItemClick(version: FileVersion)
    fun lastActivityReached()
}