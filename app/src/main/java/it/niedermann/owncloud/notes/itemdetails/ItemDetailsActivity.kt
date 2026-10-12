package it.niedermann.owncloud.notes.itemdetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.ActivitiesFragment
import it.niedermann.owncloud.notes.branding.BrandedActivity
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.databinding.ActivityItemDetailsBinding
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.share.NoteShareFragment
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument

class ItemDetailsActivity : BrandedActivity() {

    lateinit var binding : ActivityItemDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val note = intent.extras.getSerializableArgument(ARG_NOTE, Note::class.java)
            ?: throw RuntimeException("Argument $ARG_NOTE is mandatory")
        val account = intent.extras.getSerializableArgument(ARG_ACCOUNT, Account::class.java)
            ?: throw RuntimeException("Argument $ARG_ACCOUNT is mandatory")

        val pages = listOf<Pair<String,BrandedFragment>>(
            Pair(getString(R.string.activities_title), ActivitiesFragment.newInstance()),
            Pair(getString(R.string.share), NoteShareFragment.newInstance(note, account))
        )

        binding.pager.adapter = PagerAdapter(pages)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = pages[position].first
        }.attach()
    }

    override fun applyBrand(color: Int) {
        // TODO
    }

    private inner class PagerAdapter(val fragments: List<Pair<String,BrandedFragment>>) :
        FragmentStateAdapter(this@ItemDetailsActivity)
    {
        override fun getItemCount() = fragments.size
        override fun createFragment(position: Int) = fragments[position].second
    }

    companion object {
        const val ARG_NOTE: String = "note"
        const val ARG_ACCOUNT: String = "account"

        @JvmStatic
        fun getActivityStartIntent(context: Context, note: Note, account: Account): Intent {
            val bundle = Bundle()
            bundle.putSerializable(ARG_NOTE, note)
            bundle.putSerializable(ARG_ACCOUNT, account)

            val intent = Intent(context, ItemDetailsActivity::class.java)
            intent.putExtras(bundle)
            return intent
        }
    }

}
