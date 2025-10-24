package it.niedermann.owncloud.notes.util

import androidx.core.app.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.utils.Log_OC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ComponentActivity.ssoAccount(onCompleted: (SingleSignOnAccount?) -> Unit) {
    lifecycleScope.launch(Dispatchers.IO) {
        val result = try {
            val account = SingleAccountHelper.getCurrentSingleSignOnAccount(this@ssoAccount)
            account
        } catch (t: Throwable) {
            Log_OC.e("ComponentActivityExtension", "cant get sso account: $t")
            null
        }
        withContext(Dispatchers.Main) {
            onCompleted(result)
        }
    }
}
