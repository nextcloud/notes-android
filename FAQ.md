# Frequently asked questions

## I have experienced an error

Sorry. There are so many different environments, that it is impossible for us to test each and every constellation.

First of all make sure you have updated to and tried with the latest available versions of both, this app and the [Notes server app](https://apps.nextcloud.com/apps/notes).

In case you receive a `NextcloudApiNotRespondingException`, try to disable the battery optimization for both apps.
In all other cases please try  to clear the storage of **both** apps, Nextcloud Android **and** Nextcloud Notes Android.

You can achieve this by navigating to

```
Android settings
 ↳ Apps
   ↳ Nextcloud / Notes
     ↳ Storage
       ↳ Clear storage
```

Often there is an issue with the state of the server app. Try to create a new test account at your Nextcloud instance and connect to it.
If there are no errors, share the boards of your actual account one by one with your test account to find the defect one.

If the issue persists, [open a bug report in our issue tracker](https://github.com/stefan-niedermann/nextcloud-notes/issues/new?assignees=&labels=bug&template=bug_report.md&title=).

## Why has my bug report been closed?

As stated in the bug templates, we reserve to close issues which do not fill the **complete** issue template. The information we ask for is urgently needed, even if it might not seem to be important or relevant to you.

We have very limited resources and capacity and we really want to help you fixing different bugs, but we can impossibly know your environment, your different software versions, the store you used.
Therefore it is extremely important for you to describe the **exact steps to reproduce**. This includes information about your environment.

Example for a bad description:

> 1. The app crashes when i save a note

Example for a good description:

> 1. Open any existing note
> 2. Change category to another existing category
> 3. Click on the ⇦ in the top left
> 4. See app crash

We also preserve to close issues where the original reporter does not answer within a certain time frame. We usually answer issues within a hour and expect you to respond to our questions within a week.

This is necessary for two reasons:

1. We have a rapid development cycle - bugs which have been reported weeks ago might no longer relevant
2. We are loosing the context of a report or a question over the time. We have many things to care about and digging into an issue deep and then relying on an response which is not coming is a waste of our limited free time
