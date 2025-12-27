/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.nextcloud.android.common.ui.util.PlatformThemeUtil.isDarkMode;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nextcloud.android.common.core.utils.DateFormatter;
import com.nextcloud.android.common.ui.theme.utils.ColorRole;

import java.util.Calendar;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

public abstract class NoteViewHolder extends RecyclerView.ViewHolder {
    @NonNull
    private final NoteClickListener noteClickListener;

    @NonNull
    private final DateFormatter dateFormatter;

    public NoteViewHolder(@NonNull View v, @NonNull NoteClickListener noteClickListener) {
        super(v);
        this.noteClickListener = noteClickListener;
        this.setIsRecyclable(false);
        this.dateFormatter = new DateFormatter(v.getContext());
    }

    @CallSuper
    public void bind(boolean isSelected, @NonNull Note note, boolean showCategory, @ColorInt int color, @Nullable CharSequence searchQuery) {
        itemView.setActivated(isSelected);
        itemView.setSelected(isSelected);
        itemView.setOnClickListener((view) -> noteClickListener.onNoteClick(getLayoutPosition(), view));
    }

    protected void bindModified(@NonNull TextView noteModified, @Nullable Calendar modified) {
        if (modified != null && modified.getTimeInMillis() > 0) {
            noteModified.setText(dateFormatter.getConditionallyRelativeFormattedTimeSpan(modified));
            noteModified.setVisibility(VISIBLE);
        } else {
            noteModified.setVisibility(INVISIBLE);
        }
    }

    protected void bindStatus(CircularProgressIndicator noteSyncStatus, DBStatus status, int color) {
        noteSyncStatus.setVisibility(DBStatus.VOID.equals(status) ? INVISIBLE : VISIBLE);

        final var context = noteSyncStatus.getContext();
        final var util = BrandingUtil.of(color, context);
        util.material.colorProgressBar(noteSyncStatus, ColorRole.PRIMARY);
    }

    protected void bindCategory(@NonNull Context context, @NonNull TextView noteCategory, boolean showCategory, @NonNull String category, int color) {
        if (!showCategory || category.isEmpty()) {
            noteCategory.setVisibility(View.GONE);
        } else {
            noteCategory.setText(category);

            final var util = BrandingUtil.of(color, context);

            if (noteCategory instanceof Chip) {
                util.material.colorChipBackground((Chip) noteCategory);
            } else {
                if (isDarkMode(context)) {
                    util.platform.tintDrawable(context, noteCategory.getBackground(), ColorRole.SECONDARY_CONTAINER);
                    util.platform.colorTextView(noteCategory, ColorRole.ON_SECONDARY_CONTAINER);
                } else {
                    util.platform.tintDrawable(context, noteCategory.getBackground(), ColorRole.PRIMARY);
                    util.platform.colorTextView(noteCategory, ColorRole.ON_PRIMARY_CONTAINER);
                }
            }

            noteCategory.setVisibility(View.VISIBLE);
        }
    }

    protected void bindFavorite(@NonNull ImageView noteFavorite, boolean isFavorite) {
        noteFavorite.setImageResource(isFavorite ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_border_grey_ccc_24dp);
        noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(getLayoutPosition(), view));
    }

    protected void bindSearchableContent(@NonNull Context context, @NonNull TextView textView, @Nullable CharSequence searchQuery, @NonNull String content, int color) {
        textView.setText(content);

        if (!TextUtils.isEmpty(searchQuery)) {
            final var util = BrandingUtil.of(color, context);
            util.platform.highlightText(textView, content, searchQuery.toString());
        }
    }

    public abstract void showSwipe(float dX);

    @Nullable
    public abstract View getNoteSwipeable();

    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<>() {
            @Override
            public int getPosition() {
                return getAdapterPosition();
            }

            @Override
            public Long getSelectionKey() {
                return getItemId();
            }
        };
    }
}