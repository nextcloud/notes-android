package it.niedermann.owncloud.notes.share

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.SearchManager
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedFragment
import it.niedermann.owncloud.notes.branding.BrandedSnackbar
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.ActivityNoteShareBinding
import it.niedermann.owncloud.notes.main.MainActivity
import it.niedermann.owncloud.notes.persistence.ApiResult
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.persistence.isError
import it.niedermann.owncloud.notes.persistence.isSuccess
import it.niedermann.owncloud.notes.share.adapter.ShareeListAdapter
import it.niedermann.owncloud.notes.share.adapter.SuggestionAdapter
import it.niedermann.owncloud.notes.share.dialog.NoteShareActivityShareItemActionBottomSheetDialog
import it.niedermann.owncloud.notes.share.dialog.QuickSharingPermissionsBottomSheetDialog
import it.niedermann.owncloud.notes.share.dialog.QuickSharingPermissionsBottomSheetDialog.QuickPermissionSharingBottomSheetActions
import it.niedermann.owncloud.notes.share.dialog.ShareLinkToDialog.Companion.newInstance
import it.niedermann.owncloud.notes.share.dialog.SharePasswordDialogFragment
import it.niedermann.owncloud.notes.share.dialog.SharePasswordDialogFragment.Companion.newInstance
import it.niedermann.owncloud.notes.share.dialog.SharePasswordDialogFragment.SharePasswordDialogListener
import it.niedermann.owncloud.notes.share.helper.AvatarLoader
import it.niedermann.owncloud.notes.share.helper.UsersAndGroupsSearchProvider
import it.niedermann.owncloud.notes.share.listener.NoteShareItemAction
import it.niedermann.owncloud.notes.share.listener.ShareeListAdapterListener
import it.niedermann.owncloud.notes.share.model.CreateShareResponse
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest
import it.niedermann.owncloud.notes.share.model.UpdateShareRequest.Companion.createAttributes
import it.niedermann.owncloud.notes.share.model.UsersAndGroupsSearchConfig.reset
import it.niedermann.owncloud.notes.share.model.UsersAndGroupsSearchConfig.searchOnlyUsers
import it.niedermann.owncloud.notes.share.model.toOCShare
import it.niedermann.owncloud.notes.share.repository.ShareRepository
import it.niedermann.owncloud.notes.shared.model.Capabilities
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import it.niedermann.owncloud.notes.shared.util.DisplayUtils
import it.niedermann.owncloud.notes.shared.util.ShareUtil
import it.niedermann.owncloud.notes.shared.util.clipboard.ClipboardUtil.copyToClipboard
import it.niedermann.owncloud.notes.shared.util.extensions.getSerializableArgument
import it.niedermann.owncloud.notes.util.DateUtil.getExpirationDate
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.text.toInt


