/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2022-2023 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2020 Nextcloud GmbH
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.accountswitcher.bottomSheet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.users.ClearAt
import com.owncloud.android.lib.resources.users.PredefinedStatus
import com.owncloud.android.lib.resources.users.Status
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.vanniktech.emoji.installDisableKeyboardInput
import com.vanniktech.emoji.installForceSingleEmoji
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.accountswitcher.adapter.predefinedStatus.PredefinedStatusClickListener
import it.niedermann.owncloud.notes.accountswitcher.adapter.predefinedStatus.PredefinedStatusListAdapter
import it.niedermann.owncloud.notes.accountswitcher.repository.UserStatusRepository
import it.niedermann.owncloud.notes.branding.BrandedBottomSheetDialogFragment
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.SetStatusMessageBottomSheetBinding
import it.niedermann.owncloud.notes.shared.util.DisplayUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

private const val POS_DONT_CLEAR = 0
private const val POS_HALF_AN_HOUR = 1
private const val POS_AN_HOUR = 2
private const val POS_FOUR_HOURS = 3
private const val POS_TODAY = 4
private const val POS_END_OF_WEEK = 5

private const val ONE_SECOND_IN_MILLIS = 1000
private const val ONE_MINUTE_IN_SECONDS = 60
private const val THIRTY_MINUTES = 30
private const val FOUR_HOURS = 4
private const val LAST_HOUR_OF_DAY = 23
private const val LAST_MINUTE_OF_HOUR = 59
private const val LAST_SECOND_OF_MINUTE = 59

private const val CLEAR_AT_TYPE_PERIOD = "period"
private const val CLEAR_AT_TYPE_END_OF = "end-of"

