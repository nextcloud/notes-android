package it.niedermann.owncloud.notes.util.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.owncloud.android.lib.resources.users.PredefinedStatus

object UserStorage {
    private const val APP = "NextcloudNotes"
    private const val PREDEFINED_STATUS = "PREDEFINED_STATUS"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP, Context.MODE_PRIVATE)
    }

    fun readPredefinedStatus(context: Context): ArrayList<PredefinedStatus> {
        val json = getSharedPreferences(context).getString(PREDEFINED_STATUS, null)
        if (json == null) {
            return arrayListOf()
        }

        val type = object : TypeToken<ArrayList<PredefinedStatus>>() {}.type
        return Gson().fromJson(json, type)
    }
}