/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
class NoteShareFragment : BrandedFragment(), ShareeListAdapterListener, NoteShareItemAction,
    QuickPermissionSharingBottomSheetActions, SharePasswordDialogListener {
    private var executorService: ScheduledExecutorService? = null
    private var future: Future<*>? = null
    private var binding: ActivityNoteShareBinding? = null
    private var note: Note? = null
    private var account: Account? = null
    private var repository: ShareRepository? = null
    private var capabilities: Capabilities? = null
    private val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            updateShareeListAdapter()
        }
    }

    private val shares: MutableList<OCShare> =
        Collections.synchronizedList(ArrayList<OCShare>())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        executorService = Executors.newSingleThreadScheduledExecutor()
        binding = ActivityNoteShareBinding.inflate(layoutInflater)
        initializeArguments()
        return binding!!.getRoot()
    }

    private fun initializeArguments() {
        val bundler = arguments
        note = bundler.getSerializableArgument(ARG_NOTE, Note::class.java)
        account = bundler.getSerializableArgument(ARG_ACCOUNT, Account::class.java)

        requireNotNull(note) { "Note cannot be null" }

        requireNotNull(account) { "Account cannot be null" }

        executorService!!.submit {
            try {
                val context = context ?: return@submit
                val ssoAcc =
                    SingleAccountHelper.getCurrentSingleSignOnAccount(context)
                repository = ShareRepository(context, ssoAcc)
                capabilities = repository!!.getCapabilities()
                repository!!.fetchSharesForNotesAndSaveShareEntities()

                activity?.runOnUiThread {
                    binding!!.searchContainer.visibility = View.VISIBLE
                    binding!!.sharesList.visibility = View.VISIBLE
                    binding!!.sharesList.setAdapter(
                        ShareeListAdapter(
                            activity,
                            ArrayList<OCShare?>(),
                            this,
                            account
                        )
                    )
                    binding!!.sharesList.setLayoutManager(LinearLayoutManager(context))
                    binding!!.pickContactEmailBtn.setOnClickListener { checkContactPermission() }
                    binding!!.btnShareButton.setOnClickListener {
                        ShareUtil.openShareDialog(
                            context,
                            note!!.title,
                            note!!.content
                        )
                    }

                    if (note!!.readonly) {
                        setupReadOnlySearchView()
                    } else {
                        setupSearchView()
                    }

                    updateShareeListAdapter()
                    binding!!.loadingLayout.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log_OC.e(TAG, "Exception at NoteShareActivity.init: $e")
            }
        }
    }

    public override fun onStart() {
        super.onStart()

        // note cannot be encrypted - logic ported from files app
        searchOnlyUsers = false
    }

    public override fun onStop() {
        super.onStop()
        reset()
    }

    private fun disableSearchView(view: View) {
        view.setEnabled(false)

        if (view is ViewGroup) {
            for (i in 0..<view.size) {
                disableSearchView(view.getChildAt(i))
            }
        }
    }

    private fun setupReadOnlySearchView() {
        binding!!.searchView.setIconifiedByDefault(false)
        binding!!.searchView.setQueryHint(resources.getString(R.string.note_share_activity_resharing_not_allowed))
        binding!!.searchView.inputType = InputType.TYPE_NULL
        binding!!.pickContactEmailBtn.setVisibility(View.GONE)
        disableSearchView(binding!!.searchView)
    }

    private fun setupSearchView() {
        val context = context ?: return
        val suggestionAdapter = SuggestionAdapter(context, null, account!!)
        val provider = UsersAndGroupsSearchProvider(context, repository)

        binding!!.searchView.setSuggestionsAdapter(suggestionAdapter)
        binding!!.searchView.setIconifiedByDefault(false)
        binding!!.searchView.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        binding!!.searchView.setQueryHint(resources.getString(R.string.note_share_activity_search_text))
        binding!!.searchView.inputType = InputType.TYPE_CLASS_TEXT
        binding!!.searchView.setFocusable(true)
        binding!!.searchView.setFocusableInTouchMode(true)

        val closeButton =
            binding!!.searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener {
            binding!!.progressBar.visibility = View.GONE
            binding!!.searchView.setQuery("", false)
        }

        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // return true to prevent the query from being processed;
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) {
                    binding!!.progressBar.visibility = View.VISIBLE

                    // Cancel the previous task if it's still running
                    if (future != null && !future!!.isDone) {
                        future!!.cancel(true)
                    }

                    // Schedule a new task with a delay
                    future = executorService!!.schedule(Runnable schedule@{
                        if (capabilities == null) {
                            Log_OC.d(TAG, "Capabilities cannot be null")
                            return@schedule
                        }
                        val isFederationShareAllowed = capabilities!!.federationShare
                        try {
                            val cursor =
                                provider.searchForUsersOrGroups(newText, isFederationShareAllowed)

                            if (cursor == null || cursor.count == 0) {
                                return@schedule
                            }

                            activity?.runOnUiThread {
                                if (binding!!.searchView.isVisible) {
                                    suggestionAdapter.swapCursor(cursor)
                                }
                                binding!!.progressBar.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log_OC.d(TAG, "Exception setupSearchView.onQueryTextChange: $e")
                            activity?.runOnUiThread { binding!!.progressBar.visibility = View.GONE }
                        }
                    }, SEARCH_DELAY_MS, TimeUnit.MILLISECONDS)
                } else {
                    binding!!.progressBar.visibility = View.GONE
                    suggestionAdapter.swapCursor(null)
                }
                return false
            }
        })

        binding!!.searchView.setOnQueryTextFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                val imm = context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    binding!!.searchView.findFocus(),
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        }

        binding!!.searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = suggestionAdapter.cursor
                if (cursor != null && cursor.moveToPosition(position)) {
                    val suggestion =
                        cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1))
                    binding!!.searchView.setQuery(suggestion, false)

                    val shareWith =
                        cursor.getString(cursor.getColumnIndexOrThrow(UsersAndGroupsSearchProvider.SHARE_WITH))
                    val shareType =
                        cursor.getInt(cursor.getColumnIndexOrThrow(UsersAndGroupsSearchProvider.SHARE_TYPE))
                    navigateNoteShareDetail(shareWith, shareType)
                }
                return true
            }
        })
    }

    private fun navigateNoteShareDetail(shareWith: String?, shareType: Int) {
        val bundle = Bundle()

        bundle.putSerializable(NoteShareDetailActivity.ARG_NOTE, note)
        bundle.putString(NoteShareDetailActivity.ARG_SHAREE_NAME, shareWith)
        bundle.putInt(NoteShareDetailActivity.ARG_SHARE_TYPE, shareType)

        val intent = Intent(context, NoteShareDetailActivity::class.java)
        intent.putExtras(bundle)
        resultLauncher.launch(intent)
    }

    private fun accountOwnsFile(): Boolean {
        if (shares.isEmpty()) {
            return true
        }

        val share = shares[0]
        val ownerDisplayName = share.ownerDisplayName
        return TextUtils.isEmpty(ownerDisplayName) || account!!.accountName
            .split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].equals(
            ownerDisplayName,
            ignoreCase = true
        )
    }

    private fun setShareWithYou() {
        if (accountOwnsFile()) {
            binding!!.sharedWithYouContainer.visibility = View.GONE
        } else {
            if (shares.isEmpty()) {
                return
            }

            val share = shares[0]

            binding!!.sharedWithYouUsername.text = String.format(
                getString(R.string.note_share_activity_shared_with_you),
                share.ownerDisplayName
            )
            context?.apply { AvatarLoader.load(this, binding!!.sharedWithYouAvatar, account!!) }
            binding!!.sharedWithYouAvatar.setVisibility(View.VISIBLE)

            val description = share.note

            if (!TextUtils.isEmpty(description)) {
                binding!!.sharedWithYouNote.setText(description)
                binding!!.sharedWithYouNoteContainer.visibility = View.VISIBLE
            } else {
                binding!!.sharedWithYouNoteContainer.visibility = View.GONE
            }
        }
    }

    override fun copyInternalLink() {
        if (account == null) {
            DisplayUtils.showSnackMessage(
                view,
                getString(R.string.note_share_activity_could_not_retrieve_url)
            )
            return
        }

        val link = createInternalLink()
        showShareLinkDialog(link)
    }

    override fun createPublicShareLink() {
        if (capabilities == null) {
            Log_OC.d(TAG, "Capabilities cannot be null")
            return
        }

        if (capabilities!!.publicPasswordEnforced || capabilities!!.askForOptionalPassword) {
            // password enforced by server, request to the user before trying to create
            requestPasswordForShareViaLink(true, capabilities!!.askForOptionalPassword)
        } else {
            executorService!!.submit {
                val result: ApiResult<OcsResponse<CreateShareResponse>?> =
                    repository!!.addShare(note!!, ShareType.PUBLIC_LINK, "")
                activity?.runOnUiThread {
                    if (result is ApiResult.Success<OcsResponse<CreateShareResponse>?> && binding!!.sharesList.adapter is ShareeListAdapter) {
                        DisplayUtils.showSnackMessage(view, result.message)
                        note!!.setIsShared(true)
                        repository!!.updateNote(note!!)
                        val adapter = binding!!.sharesList.adapter as ShareeListAdapter
                        adapter.addShare(result.data!!.ocs.data!!.toOCShare())
                    } else if (result is ApiResult.Error) {
                        DisplayUtils.showSnackMessage(view, result.message)
                    }
                }
            }
        }
    }

    fun requestPasswordForShareViaLink(createShare: Boolean, askForPassword: Boolean) {
        val dialog = newInstance(note, createShare, askForPassword, this)
        dialog.show(parentFragmentManager, SharePasswordDialogFragment.PASSWORD_FRAGMENT)
    }

    // TODO:
    override fun createSecureFileDrop() {
    }

    private fun showShareLinkDialog(link: String?) {
        val intentToShareLink = Intent(Intent.ACTION_SEND)

        intentToShareLink.putExtra(Intent.EXTRA_TEXT, link)
        intentToShareLink.setType("text/plain")
        intentToShareLink.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.note_share_activity_subject_shared_with_you, note!!.title)
        )

        val packagesToExclude = arrayOf<String?>(context?.packageName)
        val chooserDialog: DialogFragment = newInstance(intentToShareLink, *packagesToExclude)
        chooserDialog.show(parentFragmentManager, FTAG_CHOOSER_DIALOG)
    }

    private fun createInternalLink(): String {
        val baseUri = account!!.url.toUri()
        return baseUri.toString() + "/index.php/f/" + note!!.remoteId
    }

    override fun copyLink(share: OCShare) {
        if (!note!!.isShared) {
            return
        }

        if (TextUtils.isEmpty(share.shareLink)) {
            copyAndShareFileLink(share.shareLink)
        } else {
            context?.apply { copyToClipboard(this, share.shareLink) }
        }
    }

    private fun copyAndShareFileLink(link: String?) {
        context?.apply { copyToClipboard(this, link, false) }
        val snackbar = Snackbar
            .make(
                requireView(),
                R.string.clipboard_text_copied,
                Snackbar.LENGTH_LONG
            )
            .setAction(
                R.string.share
            ) { showShareLinkDialog(link) }
        snackbar.show()
    }

    override fun showSharingMenuActionSheet(share: OCShare?) {
        NoteShareActivityShareItemActionBottomSheetDialog(activity, this, share).show()
    }

    override fun showShareExpirationSnackbar(share: OCShare) {
        val expirationDescription = getString(
            R.string.share_expires,
            SimpleDateFormat.getDateInstance().format(Date(share.expirationDate))
        )
        val snackbar = Snackbar
            .make(binding!!.getRoot(), expirationDescription, Snackbar.LENGTH_LONG)
            .setAction(R.string.dismiss) { }
        context?.apply {
            val util = BrandingUtil.of(BrandingUtil.readBrandMainColor(this), this)
            util.material.themeSnackbar(snackbar)
        }
        snackbar.show()
    }

    override fun showPermissionsDialog(share: OCShare?) {
        QuickSharingPermissionsBottomSheetDialog(activity, this, share).show()
    }

    override fun requestPasswordForShare(share: OCShare?, askForPassword: Boolean) {
        val dialog = newInstance(share, askForPassword, this)
        dialog.show(parentFragmentManager, SharePasswordDialogFragment.PASSWORD_FRAGMENT)
    }

    // TODO:
    override fun showProfileBottomSheet(account: Account?, shareWith: String?) {
    }

    private var isInitializingShares = false

    fun updateShareeListAdapter() {
        if (isInitializingShares) {
            Log_OC.d(TAG, "Shares initialization already in progress")
            return
        }

        isInitializingShares = true

        executorService!!.submit(Runnable submit@{
            try {
                val adapter = binding!!.sharesList.adapter as ShareeListAdapter?

                if (adapter == null) {
                    activity?.runOnUiThread {
                        DisplayUtils.showSnackMessage(
                            view,
                            getString(R.string.could_not_retrieve_shares)
                        )
                    }
                    isInitializingShares = false
                    return@submit
                }
                val remoteNotes: List<OCShare> = if (note != null) {
                    repository!!.fetchSharesFromNote(note!!)
                } else {
                    listOf()
                }

                activity?.runOnUiThread {
                    shares.clear()
                    shares.addAll(remoteNotes)

                    adapter.removeAll()
                    adapter.addShares(shares)
                    addPublicShares(adapter)
                    setShareWithYou()
                    isInitializingShares = false
                }
            } catch (e: Exception) {
                Log_OC.d(TAG, "Exception while updateShareeListAdapter: $e")
            }
        })
    }

    private fun addPublicShares(adapter: ShareeListAdapter) {
        val publicShares: MutableList<OCShare> = ArrayList()

        if (containsNoNewPublicShare(adapter.shares)) {
            val ocShare = OCShare()
            ocShare.shareType = ShareType.NEW_PUBLIC_LINK
            publicShares.add(ocShare)
        } else {
            adapter.removeNewPublicShare()
        }

        adapter.addShares(publicShares)
    }

    private fun checkContactPermission() {
        val context = context ?: return
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            pickContactEmail()
        } else {
            requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun pickContactEmail() {
        val context = context ?: return
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI)

        // FIXME:
        if (intent.resolveActivity(context.packageManager) != null) {
            onContactSelectionResultLauncher.launch(intent)
        } else {
            DisplayUtils.showSnackMessage(
                view,
                getString(R.string.file_detail_sharing_fragment_no_contact_app_message)
            )
        }
    }

    private fun handleContactResult(contactUri: Uri) {
        // Define the projection to get all email addresses.
        val projection = arrayOf<String?>(ContactsContract.CommonDataKinds.Email.ADDRESS)

        val cursor = context?.contentResolver?.query(contactUri, projection, null, null, null)
        if (cursor == null) {
            DisplayUtils.showSnackMessage(view, getString(R.string.email_pick_failed))
            Log_OC.e(TAG, "Failed to pick email address as Cursor is null.")
            return
        }

        if (!cursor.moveToFirst()) {
            DisplayUtils.showSnackMessage(view, getString(R.string.email_pick_failed))
            Log_OC.e(TAG, "Failed to pick email address as no Email found.")
            return
        }

        // The contact has only one email address, use it.
        val columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
        if (columnIndex == -1) {
            DisplayUtils.showSnackMessage(view, getString(R.string.email_pick_failed))
            Log_OC.e(TAG, "Failed to pick email address.")
            return
        }

        // Use the email address as needed.
        // email variable contains the selected contact's email address.
        val email = cursor.getString(columnIndex)
        binding!!.searchView.post {
            binding!!.searchView.setQuery(email, false)
            binding!!.searchView.requestFocus()
        }

        cursor.close()
    }

    private fun containsNoNewPublicShare(shares: MutableList<OCShare>): Boolean {
        for (share in shares) {
            if (share.shareType != null && share.shareType == ShareType.NEW_PUBLIC_LINK) {
                return false
            }
        }

        return true
    }

    private fun isReshareForbidden(share: OCShare): Boolean {
        return (share.shareType != null && ShareType.FEDERATED == share.shareType) ||
            capabilities != null && !capabilities!!.isReSharingAllowed
    }

    @VisibleForTesting
    fun search(query: String?) {
        val searchView = binding?.searchView
        searchView?.setQuery(query, true)
    }

    override fun advancedPermissions(share: OCShare) {
        modifyExistingShare(share, NoteShareDetailActivity.SCREEN_TYPE_PERMISSION, false)
    }


    override fun sendNewEmail(share: OCShare) {
        modifyExistingShare(share, NoteShareDetailActivity.SCREEN_TYPE_NOTE, true)
    }

    override fun unShare(share: OCShare) {
        executorService!!.submit(Runnable {
            val result = repository!!.removeShare(share, note!!)
            activity?.runOnUiThread(Runnable {
                if (result) {
                    val adapter = binding!!.sharesList.adapter as ShareeListAdapter?
                    if (adapter == null) {
                        DisplayUtils.showSnackMessage(
                            view,
                            getString(R.string.email_pick_failed)
                        )
                        return@Runnable
                    }
                    adapter.remove(share)
                } else {
                    DisplayUtils.showSnackMessage(
                        view,
                        getString(R.string.failed_the_remove_share)
                    )
                }
            })
        })
    }

    // TODO:
    override fun sendLink(share: OCShare?) {
    }

    override fun addAnotherLink(share: OCShare?) {
        createPublicShareLink()
    }

    private fun modifyExistingShare(share: OCShare, screenTypePermission: Int, sendEmail: Boolean) {
        val bundle = Bundle()

        bundle.putSerializable(NoteShareDetailActivity.ARG_OCSHARE, share)
        bundle.putInt(NoteShareDetailActivity.ARG_SCREEN_TYPE, screenTypePermission)
        bundle.putBoolean(NoteShareDetailActivity.ARG_RESHARE_SHOWN, !isReshareForbidden(share))
        bundle.putBoolean(NoteShareDetailActivity.ARG_EXP_DATE_SHOWN, this.expDateShown)
        bundle.putBoolean(NoteShareDetailActivity.ARG_SEND_EMAIL, sendEmail)

        val intent = Intent(context, NoteShareDetailActivity::class.java)
        intent.putExtras(bundle)
        resultLauncher.launch(intent)
    }

    private val expDateShown: Boolean
        get() {
            try {
                if (capabilities == null) {
                    Log_OC.d(TAG, "Capabilities cannot be null")
                    return false
                }

                val majorVersionAsString = capabilities!!.nextcloudMajorVersion
                if (majorVersionAsString != null) {
                    val majorVersion = majorVersionAsString.toInt()
                    return majorVersion >= 18
                }

                return false
            } catch (_: NumberFormatException) {
                Log_OC.d(TAG, "Exception while getting expDateShown")
                return false
            }
        }

    override fun onQuickPermissionChanged(share: OCShare, permission: Int) {
        executorService!!.submit {
            val result: ApiResult<OcsResponse<CreateShareResponse>?> =
                repository!!.updateSharePermission(share.id, permission)
            activity?.runOnUiThread {
                if (result.isSuccess()) {
                    updateShare(share)
                } else if (result is ApiResult.Error) {
                    DisplayUtils.showSnackMessage(view, result.message)
                }
            }
        }
    }

    private fun updateShare(share: OCShare) {
        if (note == null) {
            Log_OC.e(TAG, "note is null, cannot update share")
            return
        }

        executorService!!.submit(Runnable {
            try {
                val updatedShares: List<OCShare?> = repository!!.fetchSharesFromNote(note!!)

                activity?.runOnUiThread(Runnable {
                    if (binding!!.sharesList.adapter is ShareeListAdapter) {
                        var updatedShare: OCShare? = null
                        for (i in updatedShares.indices) {
                            if (updatedShares[i]!!.id == share.id) {
                                updatedShare = updatedShares[i]
                                break
                            }
                        }

                        if (updatedShare == null) {
                            return@Runnable
                        }

                        val adapter = binding!!.sharesList.adapter as ShareeListAdapter
                        adapter.updateShare(updatedShare)
                    }
                })
            } catch (e: Exception) {
                Log_OC.d(TAG, "Exception while refreshing share info: $e")
            }
        })
    }

    private val requestContactPermissionLauncher = registerForActivityResult<String?, Boolean?>(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (isGranted == true) {
            pickContactEmail()
        } else {
            BrandedSnackbar.make(
                binding!!.getRoot(),
                getString(R.string.note_share_activity_contact_no_permission),
                Snackbar.LENGTH_LONG
            )
                .show()
        }
    }

    private val onContactSelectionResultLauncher =
        registerForActivityResult<Intent?, ActivityResult?>(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result: ActivityResult? ->
                if (result!!.resultCode == RESULT_OK) {
                    val intent = result.data
                    if (intent == null) {
                        BrandedSnackbar.make(
                            binding!!.getRoot(),
                            getString(R.string.email_pick_failed),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        return@ActivityResultCallback
                    }

                    val contactUri = intent.data
                    if (contactUri == null) {
                        BrandedSnackbar.make(
                            binding!!.getRoot(),
                            getString(R.string.email_pick_failed),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        return@ActivityResultCallback
                    }

                    handleContactResult(contactUri)
                }
            })

    override fun applyBrand(color: Int) {
        val context = context ?: return
        val util = BrandingUtil.of(color, context)
        util.notes.themeContentSearchView(binding!!.searchView)
        util.platform.colorImageView(binding!!.searchViewIcon, ColorRole.ON_SURFACE_VARIANT)
        util.platform.colorImageView(binding!!.pickContactEmailBtn, ColorRole.ON_SURFACE_VARIANT)
        util.platform.colorCircularProgressBar(binding!!.loadingLayoutIndicator, ColorRole.PRIMARY)
        util.platform.themeHorizontalProgressBar(binding!!.progressBar)
        util.platform.colorViewBackground(binding!!.getRoot())
        util.material.colorMaterialButtonPrimaryOutlined(binding!!.btnShareButton)
        util.notes.themeSearchCardView(binding!!.searchCardWrapper)
    }

    override fun onDestroy() {
        executorService!!.shutdown()
        super.onDestroy()
    }

    override fun shareFileViaPublicShare(note: Note?, password: String?) {
        if (note == null || password == null) {
            Log_OC.d(TAG, "note or password is null, cannot create a public share")
            return
        }

        executorService!!.submit {
            val result: ApiResult<OcsResponse<CreateShareResponse>?> = repository!!.addShare(
                note,
                ShareType.PUBLIC_LINK,
                "",
                "false",
                password,
                repository!!.getCapabilities().defaultPermission
            )
            activity?.runOnUiThread {
                if (result is ApiResult.Success<OcsResponse<CreateShareResponse>?> &&
                    binding!!.sharesList.adapter is ShareeListAdapter
                ) {
                    val adapter = binding!!.sharesList.adapter as ShareeListAdapter
                    adapter.addShare(result.data!!.ocs.data!!.toOCShare())
                } else if (result is ApiResult.Error) {
                    DisplayUtils.showSnackMessage(view, result.message)
                }
            }
        }
    }

    override fun setPasswordToShare(share: OCShare, password: String?) {
        if (password == null) {
            Log_OC.d(TAG, "password is null, cannot update a public share")
            return
        }

        executorService!!.submit {
            run {
                val isDownloadAndAllowsSyncEnabled = repository!!.isAllowDownloadAndSync(share)
                val attributes = createAttributes(
                    repository!!.getCapabilities(),
                    isDownloadAndAllowsSyncEnabled,
                    share.shareType
                )

                val requestBody = UpdateShareRequest(
                    share.permissions,
                    password,
                    share.note,
                    share.label,
                    getExpirationDate(share.expirationDate),
                    share.isHideFileDownload.toString(),
                    attributes
                )
                val result: ApiResult<OcsResponse<CreateShareResponse>?> =
                    repository!!.updateShare(share.id, requestBody)
                activity?.runOnUiThread {
                    if (result.isSuccess()) {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        activity?.startActivity(intent)
                    } else if (result.isError()) {
                        val error = result as ApiResult.Error
                        DisplayUtils.showSnackMessage(view, error.message)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "NoteShareFragment"
        const val ARG_NOTE: String = "NOTE"
        const val ARG_ACCOUNT: String = "ACCOUNT"
        const val FTAG_CHOOSER_DIALOG: String = "CHOOSER_DIALOG"

        private const val SEARCH_DELAY_MS: Long = 500

        fun newInstance(note: Note, account: Account) : NoteShareFragment {
            val f = NoteShareFragment()

            val bundle = Bundle()
            bundle.putSerializable(ARG_NOTE, note)
            bundle.putSerializable(ARG_ACCOUNT, account)

            f.arguments = bundle
            return f
        }
    }
}
