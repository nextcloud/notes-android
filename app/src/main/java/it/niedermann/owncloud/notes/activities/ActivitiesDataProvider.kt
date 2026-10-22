package it.niedermann.owncloud.notes.activities

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.nextcloud.android.sso.helper.SingleAccountHelper
import it.niedermann.owncloud.notes.activities.ActivitiesFragment.Companion.TAG
import it.niedermann.owncloud.notes.activities.adapter.ActivitiesData
import it.niedermann.owncloud.notes.activities.repository.ActivitiesRepository
import it.niedermann.owncloud.notes.activities.repository.ActivitiesResult
import it.niedermann.owncloud.notes.activities.repository.VersionsRepository
import it.niedermann.owncloud.notes.activities.repository.VersionsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivitiesDataProvider (
    val context: Context,
    val lifecycleScope: LifecycleCoroutineScope,
    val noteId: Long,
    val listener: OnPageFetchedListener
) {
    val data = ActivitiesData()
    private var job: Job? = null

    private val ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context)
    private val activitiesRepo = ActivitiesRepository(context, ssoAccount)
    private val versionsRepo = VersionsRepository(context, ssoAccount)

    fun fetchFirstPage() {
        job?.cancel()
        job = lifecycleScope.launch {
            val (activitiesResult, versionsResult) = withContext(Dispatchers.IO) {
                val activities = async {
                    activitiesRepo.fetchActivities(noteId)
                }
                val versions = async {
                    versionsRepo.fetchVersions(noteId)
                }
                activities.await() to versions.await()
            }

            if (!parseActivitiesResult(activitiesResult)) return@launch

            if (!parseVersionsResult(versionsResult)) return@launch

            listener.onPageFetched()
        }
    }

    fun fetchNextPage() {
        job?.cancel()
        job = lifecycleScope.launch {
            val activitiesResult = activitiesRepo.fetchActivities(noteId)

            if (!parseActivitiesResult(activitiesResult)) return@launch

            listener.onPageFetched()
        }
    }

    private fun parseActivitiesResult(activitiesResult: ActivitiesResult): Boolean {
        return when (activitiesResult) {
            is ActivitiesResult.Success -> {
                data.activitiesLastGiven = activitiesResult.lastGiven
                data.activitiesMore = activitiesResult.lastGiven != null
                data.activities.addAll(activitiesResult.activities)
                true
            }

            is ActivitiesResult.FailedNoCapability -> {
                Log.d(TAG, "Server has no Activity endpoint")
                data.activitiesMore = false
                true
            }

            is ActivitiesResult.Failed -> {
                Log.e(TAG, "Fetching activities failed")
                listener.onPageFetchError()
                false
            }
        }
    }

    private fun parseVersionsResult(versionsResult: VersionsResult): Boolean {
        return when (versionsResult) {
            is VersionsResult.Success -> {
                data.versions.addAll(versionsResult.versions)
                true
            }
            is VersionsResult.Failed -> {
                Log.e(TAG, "Fetching versions failed")
                false
            }
        }
    }

    fun reset() {
        data.activities.clear()
        data.versions.clear()
        data.activitiesLastGiven = null
        data.activitiesMore = true
    }

    interface OnPageFetchedListener {
        fun onPageFetched()
        fun onPageFetchError()
    }
}