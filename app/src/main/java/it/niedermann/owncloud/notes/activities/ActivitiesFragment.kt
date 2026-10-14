package it.niedermann.owncloud.notes.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.nextcloud.android.sso.helper.SingleAccountHelper
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.adapter.ActivityListAdapter
import it.niedermann.owncloud.notes.activities.adapter.OnActivityClickListener
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.activities.repository.ActivitiesRepository
import it.niedermann.owncloud.notes.activities.repository.ActivitiesResult
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

    private var executorService: ScheduledExecutorService? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        executorService = Executors.newSingleThreadScheduledExecutor()
        binding = FragmentActivitiesBinding.inflate(inflater, container, false)

        initializeArguments()
        initializeList()
        fetchActivities()

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
    }

    override fun applyBrand(color: Int) {
        val util = BrandingUtil.of(color, requireContext())
        util.platform.colorViewBackground(binding.getRoot())
    }

    private fun fetchActivities() {
        val noteId = note.remoteId ?: return

        binding.progress.visibility = View.VISIBLE

        executorService!!.submit(Runnable submit@{
            val ssoAccount =
                SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext())
            val repository = ActivitiesRepository(requireContext(), ssoAccount)
            lifecycleScope.launch(Dispatchers.IO) {
                val result = repository.fetchActivities(noteId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is ActivitiesResult.Success -> {
                            showActivities(result.activities)
                            // TODO: pagination with LastGiven
                            Log.d("DEBUG", "Activities LastGiven is ${result.lastGiven}")
                        }

                        is ActivitiesResult.FailedNotModified -> {
                            adapter.clear()
                            showEmptyContent(getString(R.string.activities_no_results))
                        }

                        is ActivitiesResult.Failed -> {
                            adapter.clear()
                            showEmptyContent(getString(R.string.activities_error))
                        }
                    }

                    binding.progress.visibility = View.GONE
                    binding.emptyList.visibility =
                        if (adapter.itemCount > 0) View.VISIBLE
                        else View.GONE
                }
            }

        })
    }

    private fun showActivities(activities: List<Activity>) {
        binding.emptyList.visibility = View.GONE
        adapter.setItems(activities)
    }

    private fun showEmptyContent(message: String) =
        with (binding.emptyList) {
            visibility = View.VISIBLE
            text = message
        }

    override fun onItemClick(activity: Activity) {
        TODO("Not yet implemented")
    }

    companion object {
        const val ARG_NOTE: String = "NOTE"
        const val ARG_ACCOUNT: String = "ACCOUNT"

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