<!--
 ~ SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-FileCopyrightText: 2020-2024 Stefan Niedermann <info@niedermann.it>
 ~ SPDX-License-Identifier: GPL-3.0-or-later
-->
# Frequently asked questions

- [Why aren't there any buttons to apply formatting?](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#why-arent-there-any-buttons-to-apply-formatting)
- [I have experienced an error](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#i-have-experienced-an-error)
  - [`NextcloudApiNotRespondingException`](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#nextcloudapinotrespondingexception)
  - [`UnknownErrorException: Read timed out`](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#unknownerrorexception-read-timed-out)
  - [`NextcloudHttpRequestFailedException`](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#nextcloudhttprequestfailedexception)
  - [`IllegalStateException: Duplicate key`](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#illegalstateexception-duplicate-key)
  - [`NextcloudFilesAppAccountNotFoundException`](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#nextcloudfilesappaccountnotfoundexception)
  - [`TokenMismatchException`](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#tokenmismatchexception)
  - [Workarounds](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#workarounds)
- [How to share notes?](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#how-to-share-notes)
- [Why don't you make an option for…?](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#why-dont-you-make-an-option-for)
- [Why is there no support for pens?](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#why-is-there-no-support-for-pens)
- [Why has my bug report been closed?](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#why-has-my-bug-report-been-closed)
- [How can i activate the dark mode for widgets?](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#how-can-i-activate-the-dark-mode-for-widgets)

## Why aren't there any buttons to apply formatting

We use context based formatting to avoid distractions while writing. This is not "Word on Android".

You have some shortcuts available in a context, e.g.
- when you select some text, you can make it bold, italic, insert a link, etc.:

  ![Selection formatting](https://user-images.githubusercontent.com/4741199/102229887-89cc3e80-3eec-11eb-8398-10073bbb7359.png)
- when you hit the selector thumb without selected text, you will have actions in the context of the current line, like making it a checkbox:

  ![Line formatting](https://user-images.githubusercontent.com/4741199/102230123-c5ff9f00-3eec-11eb-990e-c4c25e016b5d.png)

This approach allows us to only show the actions that make sense for the current context.

We plan to extend this system further in the future and might add toggles for headlines etc.

## I have experienced an error

Sorry. There are so many different environments, that it is impossible for us to test each and every constellation.

First of all make sure you have updated to and tried with the latest available versions of this app, the [Nextcloud Android](https://play.google.com/store/apps/details?id=com.nextcloud.client) app and the [Notes server app](https://apps.nextcloud.com/apps/notes).

### `NextcloudApiNotRespondingException`

Try to disable the battery "optimization" for Notes Android and Nextcloud Android. Some manufacturers prevent the Notes Android app from communicating with the Nextcloud Android app properly. It is recommended to clear the storage of both apps as [explained below](#workarounds).
This is a [known issue of the SingleSignOn mechanism](https://github.com/nextcloud/Android-SingleSignOn#troubleshooting) which we only can work around but not solve on our side.

### `UnknownErrorException: Read timed out`

This issue is caused by a connection time out. This can be the case if there are infrastructural or environmental problems (like a misconfigured server or a bad network connection).
Probably you will experience it when importing an account, because at this moment, all your Notes will getting downloaded. Given you have a lots of notes, this might take longer than the connection is available.
Further synchronizations are usually not causing this issue, because the Notes app tries to synchronize only *changed* notes after the first import.
If your notes are not ten thousands of characters long, it is very unlikely that this causes a connection timeout.

We improved the import of an account in version `3.4.12` to make it more reliable by [fetching notes step by step](https://github.com/nextcloud/notes-android/issues/761#issuecomment-836989421).
If you are using an older version, you can as a workaround for the first import try to
1. move all your notes to a different folder on your Nextcloud instance
2. import your account on your smartphone
3. put your notes back to the original folder step by step and sync everytime you put some notes back 

### `NextcloudHttpRequestFailedException`

#### `HTTP status-code: 301`

This issue can happen in case of a complex inconsistent state between the Notes Android app, the Single Sign On library, the Nextcloud Android app and your Nextcloud instance. Please try to remove your account from *both*, Notes Android *and* Nextcloud Android and readd it again [as described in the `workarounds` section](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#workarounds). If the issue persists, please report especially any changes on your server side environment: Did you change your domain or IP address of your Nextcloud server? Did you change something about your user account or en- / disabled multi factor authentication (2FA / MFA)? Did you remove your account (only) from the Nextcloud Android app?

#### `HTTP status-code: 302`

As clearly described in the description of the app, [one of the requirements](https://github.com/nextcloud/notes-android#link-requirements) is to have installed the [`Notes`](https://apps.nextcloud.com/apps/notes) app on your server. This means:
- **not** [`Quick notes`](https://apps.nextcloud.com/apps/quicknotes)
- **not** [`Carnet`](https://apps.nextcloud.com/apps/carnet)
- **not** `SimpleNote`, `NextNotes`, `Joplin` nor any other app.

Only the [`Notes`](https://apps.nextcloud.com/apps/notes) app is supported by the Notes Android app. Granted, detecting a missing installation of the `Notes` app should be more seamlessly - [we are aware of it](https://github.com/nextcloud/notes-android/issues/1475) and will try to enhance the detection.

### `IllegalStateException: Duplicate key`

This is issue was caused by a bug which was present in the Notes Android app between `3.4.0` and `3.4.10`. It has been fixed in `3.4.11`, though it created a corrupt database state which is not recoverable automatically without data loss. It is therefore required to [clear the storage of the Notes Android app](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#workarounds) and import your account again from scratch. Make sure to backup unsynchronized changes before doing this.

### `NextcloudFilesAppAccountNotFoundException`

We are not yet sure what exactly causes this issue, but investigate it by [adding more debug logs to recent versions](https://github.com/nextcloud/notes-android/issues/1256#issuecomment-859505153). In theory this might happen if an already imported account has been deleted in the Nextcloud app.
As a workaround you can remove the account (or clear the storage of the app as described below if you can't access the account manager anymore) and import it again.

### `TokenMismatchException`

The reason of this error is not yet clear. It often seems to be connected to changes of the authentication (for example enabling 2FA after some time). Please clear the storage of both, the Notes and the Nextcloud Android apps as described in the [workarounds](https://github.com/nextcloud/notes-android/blob/main/FAQ.md#wrokarounds) section.

### Workarounds

In some cases, clearing the storage of the Notes Android app and restarting with a clean state will already be enough. Since we use the [Single Sign On mechanism](https://github.com/nextcloud/Android-SingleSignOn/) of Nextcloud, it might be necessary to clear the storage of **both** apps, Notes Android **and** Nextcloud Android. (the Nextcloud Android app manages some parts of the authentication and the network stack).

- ⚠️ Not yet synchronized changes will be lost by performing this step.  
- ⚠️ Uninstalling an app is **not** the same as clearing the storage, since Android keeps some data left on your device when uninstalling an app.

You can achieve this by navigating to

```
Android settings
       ↓
     Apps
       ↓
Nextcloud / Notes
       ↓
    Storage
       ↓
 Clear storage
```

Then set up your account in the Nextcloud Android app again and import the configured account in the Notes Android app.

If the issue persists, [open a bug report in our issue tracker](https://github.com/nextcloud/notes-android/issues/new?assignees=&labels=bug&template=bug_report.md&title=).

## How to share notes?

The Notes server app does [not yet provide support for sharing notes](https://github.com/nextcloud/notes/issues/38). However, there is a limited workaround until this has been implemented:
1. Open the Nextcloud Files (*not* Notes!) app in the Web UI or the Nextcloud Android app
2. Share your `Notes` folder (or any category) with the Nextcloud files app with the target user
3. The target user needs to *move* the shared folder into his / her `Notes` folder

⚠️ Both users will now be able to read and edit the notes. Keep in mind though, that there is currently no conflict resolution yet. This means: The last write wins which may cause data loss if both are editing a note in the same time. Minimize this risc by manually pull to refresh before editing a shared note.


## Why don't you make an option for…?

We prefer good defaults over providing an option for each edge case. Our resources are quite limited, so we have to consider introducing new options very carefully.

1. A feature is implemented quickly, but who will maintain it for the next 5 years?
2. Each option increases the test matrix exponentially and leads to huge efforts to test every combination
3. Each option increases the possible constellations, making it hard to track down issues
4. Each option increases the visual noise for people who will *not* use the options
5. Each option increases the maintenance efforts, making it harder over the time to work on actual features
6. Each option introduces new side effects, which might lead to undiscovered bugs or break existing features
7. The Android app aims to mirror feature parity with the corresponding server app

## Why is there no support for pens?

This topic has been requested multiple times and we'd love to support pens. There are some obstacles, though:

### Choice of approach

Handwritten notes can be implemented in various ways - for example as attachments or just recognizing the characters and translate them to text. The first approach depends on attachments support for the Notes server app (currently [work in progress](https://github.com/nextcloud/notes/issues/74)).

### Licensing issues

The Notes Android app was, is and will always be free. [Free not as in "free beer" but as in "freedom"](https://www.gnu.org/philosophy/free-sw.en.html). I therefore will not accept any solution that requires to include proprietary libraries (also not for the Google Play Store flavor).
Since i am not aware of any proper free SDK for pens, I recommend you to ask your manufacturer to publish a development SDK under a free license and ask them why they sell stuff to you which you in fact do not own.

### Hardware issues

Given a [free SDK](#licensing-issues) can be found, there is another issue: I don't own a device with a pen. I welcome [Pull Requests](https://github.com/nextcloud/notes-android/pulls) with contributions to this topic, but i can and will not buy a new device just for this aspect, sorry.

## Why has my bug report been closed?

As stated in the bug templates, we reserve to close issues which do not fill the **complete issue template**. The information we ask for is urgently needed, even if it might not seem to be important or relevant to you.

We have very limited resources and capacity and we really want to help you fixing different bugs, but we can impossibly know your environment, your different software versions, the store you used.
Therefore it is extremely important for you to describe the **exact steps to reproduce**. This includes information about your environment.

Example for a bad description:

> 1. The app crashes when i save a note

Example for a good description:

> 1. Open any existing note
> 2. Change category to another existing category
> 3. Click on the ⇦ in the top left
> 4. See app crash

We also preserve to close issues where the **original reporter does not answer within a certain time frame**. We usually answer issues within a hour and expect you to respond to our questions within a week.

This is necessary for two reasons:

1. We have a rapid development cycle - bugs which have been reported weeks ago might no longer relevant
2. We are loosing the context of a report or a question over the time. We have many things to care about and digging into an issue deep and then relying on an response which is not coming is a waste of our limited free time

## How can i activate the dark mode for widgets?

Since `v3.2.0` the widgets are using the **global Android setting**. You can change it in the Android settings, depending on your manufacturer probably under the "Display" menu item:

![Enable global Android dark mode](https://user-images.githubusercontent.com/4741199/111076875-8c8bff00-84ee-11eb-8052-b086c8e143b3.png)

The main reason is a better and tighter integration in the default android theming mechanism. For example Android will switch the complete system UI to a dark mode when the battery is low.
The widgets have previously not respected those Android intentions, also they ignored the user setting mentioned above for a overall-same theme on the device.

The dark mode of the app does *not* affect the appearance of the widgets because the context is different.
While the app is something one starts intentionally and runs in its own context, the widgets run always in the context of the launcher.
To provide a homogeneous interface in your launcher and take full benefits of OLED screens and battery saving mechanisms, Google implemented the global Android setting in Android 10 to affect everything in this context at once - the app drawer, the status bar and the widgets. 

According to the Play Store statistics when we release `v3.2.0`, more than `73%` of our (Play Store) users used Android 10 or higher and therefore the global setting is already present for them.
Further `24%` used Android 7 - Android 9 and can utilize [tweaks](https://www.androidauthority.com/night-mode-on-android-886864/) (or other workarounds if they prefer to stay on old and partially even by Google [abandoned Android versions without security fixes](https://endoflife.date/android) instead of using a modern Custom ROM or other alternatives).

The efforts and benefits of a) maintaining a custom hacky dark mode vs. b) supporting natively the global dark mode inclusive auto-theming on low battery etc. is in absolutely no proportion anymore.

The same applies to the widgets of the Nextcloud Deck App and the Nextcloud News App.
