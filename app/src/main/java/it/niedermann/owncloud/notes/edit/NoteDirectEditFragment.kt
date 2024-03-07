/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.edit

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.nextcloud.android.common.ui.theme.utils.ColorRole
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import it.niedermann.owncloud.notes.BuildConfig
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.branding.Branded
import it.niedermann.owncloud.notes.branding.BrandedSnackbar
import it.niedermann.owncloud.notes.branding.BrandingUtil
import it.niedermann.owncloud.notes.databinding.FragmentNoteDirectEditBinding
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.persistence.DirectEditingRepository
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import it.niedermann.owncloud.notes.shared.model.ISyncCallback
import it.niedermann.owncloud.notes.shared.util.ExtendedFabUtil
import it.niedermann.owncloud.notes.shared.util.rx.DisposableSet
import java.util.concurrent.TimeUnit

class NoteDirectEditFragment : BaseNoteFragment(), Branded {
    private var _binding: FragmentNoteDirectEditBinding? = null
    private val binding: FragmentNoteDirectEditBinding
        get() = _binding!!

    private val disposables: DisposableSet = DisposableSet()
    private var switchToEditPending = false

    val account: SingleSignOnAccount by lazy {
        SingleAccountHelper.getCurrentSingleSignOnAccount(
            requireContext(),
        )
    }

    val notesApi: NotesAPI by lazy {
        ApiProvider.getInstance().getNotesAPI(requireContext(), account, ApiVersion.API_VERSION_1_0)
    }

    // for hiding / showing the fab
    private var scrollStart: Int = 0

    public override fun getScrollView(): ScrollView? {
        return null
    }

