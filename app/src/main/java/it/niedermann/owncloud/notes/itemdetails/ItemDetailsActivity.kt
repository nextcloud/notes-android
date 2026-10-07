package it.niedermann.owncloud.notes.itemdetails

import android.os.Bundle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.ActivitiesFragment
import it.niedermann.owncloud.notes.branding.BrandedActivity
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.databinding.ActivityItemDetailsBinding
import it.niedermann.owncloud.notes.share.NoteShareActivity

class ItemDetailsActivity : BrandedActivity() {

    lateinit var binding : ActivityItemDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pages = listOf<Pair<String,BrandedFragment>>(
            Pair(getString(R.string.activities_title), ActivitiesFragment.newInstance())
            // TODO: convert NoteShareActivity to Kotlin and refactor to be a BrandedFragment
            //Pair(getString(R.string.share), NoteShareFragment.newInstance())
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

}
