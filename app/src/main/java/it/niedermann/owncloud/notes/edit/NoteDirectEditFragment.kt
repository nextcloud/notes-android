package it.niedermann.owncloud.notes.edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.widget.ScrollView
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
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
import it.niedermann.owncloud.notes.shared.model.ApiVersion
import it.niedermann.owncloud.notes.shared.model.ISyncCallback
import it.niedermann.owncloud.notes.shared.util.ExtendedFabUtil
import it.niedermann.owncloud.notes.shared.util.rx.DisposableSet

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

    override fun onNoteLoaded(note: Note) {
        super.onNoteLoaded(note)
        val directEditingRepository =
            DirectEditingRepository.getInstance(requireContext().applicationContext)
        val urlDisposable =
            directEditingRepository.getDirectEditingUrl(account, note)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ url ->
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onNoteLoaded: url = $url")
                    }
                    // TODO handle error
                    // TODO show warn/error if not loaded after 10 seconds
                    binding.noteWebview.loadUrl(url)
                }, { throwable ->
                    handleLoadError()
                    Log.e(TAG, "onNoteLoaded:", throwable)
                })
        disposables.add(urlDisposable)
    }

    private fun handleLoadError() {
        BrandedSnackbar.make(
            binding.plainEditingFab,
            getString(R.string.direct_editing_error),
            Snackbar.LENGTH_INDEFINITE,
        ).setAction(R.string.switch_to_plain_editing) {
            changeToEditMode()
        }.show()
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
        // TODO change useragent?
        webSettings.userAgentString = "Mozilla/5.0 (Android) Nextcloud-android/3.23.0"

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
        val notesAPI = ApiProvider.getInstance()
            .getNotesAPI(requireContext(), account, ApiVersion.API_VERSION_1_0)
        // TODO clean this up a bit
        val updateDisposable = Single.just(note.remoteId)
            .map { remoteId ->
                val newNote = notesAPI.getNote(remoteId).singleOrError().blockingGet().response
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
    }
}
