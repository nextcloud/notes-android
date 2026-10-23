package it.niedermann.owncloud.notes.itemdetails

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.ActivitiesFragment
import it.niedermann.owncloud.notes.branding.BrandedActivity
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityItemDetailsBinding
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.share.NoteShareFragment
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument


/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
class ItemDetailsActivity : BrandedActivity() {

    lateinit var binding : ActivityItemDetailsBinding
    lateinit var note: Note
    lateinit var account: Account
    lateinit var page: PAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parseArguments()

        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadHeaderData()
        loadFragments()
    }

    private fun parseArguments() {
        note = intent.extras.getSerializableArgument(ARG_NOTE, Note::class.java)
            ?: throw RuntimeException("Argument $ARG_NOTE is mandatory")
        account = intent.extras.getSerializableArgument(ARG_ACCOUNT, Account::class.java)
            ?: throw RuntimeException("Argument $ARG_ACCOUNT is mandatory")
        page = intent.extras.getSerializableArgument(ARG_PAGE, PAGE::class.java)
            ?: throw RuntimeException("Argument $ARG_ACCOUNT is mandatory")
    }

    private fun loadHeaderData() {
        binding.filename.text = note.title
        binding.favorite.setImageResource(
            if (note.favorite) R.drawable.ic_star_yellow_24dp
            else R.drawable.ic_star_border_grey_ccc_24dp
        )
        binding.lastModificationTimestamp.text = note.modified?.let {
            DateUtils.formatDateTime(
                this,
                it.timeInMillis,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            )
        }
    }

    private fun loadFragments() {
        val pages = listOf(
            Page(
                PAGE.ACTIVITIES,
                getString(R.string.activities_title),
                AppCompatResources.getDrawable(this, R.drawable.nav_activity),
                ActivitiesFragment.newInstance(note, account)
            ),
            Page(
                PAGE.SHARE,
                getString(R.string.share),
                AppCompatResources.getDrawable(this, R.drawable.ic_share_white_24dp),
                NoteShareFragment.newInstance(note, account)
            )
        )
        binding.pager.adapter = PagerAdapter(pages)

        // Select page
        binding.pager.currentItem = pages.indexOf(pages.find { it.page == page })

        // Set tabs
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = pages[position].title
            tab.icon = pages[position].icon
        }.attach()
    }

    override fun applyBrand(color: Int) {
        val util = BrandingUtil.of(color, this)
        util.material.themeToolbar(binding.toolbar);
        util.platform.colorViewBackground(binding.getRoot())
        util.platform.colorViewBackground(binding.tabLayout)
        util.material.themeTabLayout(binding.tabLayout)
    }

    private data class Page (
        val page: PAGE,
        val title: String,
        val icon: Drawable?,
        val fragment: BrandedFragment
    )

    private inner class PagerAdapter(val fragments: List<Page>) :
        FragmentStateAdapter(this@ItemDetailsActivity)
    {
        override fun getItemCount() = fragments.size
        override fun createFragment(position: Int) = fragments[position].fragment
    }

    public enum class PAGE { ACTIVITIES, SHARE }

    companion object {
        const val ARG_NOTE: String = "note"
        const val ARG_ACCOUNT: String = "account"
        const val ARG_PAGE: String = "page"

        @JvmStatic
        fun getActivityStartIntent(context: Context, note: Note, account: Account): Intent =
            getActivityStartIntent(context, note, account, PAGE.ACTIVITIES)

        @JvmStatic
        fun getActivityStartIntent(context: Context, note: Note, account: Account, page: PAGE): Intent {
            val bundle = Bundle()
            bundle.putSerializable(ARG_NOTE, note)
            bundle.putSerializable(ARG_ACCOUNT, account)
            bundle.putSerializable(ARG_PAGE, page)

            val intent = Intent(context, ItemDetailsActivity::class.java)
            intent.putExtras(bundle)
            return intent
        }
    }

}
