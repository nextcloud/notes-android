<!--
 ~ SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-FileCopyrightText: 2020-2024 Stefan Niedermann <info@niedermann.it>
 ~ SPDX-License-Identifier: GPL-3.0-or-later
-->
# Nextcloud Notes Android Privacy Policy

The "Nextcloud Notes Android" Android-App (in the following referred to as "App") does not collect or send any data from you or your device to a server of the developers or the [Nextcloud GmbH](https://nextcloud.com/). The App sends all data exclusively to the server configured by you with the intention to synchronize the contents of the App with those of the server. This data can contain IP-addresses, timestamps and further information as meta data.
It is important to mention that all contents of the App may also be transmitted to the configured server. This contents can also contain personal information depending on the use. The servers you configured are technically outside the access area of this App developers, so that we neither know nor can prevent what happens to your data there. Please consult the privacy policy of the respective server operator.

The license of this project allows you to verify that no data is collected by the creators by reading the source code or asking someone else to do it.

## Permissions

This is a list of permissions required and asked by the App in order to properly work on your device:

- `android.permission.INTERNET`

  Used by [Nextcloud Single Sign On library](https://github.com/nextcloud/Android-SingleSignOn/) to communicate with your Nextcloud instance and synchronize contents.

- `android.permission.ACCESS_NETWORK_STATE`

  Used to provide offline support and make the "Sync only on Wi-Fi" option possible.

- `android.permission.GET_ACCOUNTS`

  Used by [Nextcloud Single Sign On library](https://github.com/nextcloud/Android-SingleSignOn/) to read available accounts to import.

- `android.permission.WAKE_LOCK`

  Used by [AndroidX WorkManager](https://developer.android.com/jetpack/androidx/releases/work) for background synchronization.

- `android.permission.RECEIVE_BOOT_COMPLETED`

  Used by [AndroidX WorkManager](https://developer.android.com/jetpack/androidx/releases/work) for background synchronization.

- `android.permission.FOREGROUND_SERVICE`

  Used by [AndroidX WorkManager](https://developer.android.com/jetpack/androidx/releases/work) for background synchronization.

## Nextcloud privacy policy

You can get more information on Nextcloud general privacy policy which is accessible at [nextcloud.com/privacy](https://nextcloud.com/privacy/).