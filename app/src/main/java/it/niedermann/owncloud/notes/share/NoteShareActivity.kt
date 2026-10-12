/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.View.OnFocusChangeListener
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
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.BrandedActivity
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

class NoteShareActivity : BrandedActivity(), ShareeListAdapterListener, NoteShareItemAction,
    QuickPermissionSharingBottomSheetActions, SharePasswordDialogListener {
    private var executorService: ScheduledExecutorService? = null
    private var future: Future<*>? = null
    private var binding: ActivityNoteShareBinding? = null
    private var note: Note? = null
    private var account: Account? = null
    private var repository: ShareRepository? = null
    private var capabilities: Capabilities? = null
    private var resultLauncher: ActivityResultLauncher<Intent?>? = null
    private val shares: MutableList<OCShare> =
        Collections.synchronizedList(ArrayList<OCShare>())

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executorService = Executors.newSingleThreadScheduledExecutor()
        binding = ActivityNoteShareBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        setSupportActionBar(binding!!.toolbar)
        binding!!.toolbar.setNavigationOnClickListener(View.OnClickListener { v: View? -> backPressed() })
        registerResultLauncher()
        initializeArguments()
        initializeOnBackPressedDispatcher()
    }

    private fun registerResultLauncher() {
        resultLauncher = registerForActivityResult<Intent?, ActivityResult?>(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result: ActivityResult? ->
                if (result!!.getResultCode() == RESULT_OK && result.getData() != null) {
                    recreate()
                }
            })
    }

    private fun initializeOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
    }

    private fun backPressed() {
        val intent = Intent(this@NoteShareActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun initializeArguments() {
        val bundler = getIntent().getExtras()
        note = bundler.getSerializableArgument(ARG_NOTE, Note::class.java)
        account = bundler.getSerializableArgument(ARG_ACCOUNT, Account::class.java)

        requireNotNull(note) { "Note cannot be null" }

        requireNotNull(account) { "Account cannot be null" }

        executorService!!.submit(Runnable {
            try {
                val ssoAcc =
                    SingleAccountHelper.getCurrentSingleSignOnAccount(this@NoteShareActivity)
                repository = ShareRepository(this@NoteShareActivity, ssoAcc)
                capabilities = repository!!.getCapabilities()
                repository!!.fetchSharesForNotesAndSaveShareEntities()

                runOnUiThread(Runnable {
                    binding!!.fileName.setText(note!!.getTitle())
                    binding!!.searchContainer.setVisibility(View.VISIBLE)
                    binding!!.sharesList.setVisibility(View.VISIBLE)
                    binding!!.sharesList.setAdapter(
                        ShareeListAdapter(
                            this,
                            ArrayList<OCShare?>(),
                            this,
                            account
                        )
                    )
                    binding!!.sharesList.setLayoutManager(LinearLayoutManager(this))
                    binding!!.pickContactEmailBtn.setOnClickListener(View.OnClickListener { v: View? -> checkContactPermission() })
                    binding!!.btnShareButton.setOnClickListener(View.OnClickListener { v: View? ->
                        ShareUtil.openShareDialog(
                            this,
                            note!!.getTitle(),
                            note!!.getContent()
                        )
                    })

                    if (note!!.getReadonly()) {
                        setupReadOnlySearchView()
                    } else {
                        setupSearchView(
                            getSystemService(SEARCH_SERVICE) as SearchManager?,
                            getComponentName()
                        )
                    }

                    updateShareeListAdapter()
                    binding!!.loadingLayout.setVisibility(View.GONE)
                })
            } catch (e: Exception) {
                Log_OC.e(TAG, "Exception at NoteShareActivity.init: " + e)
            }
        })
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
            for (i in 0..<view.getChildCount()) {
                disableSearchView(view.getChildAt(i))
            }
        }
    }

    private fun setupReadOnlySearchView() {
        binding!!.searchView.setIconifiedByDefault(false)
        binding!!.searchView.setQueryHint(getResources().getString(R.string.note_share_activity_resharing_not_allowed))
        binding!!.searchView.setInputType(InputType.TYPE_NULL)
        binding!!.pickContactEmailBtn.setVisibility(View.GONE)
        disableSearchView(binding!!.searchView)
    }

    private fun setupSearchView(searchManager: SearchManager?, componentName: ComponentName?) {
        if (searchManager == null) {
            binding!!.searchView.setVisibility(View.GONE)
            return
        }

        val suggestionAdapter = SuggestionAdapter(this, null, account!!)
        val provider = UsersAndGroupsSearchProvider(this, repository)

        binding!!.searchView.setSuggestionsAdapter(suggestionAdapter)
        binding!!.searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        binding!!.searchView.setIconifiedByDefault(false)
        binding!!.searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        binding!!.searchView.setQueryHint(getResources().getString(R.string.note_share_activity_search_text))
        binding!!.searchView.setInputType(InputType.TYPE_CLASS_TEXT)
        binding!!.searchView.setFocusable(true)
        binding!!.searchView.setFocusableInTouchMode(true)

        val closeButton =
            binding!!.searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener(View.OnClickListener { v: View? ->
            binding!!.progressBar.setVisibility(View.GONE)
            binding!!.searchView.setQuery("", false)
        })

        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // return true to prevent the query from being processed;
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (!newText.isEmpty()) {
                    binding!!.progressBar.setVisibility(View.VISIBLE)

                    // Cancel the previous task if it's still running
                    if (future != null && !future!!.isDone()) {
                        future!!.cancel(true)
                    }

                    // Schedule a new task with a delay
                    future = executorService!!.schedule(Runnable schedule@{
                        if (capabilities == null) {
                            Log_OC.d(TAG, "Capabilities cannot be null")
                            return@schedule
                        }
                        val isFederationShareAllowed = capabilities!!.getFederationShare()
                        try {
                            val cursor =
                                provider.searchForUsersOrGroups(newText, isFederationShareAllowed)

                            if (cursor == null || cursor.getCount() == 0) {
                                return@schedule
                            }

                            runOnUiThread(Runnable {
                                if (binding!!.searchView.getVisibility() == View.VISIBLE) {
                                    suggestionAdapter.swapCursor(cursor)
                                }
                                binding!!.progressBar.setVisibility(View.GONE)
                            })
                        } catch (e: Exception) {
                            Log_OC.d(TAG, "Exception setupSearchView.onQueryTextChange: " + e)
                            runOnUiThread(Runnable { binding!!.progressBar.setVisibility(View.GONE) })
                        }
                    }, SEARCH_DELAY_MS, TimeUnit.MILLISECONDS)
                } else {
                    binding!!.progressBar.setVisibility(View.GONE)
                    suggestionAdapter.swapCursor(null)
                }
                return false
            }
        })

        binding!!.searchView.setOnQueryTextFocusChangeListener(OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    binding!!.searchView.findFocus(),
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        })

        binding!!.searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = suggestionAdapter.getCursor()
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

        val intent = Intent(this, NoteShareDetailActivity::class.java)
        intent.putExtras(bundle)
        resultLauncher!!.launch(intent)
    }

    private fun accountOwnsFile(): Boolean {
        if (shares.isEmpty()) {
            return true
        }

        val share = shares.get(0)
        val ownerDisplayName = share.ownerDisplayName
        return TextUtils.isEmpty(ownerDisplayName) || account!!.getAccountName()
            .split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].equals(
            ownerDisplayName,
            ignoreCase = true
        )
    }

    private fun setShareWithYou() {
        if (accountOwnsFile()) {
            binding!!.sharedWithYouContainer.setVisibility(View.GONE)
        } else {
            if (shares.isEmpty()) {
                return
            }

            val share = shares.get(0)

            binding!!.sharedWithYouUsername.setText(
                String.format(
                    getString(R.string.note_share_activity_shared_with_you),
                    share.ownerDisplayName
                )
            )
            AvatarLoader.load(this, binding!!.sharedWithYouAvatar, account!!)
            binding!!.sharedWithYouAvatar.setVisibility(View.VISIBLE)

            val description = share.note

            if (!TextUtils.isEmpty(description)) {
                binding!!.sharedWithYouNote.setText(description)
                binding!!.sharedWithYouNoteContainer.setVisibility(View.VISIBLE)
            } else {
                binding!!.sharedWithYouNoteContainer.setVisibility(View.GONE)
            }
        }
    }

    override fun copyInternalLink() {
        if (account == null) {
            DisplayUtils.showSnackMessage(
                this,
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

        if (capabilities!!.getPublicPasswordEnforced() || capabilities!!.getAskForOptionalPassword()) {
            // password enforced by server, request to the user before trying to create
            requestPasswordForShareViaLink(true, capabilities!!.getAskForOptionalPassword())
        } else {
            executorService!!.submit(Runnable {
                val result: ApiResult<OcsResponse<CreateShareResponse>?> =
                    repository!!.addShare(note!!, ShareType.PUBLIC_LINK, "")
                runOnUiThread(Runnable {
                    if (result is ApiResult.Success<OcsResponse<CreateShareResponse>?> && binding!!.sharesList.getAdapter() is ShareeListAdapter) {
                        DisplayUtils.showSnackMessage(this@NoteShareActivity, result.message)
                        note!!.setIsShared(true)
                        repository!!.updateNote(note!!)
                        val adapter = binding!!.sharesList.getAdapter() as ShareeListAdapter
                        adapter.addShare(result.data!!.ocs.data!!.toOCShare())
                    } else if (result is ApiResult.Error) {
                        DisplayUtils.showSnackMessage(this@NoteShareActivity, result.message)
                    }
                })
            })
        }
    }

    fun requestPasswordForShareViaLink(createShare: Boolean, askForPassword: Boolean) {
        val dialog = newInstance(note, createShare, askForPassword, this)
        dialog.show(getSupportFragmentManager(), SharePasswordDialogFragment.PASSWORD_FRAGMENT)
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
            getString(R.string.note_share_activity_subject_shared_with_you, note!!.getTitle())
        )

        val packagesToExclude = arrayOf<String?>(this.getPackageName())
        val chooserDialog: DialogFragment = newInstance(intentToShareLink, *packagesToExclude)
        chooserDialog.show(getSupportFragmentManager(), FTAG_CHOOSER_DIALOG)
    }

    private fun createInternalLink(): String {
        val baseUri = Uri.parse(account!!.getUrl())
        return baseUri.toString() + "/index.php/f/" + note!!.getRemoteId()
    }

    override fun copyLink(share: OCShare) {
        if (!note!!.isShared()) {
            return
        }

        if (TextUtils.isEmpty(share.shareLink)) {
            copyAndShareFileLink(share.shareLink)
        } else {
            copyToClipboard(this, share.shareLink)
        }
    }

    private fun copyAndShareFileLink(link: String?) {
        copyToClipboard(this, link, false)
        val snackbar = Snackbar
            .make(
                this.findViewById<View?>(android.R.id.content)!!,
                R.string.clipboard_text_copied,
                Snackbar.LENGTH_LONG
            )
            .setAction(
                R.string.share,
                View.OnClickListener { v: View? -> showShareLinkDialog(link) })
        snackbar.show()
    }

    override fun showSharingMenuActionSheet(share: OCShare?) {
        if (!this.isFinishing()) {
            NoteShareActivityShareItemActionBottomSheetDialog(this, this, share).show()
        }
    }

    override fun showShareExpirationSnackbar(share: OCShare) {
        val expirationDescription = getString(
            R.string.share_expires,
            SimpleDateFormat.getDateInstance().format(Date(share.expirationDate))
        )
        val snackbar = Snackbar
            .make(binding!!.getRoot(), expirationDescription, Snackbar.LENGTH_LONG)
            .setAction(R.string.dismiss, View.OnClickListener { view: View? -> })
        val util = BrandingUtil.of(BrandingUtil.readBrandMainColor(this), this)
        util.material.themeSnackbar(snackbar)
        snackbar.show()
    }

    override fun showPermissionsDialog(share: OCShare?) {
        QuickSharingPermissionsBottomSheetDialog(this, this, share).show()
    }

    override fun requestPasswordForShare(share: OCShare?, askForPassword: Boolean) {
        val dialog = newInstance(share, askForPassword, this)
        dialog.show(getSupportFragmentManager(), SharePasswordDialogFragment.PASSWORD_FRAGMENT)
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
                val adapter = binding!!.sharesList.getAdapter() as ShareeListAdapter?

                if (adapter == null) {
                    runOnUiThread(Runnable {
                        DisplayUtils.showSnackMessage(
                            this@NoteShareActivity,
                            getString(R.string.could_not_retrieve_shares)
                        )
                    })
                    isInitializingShares = false
                    return@submit
                }

                val remoteNotes: List<OCShare>
                if (note != null) {
                    remoteNotes = repository!!.fetchSharesFromNote(note!!)
                } else {
                    remoteNotes = listOf()
                }

                runOnUiThread(Runnable {
                    shares.clear()
                    shares.addAll(remoteNotes)

                    adapter.removeAll()
                    adapter.addShares(shares)
                    addPublicShares(adapter)
                    setShareWithYou()
                    isInitializingShares = false
                })
            } catch (e: Exception) {
                Log_OC.d(TAG, "Exception while updateShareeListAdapter: " + e)
            }
        })
    }

    private fun addPublicShares(adapter: ShareeListAdapter) {
        val publicShares: MutableList<OCShare> = ArrayList<OCShare>()

        if (containsNoNewPublicShare(adapter.getShares())) {
            val ocShare = OCShare()
            ocShare.shareType = ShareType.NEW_PUBLIC_LINK
            publicShares.add(ocShare)
        } else {
            adapter.removeNewPublicShare()
        }

        adapter.addShares(publicShares)
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            pickContactEmail()
        } else {
            requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun pickContactEmail() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI)

        // FIXME:
        if (intent.resolveActivity(getPackageManager()) != null) {
            onContactSelectionResultLauncher.launch(intent)
        } else {
            DisplayUtils.showSnackMessage(
                this,
                getString(R.string.file_detail_sharing_fragment_no_contact_app_message)
            )
        }
    }

    private fun handleContactResult(contactUri: Uri) {
        // Define the projection to get all email addresses.
        val projection = arrayOf<String?>(ContactsContract.CommonDataKinds.Email.ADDRESS)

        val cursor = getContentResolver().query(contactUri, projection, null, null, null)
        if (cursor == null) {
            DisplayUtils.showSnackMessage(this, getString(R.string.email_pick_failed))
            Log_OC.e(TAG, "Failed to pick email address as Cursor is null.")
            return
        }

        if (!cursor.moveToFirst()) {
            DisplayUtils.showSnackMessage(this, getString(R.string.email_pick_failed))
            Log_OC.e(TAG, "Failed to pick email address as no Email found.")
            return
        }

        // The contact has only one email address, use it.
        val columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
        if (columnIndex == -1) {
            DisplayUtils.showSnackMessage(this, getString(R.string.email_pick_failed))
            Log_OC.e(TAG, "Failed to pick email address.")
            return
        }

        // Use the email address as needed.
        // email variable contains the selected contact's email address.
        val email = cursor.getString(columnIndex)
        binding!!.searchView.post(Runnable {
            binding!!.searchView.setQuery(email, false)
            binding!!.searchView.requestFocus()
        })

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

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ARG_NOTE, note)
        outState.putSerializable(ARG_ACCOUNT, account)
    }

    private fun isReshareForbidden(share: OCShare): Boolean {
        return (share.shareType != null && ShareType.FEDERATED == share.shareType) ||
                capabilities != null && !capabilities!!.isReSharingAllowed()
    }

    @VisibleForTesting
    fun search(query: String?) {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setQuery(query, true)
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
            runOnUiThread(Runnable {
                if (result) {
                    val adapter = binding!!.sharesList.getAdapter() as ShareeListAdapter?
                    if (adapter == null) {
                        DisplayUtils.showSnackMessage(
                            this@NoteShareActivity,
                            getString(R.string.email_pick_failed)
                        )
                        return@Runnable
                    }
                    adapter.remove(share)
                } else {
                    DisplayUtils.showSnackMessage(
                        this@NoteShareActivity,
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

        val intent = Intent(this, NoteShareDetailActivity::class.java)
        intent.putExtras(bundle)
        resultLauncher!!.launch(intent)
    }

    private val expDateShown: Boolean
        get() {
            try {
                if (capabilities == null) {
                    Log_OC.d(TAG, "Capabilities cannot be null")
                    return false
                }

                val majorVersionAsString = capabilities!!.getNextcloudMajorVersion()
                if (majorVersionAsString != null) {
                    val majorVersion = majorVersionAsString.toInt()
                    return majorVersion >= 18
                }

                return false
            } catch (e: NumberFormatException) {
                Log_OC.d(TAG, "Exception while getting expDateShown")
                return false
            }
        }

    override fun onQuickPermissionChanged(share: OCShare, permission: Int) {
        executorService!!.submit(Runnable {
            val result: ApiResult<OcsResponse<CreateShareResponse>?> =
                repository!!.updateSharePermission(share.id, permission)
            runOnUiThread(Runnable {
                if (result.isSuccess<OcsResponse<CreateShareResponse>?>()) {
                    updateShare(share)
                } else if (result is ApiResult.Error) {
                    DisplayUtils.showSnackMessage(this@NoteShareActivity, result.message)
                }
            })
        })
    }

    private fun updateShare(share: OCShare) {
        if (note == null) {
            Log_OC.e(TAG, "note is null, cannot update share")
            return
        }

        executorService!!.submit(Runnable {
            try {
                val updatedShares: List<OCShare?> = repository!!.fetchSharesFromNote(note!!)

                runOnUiThread(Runnable {
                    if (binding!!.sharesList.getAdapter() is ShareeListAdapter) {
                        var updatedShare: OCShare? = null
                        for (i in updatedShares.indices) {
                            if (updatedShares.get(i)!!.id == share.id) {
                                updatedShare = updatedShares.get(i)
                                break
                            }
                        }

                        if (updatedShare == null) {
                            return@Runnable
                        }

                        val adapter = binding!!.sharesList.getAdapter() as ShareeListAdapter
                        adapter.updateShare(updatedShare)
                    }
                })
            } catch (e: Exception) {
                Log_OC.d(TAG, "Exception while refreshing share info: " + e)
            }
        })
    }

    private val requestContactPermissionLauncher = registerForActivityResult<String?, Boolean?>(
        ActivityResultContracts.RequestPermission(), ActivityResultCallback { isGranted: Boolean? ->
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
        })

    private val onContactSelectionResultLauncher =
        registerForActivityResult<Intent?, ActivityResult?>(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result: ActivityResult? ->
                if (result!!.getResultCode() == RESULT_OK) {
                    val intent = result.getData()
                    if (intent == null) {
                        BrandedSnackbar.make(
                            binding!!.getRoot(),
                            getString(R.string.email_pick_failed),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        return@ActivityResultCallback
                    }

                    val contactUri = intent.getData()
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
        val util = BrandingUtil.of(color, this)
        util.platform.themeStatusBar(this)
        util.material.themeToolbar(binding!!.toolbar)
        util.notes.themeContentSearchView(binding!!.searchView)
        util.platform.colorImageView(binding!!.searchViewIcon, ColorRole.ON_SURFACE_VARIANT)
        util.platform.colorImageView(binding!!.pickContactEmailBtn, ColorRole.ON_SURFACE_VARIANT)
        util.platform.colorCircularProgressBar(binding!!.loadingLayoutIndicator, ColorRole.PRIMARY)
        util.platform.themeHorizontalProgressBar(binding!!.progressBar)
        util.platform.colorViewBackground(getWindow().getDecorView())
        util.platform.colorViewBackground(binding!!.getRoot())
        util.material.colorMaterialButtonPrimaryOutlined(binding!!.btnShareButton)
        util.platform.colorTextView(binding!!.title, ColorRole.ON_SURFACE)
        util.platform.colorTextView(binding!!.fileName, ColorRole.ON_SURFACE_VARIANT)
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

        executorService!!.submit(Runnable {
            val result: ApiResult<OcsResponse<CreateShareResponse>?> = repository!!.addShare(
                note,
                ShareType.PUBLIC_LINK,
                "",
                "false",
                password,
                repository!!.getCapabilities().getDefaultPermission()
            )
            runOnUiThread(Runnable {
                if (result is ApiResult.Success<OcsResponse<CreateShareResponse>?> &&
                    binding!!.sharesList.getAdapter() is ShareeListAdapter
                ) {
                    val adapter = binding!!.sharesList.getAdapter() as ShareeListAdapter
                    adapter.addShare(result.data!!.ocs.data!!.toOCShare())
                } else if (result is ApiResult.Error) {
                    DisplayUtils.showSnackMessage(this@NoteShareActivity, result.message)
                }
            })
        })
    }

    override fun setPasswordToShare(share: OCShare, password: String?) {
        if (password == null) {
            Log_OC.d(TAG, "password is null, cannot update a public share")
            return
        }

        executorService!!.submit(Runnable {
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
                runOnUiThread(Runnable {
                    if (result.isSuccess<OcsResponse<CreateShareResponse>?>()) {
                        val intent = Intent(this@NoteShareActivity, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        this@NoteShareActivity.startActivity(intent)
                    } else if (result.isError<OcsResponse<CreateShareResponse>?>()) {
                        val error = result as ApiResult.Error
                        DisplayUtils.showSnackMessage(this@NoteShareActivity, error.message)
                    }
                })
            }
        })
    }

    companion object {
        private const val TAG = "NoteShareActivity"
        const val ARG_NOTE: String = "NOTE"
        const val ARG_ACCOUNT: String = "ACCOUNT"
        const val FTAG_CHOOSER_DIALOG: String = "CHOOSER_DIALOG"

        private const val SEARCH_DELAY_MS: Long = 500

        @JvmStatic
        fun getActivityStartIntent(note: Note, account: Account, activity: Activity): Intent {
            val bundle = Bundle()
            bundle.putSerializable(ARG_NOTE, note)
            bundle.putSerializable(ARG_ACCOUNT, account)

            val intent = Intent(activity, NoteShareActivity::class.java)
            intent.putExtras(bundle)
            return intent
        }
    }
}
