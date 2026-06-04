/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.nextcloud.android.sso.exceptions.SSOException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import it.niedermann.owncloud.notes.LockedActivity
import it.niedermann.owncloud.notes.NotesApplication
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityInteractiveWidgetConfigurationBinding
import it.niedermann.owncloud.notes.main.MainActivity
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter
import it.niedermann.owncloud.notes.main.navigation.NavigationClickListener
import it.niedermann.owncloud.notes.main.navigation.NavigationItem
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData
import it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType
import it.niedermann.owncloud.notes.widget.notelist.NoteListViewModel
import java.util.concurrent.Executors

class InteractiveNoteListWidgetConfigurationActivity : LockedActivity(), NavigationClickListener {
    private val executor = Executors.newCachedThreadPool()

    private var binding: ActivityInteractiveWidgetConfigurationBinding? = null
    private lateinit var viewModel: NoteListViewModel
    private lateinit var adapterCategories: NavigationAdapter
    private lateinit var repo: NotesRepository

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var localAccount: Account? = null
    private var selectedItemId = MainActivity.ADAPTER_KEY_RECENT
    private var selectedItem: NavigationItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        repo = NotesRepository.getInstance(this)
        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d(TAG, "INVALID_APPWIDGET_ID")
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[NoteListViewModel::class.java]
        adapterCategories = NavigationAdapter(this, this)

        val views = ActivityInteractiveWidgetConfigurationBinding.inflate(layoutInflater)
        binding = views
        setContentView(views.root)
        views.recyclerView.adapter = adapterCategories
        views.saveWidget.setOnClickListener { saveWidget() }
        restorePreferences(views)

        loadAccount()
    }

    private fun restorePreferences(views: ActivityInteractiveWidgetConfigurationBinding) {
        views.favoritesFirstCheckbox.isChecked =
            InteractiveWidgetPreferences.isFavoritesFirst(this, appWidgetId)

        val sortOrder = InteractiveWidgetPreferences.getSortOrder(this, appWidgetId)
        if (sortOrder == WidgetSortOrder.OLDEST_FIRST) {
            views.sortOldestFirst.isChecked = true
        } else {
            views.sortNewestFirst.isChecked = true
        }
    }

    override fun onItemClick(item: NavigationItem) = select(item)

    override fun onIconClick(item: NavigationItem) = select(item)

    private fun applyNavigationItems(navigationItems: List<NavigationItem>) {
        adapterCategories.setItems(navigationItems)

        val item = navigationItems.firstOrNull { it.id == selectedItemId }
            ?: navigationItems.firstOrNull { it.id == MainActivity.ADAPTER_KEY_RECENT }
            ?: return

        select(item)
    }

    private fun select(item: NavigationItem) {
        selectedItem = item
        selectedItemId = item.id
        adapterCategories.setSelectedItem(item.id)
    }

    private fun saveWidget() {
        val account = localAccount ?: run {
            Log.w(TAG, "No account loaded; ignoring selection")
            return
        }
        val item = selectedItem ?: run {
            Log.w(TAG, "No category selected; ignoring selection")
            return
        }
        val views = binding ?: return

        val favoritesFirst = views.favoritesFirstCheckbox.isChecked
        val sortOrder = if (views.sortOldestFirst.isChecked) {
            WidgetSortOrder.OLDEST_FIRST
        } else {
            WidgetSortOrder.NEWEST_FIRST
        }

        val data = NotesListWidgetData().apply {
            id = appWidgetId
            accountId = account.id
            themeMode = NotesApplication.getAppTheme(applicationContext).modeId
        }
        applyNavigationMode(data, item)

        executor.execute {
            repo.createOrUpdateNoteListWidgetData(data)
            InteractiveWidgetPreferences.save(applicationContext, appWidgetId, favoritesFirst, sortOrder)

            val updateIntent = Intent(
                AppWidgetManager.ACTION_APPWIDGET_UPDATE,
                null,
                applicationContext,
                InteractiveNoteListWidget::class.java
            ).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            applicationContext.sendBroadcast(updateIntent)

            runOnUiThread {
                setResult(Activity.RESULT_OK, updateIntent)
                finish()
            }
        }
    }

    private fun applyNavigationMode(data: NotesListWidgetData, item: NavigationItem) {
        when (item.type) {
            ENavigationCategoryType.RECENT -> data.mode = NotesListWidgetData.MODE_DISPLAY_ALL
            ENavigationCategoryType.FAVORITES -> data.mode = NotesListWidgetData.MODE_DISPLAY_STARRED
            ENavigationCategoryType.UNCATEGORIZED -> {
                data.mode = NotesListWidgetData.MODE_DISPLAY_CATEGORY
                data.category = null
            }

            ENavigationCategoryType.DEFAULT_CATEGORY -> applyCategoryMode(data, item)
            null -> fallBackToAllNotes(data)
        }
    }

    private fun applyCategoryMode(data: NotesListWidgetData, item: NavigationItem) {
        if (item !is NavigationItem.CategoryNavigationItem) {
            fallBackToAllNotes(data)
            return
        }

        data.mode = NotesListWidgetData.MODE_DISPLAY_CATEGORY
        data.category = item.category
    }

    private fun fallBackToAllNotes(data: NotesListWidgetData) {
        data.mode = NotesListWidgetData.MODE_DISPLAY_ALL
        Log.e(TAG, "Unknown item navigation type. Fallback to show ${ENavigationCategoryType.RECENT}")
    }

    private fun loadAccount() {
        executor.execute {
            val account = currentAccount()
            if (account == null) {
                runOnUiThread {
                    Toast.makeText(this, R.string.widget_not_logged_in, Toast.LENGTH_LONG).show()
                    finish()
                }
                return@execute
            }

            localAccount = account
            val storedData = repo.getNoteListWidgetData(appWidgetId)
            selectedItemId = InteractiveWidgetSelection.navigationItemId(storedData)

            runOnUiThread {
                if (storedData != null) {
                    binding?.saveWidget?.setText(R.string.widget_edit)
                }
                viewModel.getAdapterCategories(account.id).observe(this, ::applyNavigationItems)
            }
        }
    }

    private fun currentAccount(): Account? = try {
        repo.getAccountByName(SingleAccountHelper.getCurrentSingleSignOnAccount(this).name)
    } catch (e: SSOException) {
        Log.w(TAG, "Account not found", e)
        null
    }

    override fun applyBrand(color: Int) {
        val views = binding ?: return
        val util = BrandingUtil.of(color, this)

        util.platform.themeCheckbox(views.favoritesFirstCheckbox)
        util.platform.themeRadioButton(views.sortNewestFirst)
        util.platform.themeRadioButton(views.sortOldestFirst)
        util.material.themeExtendedFAB(views.saveWidget)
    }

    companion object {
        private val TAG: String = InteractiveNoteListWidgetConfigurationActivity::class.java.simpleName
    }
}
