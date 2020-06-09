package it.niedermann.owncloud.notes.model;

import android.content.Context;
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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.util.Notes;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static it.niedermann.owncloud.notes.util.ColorUtil.contrastRatioIsSufficient;
import static it.niedermann.owncloud.notes.util.ColorUtil.isColorDark;

public abstract class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
    private final NoteClickListener noteClickListener;

    public NoteViewHolder(@NonNull View v, @NonNull NoteClickListener noteClickListener) {
        super(v);
        this.noteClickListener = noteClickListener;
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int adapterPosition = getAdapterPosition();
        if (adapterPosition != NO_POSITION) {
            noteClickListener.onNoteClick(adapterPosition, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return noteClickListener.onNoteLongClick(getAdapterPosition(), v);
    }

    protected void bindCategory(Context context, TextView noteCategory, boolean showCategory, String category, int mainColor) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(noteCategory.getBackground(), categoryBackground);
        } else {
            final GradientDrawable drawable = (GradientDrawable) noteCategory.getBackground();
            drawable.setStroke(1, categoryBackground);
            drawable.setColor(isDarkThemeActive ? categoryBackground : Color.TRANSPARENT);
        }
        noteCategory.setTextColor(categoryForeground);
    }

    protected void bindFavorite(ImageView noteFavorite, boolean isFavorite) {
        noteFavorite.setImageResource(isFavorite ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
        noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(getAdapterPosition(), view));
    }

    protected void bindTitleAndExcerpt(Context context, TextView noteTitle, @Nullable TextView noteExcerpt, CharSequence searchQuery, DBNote note, int mainColor) {
        if (!TextUtils.isEmpty(searchQuery)) {
            @ColorInt final int searchBackground = context.getResources().getColor(R.color.bg_highlighted);
            @ColorInt final int searchForeground = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(context, mainColor);

            // The Pattern.quote method will add \Q to the very beginning of the string and \E to the end of the string
            // It implies that the string between \Q and \E is a literal string and thus the reserved keyword in such string will be ignored.
            // See https://stackoverflow.com/questions/15409296/what-is-the-use-of-pattern-quote-method
            final Pattern pattern = Pattern.compile("(" + Pattern.quote(searchQuery.toString()) + ")", Pattern.CASE_INSENSITIVE);
            SpannableString spannableString = new SpannableString(note.getTitle());
            Matcher matcher = pattern.matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            noteTitle.setText(spannableString);

            spannableString = new SpannableString(note.getExcerpt());
            matcher = pattern.matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            if (noteExcerpt != null) {
                noteExcerpt.setText(spannableString);
            }
        } else {
            noteTitle.setText(note.getTitle());
            if (noteExcerpt != null) {
                noteExcerpt.setText(note.getExcerpt());
            }
        }
    }

    public abstract void showSwipe(boolean left);

    public abstract View getNoteSwipeable();
}