# Frequently asked questions

- [Why aren't there any buttons to apply formatting?](https://github.com/stefan-niedermann/nextcloud-notes/blob/master/FAQ.md#why-arent-there-any-buttons-to-apply-formatting)
- [I have experienced an error](https://github.com/stefan-niedermann/nextcloud-notes/blob/master/FAQ.md#i-have-experienced-an-error)
- [Why has my bug report been closed?](https://github.com/stefan-niedermann/nextcloud-notes/blob/master/FAQ.md#why-has-my-bug-report-been-closed)
- [How can i activate the dark mode for widgets?](https://github.com/stefan-niedermann/nextcloud-notes/blob/master/FAQ.md#how-can-i-activate-the-dark-mode-for-widgets)

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

First of all make sure you have updated to and tried with the latest available versions of both, this app and the [Notes server app](https://apps.nextcloud.com/apps/notes).

### `NextcloudApiNotRespondingException`

Try to disable the battery "optimization" for both apps. Some manufacturers prevent the app from communicating with the Nextcloud Android properly.
This is a [known issue of the SingleSignOn mechanism](https://github.com/nextcloud/Android-SingleSignOn#troubleshooting) which we only can work around but not solve on our side.

### `UnknownErrorException: Read timed out`

This issue is caused by a connection time out. This can be the case if there are infrastructural or environmental problems (like a misconfigured server or a bad network connection).
Probably you will experience it when importing an account, because at this moment, all your Notes will getting downloaded at once. Given you have a lots of notes, this might take longer than the connection is available.
Further synchronizations are usually not causing this issue, because the Notes app tries to synchronize only *changed* notes after the first import.
If your notes are not ten thousands of characters long, it is very unlikely that this causes a connection timeout.

We plan to improve the import of an account and make it more reliable by [fetching notes step by step](https://github.com/stefan-niedermann/nextcloud-notes/issues/761#issuecomment-836989421) in a future release.
Until then you can as a workaround for the first import try to
1. move all your notes to a different folder on your Nextcloud instance
2. import your account on your smartphone
3. put your notes back to the original folder step by step and sync everytime you put some notes back 

### `NextcloudFilesAppAccountNotFoundException`

We are not yet sure what exactly causes this issue, but investigate it by [adding more debug logs to recent versions](https://github.com/stefan-niedermann/nextcloud-notes/issues/1256#issuecomment-859505153). In theory this might happen if an already imported account has been deleted in the Nextcloud app.
As a workaround you can remove the account (or clear the storage of the app as described below if you can't access the account manager anymore) and import it again.

### `TokenMismatchException` and all others

In all other cases please try to clear the storage of **both** apps, Nextcloud Android **and** Nextcloud Notes Android. Not yet synchronized changes will be lost by performing this step.

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

Then set up your account in the Nextcloud Android app again and import the configured account in the Nextcloud Notes Android app.

If the issue persists, [open a bug report in our issue tracker](https://github.com/stefan-niedermann/nextcloud-notes/issues/new?assignees=&labels=bug&template=bug_report.md&title=).

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