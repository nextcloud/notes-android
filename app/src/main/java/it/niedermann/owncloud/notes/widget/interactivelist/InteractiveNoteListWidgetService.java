/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.interactivelist;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class InteractiveNoteListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new InteractiveNoteListWidgetFactory(this.getApplicationContext(), intent);
    }
}