    override fun scrollToY(y: Int) {
        // do nothing
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentNoteDirectEditBinding.inflate(inflater, container, false)
        setupFab()
        prepareWebView()
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility") // touch listener only for UI purposes, no need to handle click
    private fun setupFab() {
        binding.plainEditingFab.isExtended = false
        ExtendedFabUtil.toggleExtendedOnLongClick(binding.plainEditingFab)
        // manually detect scroll as we can't get it from the webview (maybe with custom JS?)
        binding.noteWebview.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    scrollStart = event.y.toInt()
                }
                MotionEvent.ACTION_UP -> {
                    val scrollEnd = event.y.toInt()
                    ExtendedFabUtil.toggleVisibilityOnScroll(
                        binding.plainEditingFab,
                        scrollStart,
                        scrollEnd,
                    )
                }
            }
            return@setOnTouchListener false
        }
        binding.plainEditingFab.setOnClickListener { switchToPlainEdit() }
    }

    private fun switchToPlainEdit() {
        switchToEditPending = true
        binding.noteWebview.evaluateJavascript(JS_CLOSE) { result ->
            val resultWithoutQuotes = result.replace("\"", "")
            if (resultWithoutQuotes != JS_RESULT_OK) {
                Log.w(TAG, "Closing via JS failed: $resultWithoutQuotes")
                changeToEditMode()
            }
            // if result is OK, switch will be handled by JS interface callback
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
        binding.noteWebview.destroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val timeoutDisposable = Single.just(Unit)
            .delay(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .map {
                if (!binding.noteWebview.isVisible) {
                    Log.w(TAG, "Editor not loaded after $LOAD_TIMEOUT_SECONDS seconds")
                    handleLoadError()
                }
            }.subscribe()
        disposables.add(timeoutDisposable)
    }

    override fun onNoteLoaded(note: Note) {
        super.onNoteLoaded(note)
        Log.d(TAG, "onNoteLoaded() called")
        val newNoteParam = arguments?.getSerializable(PARAM_NEWNOTE) as Note?
        if (newNoteParam != null || note.remoteId == null) {
            createAndLoadNote(note)
        } else {
            loadNoteInWebView(note)
        }
    }

    private fun createAndLoadNote(newNote: Note) {
        Log.d(TAG, "createAndLoadNote() called")
        val noteCreateDisposable = Single
            .fromCallable {
                notesApi.createNote(newNote).execute().body()!!
            }
            .map { createdNote ->
                repo.updateRemoteId(newNote.id, createdNote.remoteId)
                repo.getNoteById(newNote.id)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ createdNote ->
                loadNoteInWebView(createdNote)
            }, { throwable ->
                note = null
                handleLoadError()
                Log.e(TAG, "createAndLoadNote:", throwable)
            })
        disposables.add(noteCreateDisposable)
    }

    private fun loadNoteInWebView(note: Note) {
        Log.d(TAG, "loadNoteInWebView() called")
        val directEditingRepository =
            DirectEditingRepository.getInstance(requireContext().applicationContext)
        val urlDisposable = directEditingRepository.getDirectEditingUrl(account, note)
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ url ->
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "loadNoteInWebView: url = $url")
                }
                binding.noteWebview.loadUrl(url)
            }, { throwable ->
                handleLoadError()
                Log.e(TAG, "loadNoteInWebView:", throwable)
            })
        disposables.add(urlDisposable)
    }

    private fun handleLoadError() {
        val snackbar = BrandedSnackbar.make(
            binding.plainEditingFab,
            getString(R.string.direct_editing_error),
            Snackbar.LENGTH_INDEFINITE,
        )
        if (note != null) {
            snackbar.setAction(R.string.switch_to_plain_editing) {
                changeToEditMode()
            }
        } else {
            snackbar.setAction(R.string.action_back) {
                close()
            }
        }
        snackbar.show()
    }

    override fun shouldShowToolbar(): Boolean = false

    @SuppressLint("SetJavaScriptEnabled")
    private fun prepareWebView() {
        val webSettings = binding.noteWebview.settings
        // enable zoom
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Non-responsive webs are zoomed out when loaded
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        // user agent
        val userAgent =
            getString(R.string.user_agent, getString(R.string.app_name), BuildConfig.VERSION_NAME)
        webSettings.userAgentString = userAgent

        // no private data storing
        webSettings.savePassword = false
        webSettings.saveFormData = false

        // disable local file access
        webSettings.allowFileAccess = false

        // enable javascript
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        if (BuildConfig.DEBUG) {
            // caching disabled in debug mode
            binding.noteWebview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }

        binding.noteWebview.addJavascriptInterface(
            DirectEditingMobileInterface(this),
            JS_INTERFACE_NAME,
        )

        binding.noteWebview.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    handleLoadError()
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError") // only for debug mode
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                if (BuildConfig.DEBUG) {
                    handler?.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }
        }
    }

    /**
     * Gets the current content of the EditText field in the UI.
     *
     * @return String of the current content.
     */
    override fun getContent(): String {
        // no way to get content from webview
        return ""
    }

    override fun saveNote(callback: ISyncCallback?) {
        val acc = repo.getAccountByName(account.name)
        repo.scheduleSync(acc, false)
    }

    override fun onCloseNote() {
        saveNote(null)
    }

    override fun applyBrand(color: Int) {
        val util = BrandingUtil.of(color, requireContext())
        util.material.themeExtendedFAB(binding.plainEditingFab)
        util.platform.colorCircularProgressBar(binding.progress, ColorRole.PRIMARY)
    }

    private class DirectEditingMobileInterface(val noteDirectEditFragment: NoteDirectEditFragment) {
        @JavascriptInterface
        fun close() {
            noteDirectEditFragment.close()
        }

        @JavascriptInterface
        fun share() {
            noteDirectEditFragment.share()
        }

        @JavascriptInterface
        fun loaded() {
            noteDirectEditFragment.onLoaded()
        }
    }

    private fun close() {
        if (switchToEditPending) {
            Log.d(TAG, "close: switching to plain edit")
            changeToEditMode()
        } else {
            Log.d(TAG, "close: closing")
            listener?.close()
        }
    }

    private fun changeToEditMode() {
        toggleLoadingUI(true)
        val updateDisposable = Single.just(note.remoteId)
            .map { remoteId ->
                val newNote = notesApi.getNote(remoteId).singleOrError().blockingGet().response
                val localAccount = repo.getAccountByName(account.name)
                repo.updateNoteAndSync(localAccount, note, newNote.content, newNote.title, null)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                listener?.changeMode(NoteFragmentListener.Mode.EDIT, true)
            }, { throwable ->
                Log.e(TAG, "changeToEditMode: ", throwable)
                listener?.changeMode(NoteFragmentListener.Mode.EDIT, true)
            })
        disposables.add(updateDisposable)
    }

    private fun share() {
        super.shareNote()
    }

    private fun onLoaded() {
        Log.d(TAG, "onLoaded: note loaded")
        toggleLoadingUI(false)
    }

    private fun toggleLoadingUI(loading: Boolean) {
        activity?.runOnUiThread {
            binding.progress.isVisible = loading
            binding.noteWebview.isVisible = !loading
            binding.plainEditingFab.isVisible = !loading
        }
    }

    companion object {
        private const val TAG = "NoteDirectEditFragment"
        private const val LOAD_TIMEOUT_SECONDS = 10L
        private const val JS_INTERFACE_NAME = "DirectEditingMobileInterface"
        private const val JS_RESULT_OK = "ok"

        // language=js
        private val JS_CLOSE = """
            (function () {
              var closeIcons = document.getElementsByClassName("icon-close");
              if (closeIcons.length > 0) {
                closeIcons[0].click();
              } else {
                return "close button not available";
              }
              return "$JS_RESULT_OK";
            })();
        """.trimIndent()

        @JvmStatic
        fun newInstance(accountId: Long, noteId: Long): BaseNoteFragment {
            val fragment = NoteDirectEditFragment()
            val args = Bundle()
            args.putLong(PARAM_NOTE_ID, noteId)
            args.putLong(PARAM_ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstanceWithNewNote(newNote: Note?): BaseNoteFragment {
            val fragment = NoteDirectEditFragment()
            val args = Bundle()
            args.putSerializable(PARAM_NEWNOTE, newNote)
            fragment.arguments = args
            return fragment
        }
    }
}