class SetStatusMessageBottomSheet(
    private val repository: UserStatusRepository,
    private val currentStatus: Status
) :
    BrandedBottomSheetDialogFragment(R.layout.set_status_message_bottom_sheet),
    PredefinedStatusClickListener {
    companion object {
        private const val TAG = "SetStatusMessageBottomSheet"
    }

    private lateinit var binding: SetStatusMessageBottomSheetBinding

    private lateinit var adapter: PredefinedStatusListAdapter
    private var selectedPredefinedMessageId: String? = null
    private var clearAt: Long? = -1
    private lateinit var popup: EmojiPopup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
    }

    private fun initRepository() {
        lifecycleScope.launch(Dispatchers.IO) {
            val predefinedStatus = repository.fetchPredefinedStatuses()
            withContext(Dispatchers.Main) {
                initPredefinedStatusAdapter(predefinedStatus)
            }
        }
    }

    private fun initPredefinedStatusAdapter(predefinedStatus: ArrayList<PredefinedStatus>) {
        adapter = PredefinedStatusListAdapter(this, requireContext())
        Log_OC.d(TAG, "PredefinedStatusListAdapter initialized")
        adapter.list = predefinedStatus
        binding.predefinedStatusList.adapter = adapter
        binding.predefinedStatusList.layoutManager = LinearLayoutManager(context)
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRepository()
        updateCurrentStatusViews(currentStatus)
        binding.clearStatus.setOnClickListener { clearStatus() }
        binding.setStatus.setOnClickListener { setStatusMessage() }
        binding.emoji.setOnClickListener { popup.show() }

        popup = EmojiPopup(view, binding.emoji, onEmojiClickListener = { _ ->
            popup.dismiss()
            binding.emoji.clearFocus()
            val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
            imm.hideSoftInputFromWindow(binding.emoji.windowToken, 0)
        })
        binding.emoji.installForceSingleEmoji()
        binding.emoji.installDisableKeyboardInput(popup)

        clearStatusAdapter()
    }

    private fun clearStatusAdapter() {
        val adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                add(getString(R.string.dontClear))
                add(getString(R.string.thirtyMinutes))
                add(getString(R.string.oneHour))
                add(getString(R.string.fourHours))
                add(getString(R.string.today))
                add(getString(R.string.thisWeek))
            }

        binding.clearStatusAfterSpinner.apply {
            this.adapter = adapter
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setClearStatusAfterValue(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
    }

    override fun applyBrand(color: Int) {
        BrandingUtil.of(color, requireContext()).run {
            platform.themeDialog(binding.root)
            material.run {
                colorMaterialButtonPrimaryBorderless(binding.clearStatus)
                colorMaterialButtonPrimaryTonal(binding.setStatus)
                colorTextInputLayout(binding.customStatusInputContainer)
            }
        }
    }

    private fun updateCurrentStatusViews(it: Status) {
        if (it.icon.isNullOrBlank()) {
            binding.emoji.setText("😀")
        } else {
            binding.emoji.setText(it.icon)
        }

        binding.customStatusInput.text?.clear()
        binding.customStatusInput.setText(it.message)

        if (it.clearAt > 0) {
            binding.clearStatusAfterSpinner.visibility = View.GONE
            binding.remainingClearTime.apply {
                binding.clearStatusMessageTextView.text = getString(R.string.clear)
                visibility = View.VISIBLE
                text = DisplayUtils.getRelativeTimestamp(
                    context,
                    it.clearAt * ONE_SECOND_IN_MILLIS,
                    true
                )
                    .toString()
                    .replaceFirstChar { it.lowercase(Locale.getDefault()) }
                setOnClickListener {
                    visibility = View.GONE
                    binding.clearStatusAfterSpinner.visibility = View.VISIBLE
                    binding.clearStatusMessageTextView.text = getString(R.string.clear_status_after)
                }
            }
        }
    }

    private fun setClearStatusAfterValue(item: Int) {
        clearAt = when (item) {
            POS_DONT_CLEAR -> null // don't clear
            POS_HALF_AN_HOUR -> {
                // 30 minutes
                System.currentTimeMillis() / ONE_SECOND_IN_MILLIS + THIRTY_MINUTES * ONE_MINUTE_IN_SECONDS
            }

            POS_AN_HOUR -> {
                // one hour
                System.currentTimeMillis() / ONE_SECOND_IN_MILLIS + ONE_MINUTE_IN_SECONDS * ONE_MINUTE_IN_SECONDS
            }

            POS_FOUR_HOURS -> {
                // four hours
                System.currentTimeMillis() / ONE_SECOND_IN_MILLIS +
                        FOUR_HOURS * ONE_MINUTE_IN_SECONDS * ONE_MINUTE_IN_SECONDS
            }

            POS_TODAY -> {
                // today
                val date = getLastSecondOfToday()
                dateToSeconds(date)
            }

            POS_END_OF_WEEK -> {
                // end of week
                val date = getLastSecondOfToday()
                while (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    date.add(Calendar.DAY_OF_YEAR, 1)
                }
                dateToSeconds(date)
            }

            else -> clearAt
        }
    }

    private fun clearAtToUnixTime(clearAt: ClearAt?): Long = when {
        clearAt?.type == CLEAR_AT_TYPE_PERIOD -> {
            System.currentTimeMillis() / ONE_SECOND_IN_MILLIS + clearAt.time.toLong()
        }

        clearAt?.type == CLEAR_AT_TYPE_END_OF && clearAt.time == "day" -> {
            val date = getLastSecondOfToday()
            dateToSeconds(date)
        }

        else -> -1
    }

    private fun getLastSecondOfToday(): Calendar {
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, LAST_HOUR_OF_DAY)
            set(Calendar.MINUTE, LAST_MINUTE_OF_HOUR)
            set(Calendar.SECOND, LAST_SECOND_OF_MINUTE)
        }
        return date
    }

    private fun dateToSeconds(date: Calendar) = date.timeInMillis / ONE_SECOND_IN_MILLIS

    private fun clearStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = repository.clearStatus()
            dismiss(result)
        }
    }

    private fun setStatusMessage() {
        if (selectedPredefinedMessageId != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = repository.setPredefinedStatus(selectedPredefinedMessageId!!, clearAt)
                dismiss(result)
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = repository.setCustomStatus(
                    binding.customStatusInput.text.toString(),
                    binding.emoji.text.toString(),
                    clearAt
                )
                dismiss(result)
            }
        }
    }

    private suspend fun dismiss(boolean: Boolean?) = withContext(Dispatchers.Main) {
        if (boolean == true) {
            dismiss()
        } else {
            val message = view?.resources?.getString(R.string.error_setting_status_message)
            DisplayUtils.showSnackMessage(view, message)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SetStatusMessageBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onClick(predefinedStatus: PredefinedStatus) {
        selectedPredefinedMessageId = predefinedStatus.id
        clearAt = clearAtToUnixTime(predefinedStatus.clearAt)
        binding.emoji.setText(predefinedStatus.icon)
        binding.customStatusInput.text?.clear()
        binding.customStatusInput.text?.append(predefinedStatus.message)

        binding.remainingClearTime.visibility = View.GONE
        binding.clearStatusAfterSpinner.visibility = View.VISIBLE
        binding.clearStatusMessageTextView.text = getString(R.string.clear_status_after)

        val clearAt = predefinedStatus.clearAt
        if (clearAt == null) {
            binding.clearStatusAfterSpinner.setSelection(0)
        } else {
            when (clearAt.type) {
                CLEAR_AT_TYPE_PERIOD -> updateClearAtViewsForPeriod(clearAt)
                CLEAR_AT_TYPE_END_OF -> updateClearAtViewsForEndOf(clearAt)
            }
        }
        setClearStatusAfterValue(binding.clearStatusAfterSpinner.selectedItemPosition)
    }

    private fun updateClearAtViewsForPeriod(clearAt: ClearAt) {
        when (clearAt.time) {
            "1800" -> binding.clearStatusAfterSpinner.setSelection(POS_HALF_AN_HOUR)
            "3600" -> binding.clearStatusAfterSpinner.setSelection(POS_AN_HOUR)
            "14400" -> binding.clearStatusAfterSpinner.setSelection(POS_FOUR_HOURS)
            else -> binding.clearStatusAfterSpinner.setSelection(POS_DONT_CLEAR)
        }
    }

    private fun updateClearAtViewsForEndOf(clearAt: ClearAt) {
        when (clearAt.time) {
            "day" -> binding.clearStatusAfterSpinner.setSelection(POS_TODAY)
            "week" -> binding.clearStatusAfterSpinner.setSelection(POS_END_OF_WEEK)
            else -> binding.clearStatusAfterSpinner.setSelection(POS_DONT_CLEAR)
        }
    }
}
