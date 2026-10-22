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

    fun fetchNextPage() {
        // Versions are non-paginated: fetch only once
        val fetchVersions = data.isEmpty()
        val fetchActivities = data.activitiesMore

        job?.cancel()
        job = lifecycleScope.launch {
            val (activitiesResult, versionsResult) = withContext(Dispatchers.IO) {
                val activities = async {
                    if (fetchActivities) activitiesRepo.fetchActivities(noteId, data.activitiesLastGiven)
                    else ActivitiesResult.Success(listOf(), data.activitiesLastGiven)
                }
                val versions = async {
                    if (fetchVersions) versionsRepo.fetchVersions(noteId)
                    else VersionsResult.Success(listOf())
                }
                activities.await() to versions.await()
            }

            when {
                activitiesResult is ActivitiesResult.Success && versionsResult is VersionsResult.Success -> {
                    if (activitiesResult.lastGiven != null && activitiesResult.lastGiven == data.activitiesLastGiven)
                        data.activitiesMore = false

                    data.activitiesLastGiven = activitiesResult.lastGiven ?: data.activitiesLastGiven
                    data.activities.addAll(activitiesResult.activities)
                    data.versions.addAll(versionsResult.versions)
                    listener.onPageFetched()
                    return@launch
                }

                activitiesResult is ActivitiesResult.Failed -> Log.e(TAG, "Fetching activities failed")

                versionsResult is VersionsResult.Failed -> Log.e(TAG, "Fetching versions failed")
            }

            listener.onPageFetchError()
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