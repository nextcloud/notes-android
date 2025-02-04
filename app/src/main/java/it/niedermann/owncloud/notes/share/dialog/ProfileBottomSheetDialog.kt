package it.niedermann.owncloud.notes.share.dialog


import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nextcloud.android.lib.resources.profile.Action
import com.nextcloud.android.lib.resources.profile.HoverCard
import it.niedermann.nextcloud.sso.glide.SingleSignOnUrl
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.databinding.ProfileBottomSheetActionBinding
import it.niedermann.owncloud.notes.databinding.ProfileBottomSheetFragmentBinding
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.shared.util.DisplayUtils

/**
 * Show actions of an user
 */
class ProfileBottomSheetDialog(
    private val fileActivity: FragmentActivity,
    private val account: Account,
    private val hoverCard: HoverCard,
) : BottomSheetDialog(fileActivity) {
    private var _binding: ProfileBottomSheetFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ProfileBottomSheetFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (window != null) {
            window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // viewThemeUtils.platform.themeDialog(binding.root)

        binding.icon.tag = hoverCard.userId


        /*
            // TODO: How to get owner display name from note?

             binding.sharedWithYouUsername.setText(
                    String.format(getString(R.string.note_share_fragment_shared_with_you), file.getOwnerDisplayName()));
             */
        Glide.with(fileActivity)
            .load(
                SingleSignOnUrl(
                    account.accountName,
                    account.url + "/index.php/avatar/" + Uri.encode(account.userName) + "/64"
                )
            )
            .placeholder(R.drawable.ic_account_circle_grey_24dp)
            .error(R.drawable.ic_account_circle_grey_24dp)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.icon)

        binding.displayName.text = hoverCard.displayName

        for (action in hoverCard.actions) {
            val actionBinding = ProfileBottomSheetActionBinding.inflate(
                layoutInflater
            )
            val creatorView: View = actionBinding.root

            if (action.appId == "email") {
                action.hyperlink = action.title
                action.title = context.resources.getString(R.string.write_email)
            }

            actionBinding.name.text = action.title

            val icon = when (action.appId) {
                "profile" -> R.drawable.ic_account_circle_grey_24dp
                "email" -> R.drawable.ic_email
                "spreed" -> R.drawable.ic_talk
                else -> R.drawable.ic_edit
            }
            actionBinding.icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    icon,
                    null
                )
            )
            // viewThemeUtils.platform.tintPrimaryDrawable(context, actionBinding.icon.drawable)

            creatorView.setOnClickListener { v: View? ->
                send(hoverCard.userId, action)
                dismiss()
            }
            binding.creators.addView(creatorView)
        }

        setOnShowListener { d: DialogInterface? ->
            BottomSheetBehavior.from(binding.root.parent as View)
                .setPeekHeight(binding.root.measuredHeight)
        }
    }

    private fun send(userId: String, action: Action) {
        when (action.appId) {
            "profile" -> openWebsite(action.hyperlink)
            "core" -> sendEmail(action.hyperlink)
            "spreed" -> openTalk(userId, action.hyperlink)
        }
    }

    private fun openWebsite(url: String) {
        DisplayUtils.startLinkIntent(fileActivity, url)
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }

        DisplayUtils.startIntentIfAppAvailable(intent, fileActivity, R.string.no_email_app_available)
    }

    private fun openTalk(userId: String, hyperlink: String) {
        // TODO:
        /*
        try {
            val sharingIntent = Intent(Intent.ACTION_VIEW)
            sharingIntent.setClassName(
                "com.nextcloud.talk2",
                "com.nextcloud.talk.activities.MainActivity"
            )
            sharingIntent.putExtra("server", user.server.uri)
            sharingIntent.putExtra("userId", userId)
            fileActivity.startActivity(sharingIntent)
        } catch (e: ActivityNotFoundException) {
            openWebsite(hyperlink)
        }
         */

    }

    override fun onStop() {
        super.onStop()
        _binding = null
    }
}
