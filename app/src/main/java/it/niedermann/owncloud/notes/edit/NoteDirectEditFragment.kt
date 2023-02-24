package it.niedermann.owncloud.notes.edit

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import androidx.core.view.isVisible
import com.nextcloud.android.sso.helper.SingleAccountHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import it.niedermann.owncloud.notes.BuildConfig
import it.niedermann.owncloud.notes.databinding.FragmentNoteDirectEditBinding
import it.niedermann.owncloud.notes.persistence.DirectEditingRepository
import it.niedermann.owncloud.notes.persistence.entity.Note
import it.niedermann.owncloud.notes.shared.model.ISyncCallback

class NoteDirectEditFragment : BaseNoteFragment() {
    private var _binding: FragmentNoteDirectEditBinding? = null
    private val binding: FragmentNoteDirectEditBinding
        get() = _binding!!

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
        // TODO prepare webview
        setupWebSettings(binding.noteWebview.settings)
        // TODO remove this
        binding.noteWebview.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                handler?.proceed()
            }
        }
        binding.noteWebview.addJavascriptInterface(
            DirectEditingMobileInterface(this),
            "DirectEditingMobileInterface",
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.noteWebview.destroy()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener.setToolbarVisibility(false)
    }

    override fun onNoteLoaded(note: Note) {
        super.onNoteLoaded(note)
        Log.d(TAG, "onNoteLoaded() called with: note = $note")
        // TODO get url and open in webview
        val appContext = requireActivity().applicationContext
        val repo = DirectEditingRepository.getInstance(appContext)
        val account = SingleAccountHelper.getCurrentSingleSignOnAccount(appContext)
        val disposable = repo.getDirectEditingUrl(account, note)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ url ->
                if (BuildConfig.DEBUG) {
                    // TODO don't print url (security risk)
                    Log.d(TAG, "onNoteLoaded: url = $url")
                }
                binding.noteWebview.loadUrl(url)
            }, { throwable ->
                // TODO handle error
                Log.e(TAG, "onNoteLoaded:", throwable)
            })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebSettings(webSettings: WebSettings) {
        WebView.setWebContentsDebuggingEnabled(true)
        // enable zoom
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Non-responsive webs are zoomed out when loaded
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        // user agent
        webSettings.setUserAgentString("Mozilla/5.0 (Android) Nextcloud-android/3.23.0")

        // no private data storing
        webSettings.savePassword = false
        webSettings.saveFormData = false

        // disable local file access
        webSettings.allowFileAccess = false

        // enable javascript
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // caching disabled in debug mode
        if (requireActivity().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE !== 0) {
            binding.noteWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE)
        }
    }

    /**
     * Gets the current content of the EditText field in the UI.
     *
     * @return String of the current content.
     */
    override fun getContent(): String {
        // TODO what to do here?
        return ""
    }

    override fun saveNote(callback: ISyncCallback?) {
        // nothing, editor autosaves
    }

    override fun onCloseNote() {
        // nothing!
        // TODO sync note with server
    }

    override fun applyBrand(color: Int) {
        // TODO check if any branding needed
        // nothing for now
    }

    companion object {
        private const val TAG = "NoteDirectEditFragment"

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

    private class DirectEditingMobileInterface(val noteDirectEditFragment: NoteDirectEditFragment) {
        @JavascriptInterface
        fun close() {
            Log.d(TAG, "close() called")
            // TODO callback interface or make this class anonymous
            noteDirectEditFragment.close()
        }

        @JavascriptInterface
        fun share() {
            // TODO share note
//            openShareDialog()
        }

        @JavascriptInterface
        fun loaded() {
            noteDirectEditFragment.onLoaded()
        }
    }

    private fun close() {
        listener.close()
    }

    private fun onLoaded() {
        Log.d(TAG, "onLoaded: note loaded")
        activity?.runOnUiThread {
            binding.progress.isVisible = false
            binding.noteWebview.isVisible = true
        }
    }
}
