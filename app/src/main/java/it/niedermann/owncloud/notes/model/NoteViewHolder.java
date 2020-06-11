package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.util.Notes;

import static it.niedermann.owncloud.notes.util.ColorUtil.contrastRatioIsSufficient;
import static it.niedermann.owncloud.notes.util.ColorUtil.isColorDark;

public abstract class NoteViewHolder extends RecyclerView.ViewHolder {
    @NonNull
    private final NoteClickListener noteClickListener;

    public NoteViewHolder(@NonNull View v, @NonNull NoteClickListener noteClickListener) {
        super(v);
        this.noteClickListener = noteClickListener;
    }

    @CallSuper
    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        itemView.setOnClickListener((view) -> noteClickListener.onNoteClick(getAdapterPosition(), view));
        itemView.setOnLongClickListener((view) -> noteClickListener.onNoteLongClick(getAdapterPosition(), view));
    }

    protected void bindCategory(@NonNull Context context, @NonNull TextView noteCategory, boolean showCategory, @NonNull String category, int mainColor) {
        final boolean isDarkThemeActive = Notes.isDarkThemeActive(context);
        noteCategory.setVisibility(showCategory && !category.isEmpty() ? View.VISIBLE : View.GONE);
        noteCategory.setText(category);

        @ColorInt int categoryForeground;
        @ColorInt int categoryBackground;

        if (isDarkThemeActive) {
            if (isColorDark(mainColor)) {
                if (contrastRatioIsSufficient(mainColor, Color.BLACK)) {
                    categoryBackground = mainColor;
                    categoryForeground = Color.WHITE;
                } else {
                    categoryBackground = Color.WHITE;
                    categoryForeground = mainColor;
                }
            } else {
                categoryBackground = mainColor;
                categoryForeground = Color.BLACK;
            }
        } else {
            categoryForeground = Color.BLACK;
            if (isColorDark(mainColor) || contrastRatioIsSufficient(mainColor, Color.WHITE)) {
                categoryBackground = mainColor;
            } else {
                categoryBackground = Color.BLACK;
            }
        }

        noteCategory.setTextColor(categoryForeground);
        if (noteCategory instanceof Chip) {
            ((Chip) noteCategory).setChipStrokeColor(ColorStateList.valueOf(categoryBackground));
            ((Chip) noteCategory).setChipBackgroundColor(ColorStateList.valueOf(isDarkThemeActive ? categoryBackground : Color.TRANSPARENT));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.setTint(noteCategory.getBackground(), categoryBackground);
            } else {
                final GradientDrawable drawable = (GradientDrawable) noteCategory.getBackground();
                drawable.setStroke(1, categoryBackground);
                drawable.setColor(isDarkThemeActive ? categoryBackground : Color.TRANSPARENT);
            }
        }
    }

    protected void bindFavorite(@NonNull ImageView noteFavorite, boolean isFavorite) {
        noteFavorite.setImageResource(isFavorite ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
        noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(getAdapterPosition(), view));
    }

    protected void bindSearchableContent(@NonNull Context context, @NonNull TextView textView, @Nullable CharSequence searchQuery, @NonNull String content, int mainColor) {
        CharSequence processedContent = content;
        if (!TextUtils.isEmpty(searchQuery)) {
            @ColorInt final int searchBackground = context.getResources().getColor(R.color.bg_highlighted);
            @ColorInt final int searchForeground = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(context, mainColor);

            // The Pattern.quote method will add \Q to the very beginning of the string and \E to the end of the string
            // It implies that the string between \Q and \E is a literal string and thus the reserved keyword in such string will be ignored.
            // See https://stackoverflow.com/questions/15409296/what-is-the-use-of-pattern-quote-method
            final Pattern pattern = Pattern.compile("(" + Pattern.quote(searchQuery.toString()) + ")", Pattern.CASE_INSENSITIVE);
            SpannableString spannableString = new SpannableString(content);
            Matcher matcher = pattern.matcher(spannableString);

            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            processedContent = spannableString;
        }
        textView.setText(processedContent);
    }

    public abstract void showSwipe(boolean left);

    @Nullable
    public abstract View getNoteSwipeable();
}