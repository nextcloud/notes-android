package it.niedermann.owncloud.notes.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.adapter.ActivityListAdapter
import it.niedermann.owncloud.notes.activities.adapter.OnActivityClickListener
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.activities.repository.ActivitiesRepository
import it.niedermann.owncloud.notes.activities.repository.ActivitiesResult
import it.niedermann.owncloud.notes.activities.repository.VersionsRepository
import it.niedermann.owncloud.notes.activities.repository.VersionsResult
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.FragmentActivitiesBinding
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
class ActivitiesFragment : BrandedFragment(), OnActivityClickListener {

    lateinit var binding : FragmentActivitiesBinding
    private lateinit var note: Note
    private lateinit var account: Account
    private lateinit var adapter: ActivityListAdapter
    private var lastGiven: Long? = null

    private var executorService: ScheduledExecutorService? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        executorService = Executors.newSingleThreadScheduledExecutor()
        binding = FragmentActivitiesBinding.inflate(inflater, container, false)

        initializeArguments()
        initializeList()
        fetchActivities()

        //DEBUG
        fetchVersions()
        //DEBUG

        return binding.root
    }

    private fun initializeArguments() {
        val bundler = arguments
        note = bundler.getSerializableArgument(ARG_NOTE, Note::class.java)
            ?: throw IllegalArgumentException("Argument $ARG_NOTE is mandatory")
        account = bundler.getSerializableArgument(ARG_ACCOUNT, Account::class.java)
            ?: throw IllegalArgumentException("Argument $ARG_ACCOUNT is mandatory")
    }

    private fun initializeList() {
        adapter = ActivityListAdapter(
            requireContext(),
            this
        )
        binding.list.adapter = adapter
        binding.swipeContainingList.setOnRefreshListener { fetchActivities() }
    }

    override fun applyBrand(color: Int) {
        val util = BrandingUtil.of(color, requireContext())
        util.platform.colorViewBackground(binding.getRoot())
        util.androidx.themeSwipeRefreshLayout(binding.swipeContainingList)
    }

    private fun fetchActivities(reqLastGiven: Long? = null) {
        val noteId = note.remoteId ?: return

        binding.swipeContainingList.isRefreshing = true

        executorService!!.submit(Runnable submit@{
            val ssoAccount =
                SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext())
            val repository = ActivitiesRepository(requireContext(), ssoAccount)
            lifecycleScope.launch(Dispatchers.IO) {
                val result = repository.fetchActivities(noteId, reqLastGiven)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is ActivitiesResult.Success -> {
                            val isFirstPage = reqLastGiven == null || lastGiven == null
                            lastGiven = result.lastGiven
                            showActivities(result.activities, isFirstPage)
                        }

                        is ActivitiesResult.FailedNotModified -> {
                            Log.i(TAG, "Reached end of list (last page starting from item $lastGiven")
                        }

                        is ActivitiesResult.Failed -> {
                            adapter.clear()
                            showEmptyContent(getString(R.string.activities_error))
                        }
                    }

                    binding.swipeContainingList.isRefreshing = false
                }
            }

        })
    }

    private fun fetchVersions() {
        val noteId = note.remoteId ?: return
        executorService!!.submit(Runnable submit@{
            val ssoAccount =
                SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext())
            val repository = VersionsRepository(requireContext(), ssoAccount)
            lifecycleScope.launch(Dispatchers.IO) {
                val result = repository.fetchVersions(noteId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is VersionsResult.Success -> {
                            showVersions(result.versions)
                            result.versions.forEach {
                                Log.d("DEBUG", "Version: ${it.remoteId} ${it.fileName} ${it.modifiedTimestamp}")
                            }
                        }

                        is VersionsResult.Failed -> {
                            Log.e("DEBUG", "Versions failed")
                        }
                    }
                }
            }
        })
    }

    private fun showActivities(items: List<Activity>, isFirstPage: Boolean) {
        binding.emptyList.visibility = View.GONE
        if (isFirstPage) adapter.clear()
        adapter.addActivities(items)
    }

    private fun showVersions(items: List<FileVersion>) {
        binding.emptyList.visibility = View.GONE
        adapter.addVersions(items)
    }

    private fun showEmptyContent(message: String) =
        with (binding.emptyList) {
            visibility = View.VISIBLE
            text = message
        }

    override fun onActivityItemClick(activity: Activity) {
        // No action
    }

    override fun onVersionItemClick(version: FileVersion) {
        // Restore the version
        TODO("Not yet implemented")
    }

    override fun lastActivityReached() {
        if (adapter.itemCount < ActivitiesRepository.PAGE_SIZE)
            return
        fetchActivities(lastGiven)
    }

    companion object {
        val TAG = ActivitiesFragment::class.simpleName
        const val ARG_NOTE = "NOTE"
        const val ARG_ACCOUNT = "ACCOUNT"

        fun newInstance(note: Note, account: Account) : ActivitiesFragment {
            val f = ActivitiesFragment()

            val bundle = Bundle()
            bundle.putSerializable(ARG_NOTE, note)
            bundle.putSerializable(ARG_ACCOUNT, account)

            f.arguments = bundle
            return f
        }
    }
}