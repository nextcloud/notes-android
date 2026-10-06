package it.niedermann.owncloud.notes.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.databinding.FragmentActivitiesBinding

class ActivitiesFragment : BrandedFragment() {

    lateinit var binding : FragmentActivitiesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentActivitiesBinding.inflate(inflater)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun applyBrand(color: Int) {
        // TODO
    }

    companion object {
        fun newInstance() : ActivitiesFragment {
            return ActivitiesFragment()
        }
    }
}