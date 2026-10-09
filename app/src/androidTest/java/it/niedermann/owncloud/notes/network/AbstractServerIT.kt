/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.network

import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.OwnCloudBasicCredentials
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import okhttp3.Credentials
import org.junit.After
import org.junit.BeforeClass

/**
 * Common base for instrumented tests that talk to a real Nextcloud server, configured via the
 * TEST_SERVER_* instrumentation arguments (see app/build.gradle).
 * ATTENTION: deletes ALL files of the test user on the server after each test, so you MUST point
 * this at a dedicated test account.
 */
abstract class AbstractServerIT {
    @After
    fun removeAllRemoteFiles() {
        val result = ReadFolderRemoteOperation(ROOT_PATH).execute(client)
        if (!result.isSuccess) {
            return
        }

        result.data
            .filterIsInstance<RemoteFile>()
            .filterNot { it.remotePath == ROOT_PATH }
            .forEach { RemoveFileRemoteOperation(it.remotePath).execute(client) }
    }

    companion object {
        private const val ROOT_PATH = "/"
        private const val FOLLOW_REDIRECTS = true
        private const val ARG_TEST_SERVER_URL = "TEST_SERVER_URL"
        private const val ARG_TEST_SERVER_USERNAME = "TEST_SERVER_USERNAME"
        private const val ARG_TEST_SERVER_PASSWORD = "TEST_SERVER_PASSWORD"

        lateinit var context: Context
        lateinit var client: OwnCloudClient
        lateinit var nextcloudClient: NextcloudClient

        @JvmStatic
        @BeforeClass
        fun connectToTestServer() {
            context = InstrumentationRegistry.getInstrumentation().targetContext
            val arguments = InstrumentationRegistry.getArguments()
            val baseUrl = Uri.parse(requireNotNull(arguments.getString(ARG_TEST_SERVER_URL)))
            val username = requireNotNull(arguments.getString(ARG_TEST_SERVER_USERNAME))
            val password = requireNotNull(arguments.getString(ARG_TEST_SERVER_PASSWORD))

            client = OwnCloudClientFactory.createOwnCloudClient(baseUrl, context, FOLLOW_REDIRECTS).apply {
                credentials = OwnCloudBasicCredentials(username, password)
                userId = username
            }

            nextcloudClient = OwnCloudClientFactory.createNextcloudClient(
                baseUrl,
                username,
                Credentials.basic(username, password),
                context,
                FOLLOW_REDIRECTS
            )
        }
    }
}
