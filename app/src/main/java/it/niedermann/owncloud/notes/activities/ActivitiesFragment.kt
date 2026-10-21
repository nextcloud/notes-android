package it.niedermann.owncloud.notes.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.adapter.ActivityListAdapter
import it.niedermann.owncloud.notes.activities.adapter.OnActivityClickListener
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.activities.repository.ActivitiesRepository
import it.niedermann.owncloud.notes.activities.repository.RestoreVersionsResult
import it.niedermann.owncloud.notes.activities.repository.VersionsRepository
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.branding.BrandedSnackbar
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.FragmentActivitiesBinding
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
class ActivitiesFragment : BrandedFragment(), OnActivityClickListener, ActivitiesDataProvider.OnPageFetchedListener {

    lateinit var binding : FragmentActivitiesBinding
    private lateinit var note: Note
    private lateinit var account: Account
    private lateinit var adapter: ActivityListAdapter

    private lateinit var activityDataProvider: ActivitiesDataProvider

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentActivitiesBinding.inflate(inflater, container, false)

        initializeArguments()
        initializeList()

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
        val id = note.remoteId
        if (id == null) {
            Log.e(TAG, "Unable to load list: note has no remoteId")
            return
        }

        activityDataProvider = ActivitiesDataProvider(
            requireContext(),
            lifecycleScope,
            id,
            this
        )
        adapter = ActivityListAdapter(
            this
        )
        binding.list.adapter = adapter
        binding.swipeContainingList.setOnRefreshListener {
            // Reload list
            activityDataProvider.reset()
            activityDataProvider.fetchNextPage()
        }

        activityDataProvider.fetchNextPage()
    }

    override fun applyBrand(color: Int) {
        adapter.applyBrand(color)
        val util = BrandingUtil.of(color, requireContext())
        util.platform.colorViewBackground(binding.getRoot())
        util.androidx.themeSwipeRefreshLayout(binding.swipeContainingList)
    }

    override fun onActivityItemClick(activity: Activity) {
        // No action
    }

    override fun onVersionItemClick(version: FileVersion) {
        // Restore the version
        val ssoAccount =
            SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext())
        val repository = VersionsRepository(requireContext(), ssoAccount)
        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository.restoreVersion(version)
            withContext(Dispatchers.Main) {
                val message =
                    if (result == RestoreVersionsResult.Success)
                        R.string.version_restored
                    else
                        R.string.version_restore_failed

                BrandedSnackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

                // Reload list
                if (result == RestoreVersionsResult.Success) {
                    activityDataProvider.reset()
                    activityDataProvider.fetchNextPage()
                }
            }
        }
    }

    override fun lastActivityReached() {
        if (adapter.itemCount < ActivitiesRepository.PAGE_SIZE)
            return
        activityDataProvider.fetchNextPage()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPageFetched() {
        binding.swipeContainingList.isRefreshing = false
        binding.emptyList.visibility = View.GONE
        adapter.items.clear()
        adapter.items.addAll(
            activityDataProvider.data.getAggregatedList(requireContext())
        )
        adapter.lastActivity = activityDataProvider.data.activities.lastOrNull()
        adapter.notifyDataSetChanged()
    }

    override fun onPageFetchError() {
        binding.swipeContainingList.isRefreshing = false
        if (adapter.items.isEmpty()) binding.emptyList.visibility = View.VISIBLE
        BrandedSnackbar.make(binding.root, R.string.activities_error, Snackbar.LENGTH_SHORT).show()
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