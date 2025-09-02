/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.menu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.nextcloud.android.sso.FilesAppTypeRegistry;
import com.nextcloud.android.sso.helper.VersionCheckHelper;
import com.nextcloud.android.sso.model.FilesAppType;

import java.util.Optional;

import it.niedermann.owncloud.notes.FormattingHelpActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.about.AboutActivity;
import it.niedermann.owncloud.notes.databinding.ItemNavigationBinding;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.preferences.PreferencesActivity;

public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    @NonNull
    private final MenuItem[] menuItems;
    @ColorInt
    private int color;
    @NonNull
    private final Consumer<MenuItem> onClick;

    public MenuAdapter(@NonNull Context context, @NonNull Account account, int settingsRequestCode, @NonNull Consumer<MenuItem> onClick, @ColorInt int color) {
        this.menuItems = new MenuItem[]{
                new MenuItem(new Intent(context, FormattingHelpActivity.class), R.string.action_formatting_help, R.drawable.ic_baseline_help_outline_24),
                new MenuItem(generateTrashbinIntent(context, account), R.string.action_trashbin, R.drawable.ic_delete_grey600_24dp),
                new MenuItem(new Intent(context, PreferencesActivity.class), settingsRequestCode, R.string.action_settings, R.drawable.ic_settings_grey600_24dp),
                new MenuItem(new Intent(context, AboutActivity.class), R.string.simple_about, R.drawable.ic_info_outline_grey600_24dp)
        };
        this.onClick = onClick;
        this.color = color;
        setHasStableIds(true);
    }

    public void applyBrand(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuViewHolder(ItemNavigationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(menuItems[position], color, onClick);
    }

    public void updateAccount(@NonNull Context context, @NonNull Account account) {
        menuItems[1].setIntent(new Intent(generateTrashbinIntent(context, account)));
    }

    @Override
    public int getItemCount() {
        return menuItems.length;
    }

    @NonNull
    private static Intent generateTrashbinIntent(@NonNull Context context, @NonNull Account account) {
        // https://github.com/nextcloud/android/pull/8405#issuecomment-852966877
        final int minVersionCode = 30170090;
        try {
            Optional<FilesAppType> prod = FilesAppTypeRegistry.getInstance().getTypes().stream().filter(t -> t.stage() == FilesAppType.Stage.PROD).findFirst();
            Optional<FilesAppType> dev = FilesAppTypeRegistry.getInstance().getTypes().stream().filter(t -> t.stage() == FilesAppType.Stage.DEV).findFirst();
            if (prod.isPresent() && VersionCheckHelper.getNextcloudFilesVersionCode(context, prod.get()) > minVersionCode) {
                return generateTrashbinAppIntent(context, account, prod.get());
            } else if (dev.isPresent() && VersionCheckHelper.getNextcloudFilesVersionCode(context, dev.get()) > minVersionCode) {
                return generateTrashbinAppIntent(context, account, dev.get());
            } else {
                // Files app is too old to be able to switch the account when launching the TrashbinActivity
                return generateTrashbinWebIntent(account);
            }
        } catch (PackageManager.NameNotFoundException | SecurityException e) {
            e.printStackTrace();
            return generateTrashbinWebIntent(account);
        }
    }

    private static Intent generateTrashbinAppIntent(@NonNull Context context, @NonNull Account account, FilesAppType type) throws PackageManager.NameNotFoundException {
        final var packageManager = context.getPackageManager();
        final String packageName = type.packageId();
        final var intent = new Intent();
        intent.setClassName(packageName, "com.owncloud.android.ui.trashbin.TrashbinActivity");
        if (packageManager.resolveActivity(intent, 0) != null) {
            return intent
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Intent.EXTRA_USER, account.getAccountName());
        }
        throw new PackageManager.NameNotFoundException("Could not resolve target activity.");
    }

    private static Intent generateTrashbinWebIntent(@NonNull Account account) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(account.getUrl() + "/index.php/apps/files/?dir=/&view=trashbin"));
    }
}
