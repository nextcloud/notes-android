package it.niedermann.owncloud.notes.model;

import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemBinding;
import it.niedermann.owncloud.notes.util.Notes;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
    private final ItemNotesListNoteItemBinding binding;
    private final NoteClickListener noteClickListener;

    public NoteViewHolder(View v, NoteClickListener noteClickListener) {
        super(v);
        binding = ItemNotesListNoteItemBinding.bind(v);
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

    public void showSwipe(boolean left) {
        binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
        binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        binding.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
    }

    public void bind(DBNote note, NoteClickListener noteClickListener, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);

        // FIXME coloring when searching
        binding.noteCategory.setVisibility(showCategory && !note.getCategory().isEmpty() ? View.VISIBLE : View.GONE);
        binding.noteCategory.setText(Html.fromHtml(note.getCategory()));

        DrawableCompat.setTint(binding.noteCategory.getBackground(), mainColor);
        binding.noteCategory.setTextColor(Notes.isDarkThemeActive(binding.getRoot().getContext()) ? textColor : Color.BLACK);

        binding.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.INVISIBLE : View.VISIBLE);
        binding.noteFavorite.setImageResource(note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
        binding.noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(getAdapterPosition(), view));

        @ColorInt final int searchBackground = binding.noteExcerpt.getContext().getResources().getColor(R.color.bg_highlighted);
        @ColorInt final int searchForeground = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(binding.noteExcerpt.getContext(), mainColor);
        if (!TextUtils.isEmpty(searchQuery)) {
            final Pattern pattern = Pattern.compile("(" + searchQuery + ")", Pattern.CASE_INSENSITIVE);
            SpannableString spannableString = new SpannableString(note.getTitle());
            Matcher matcher = pattern.matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            binding.noteTitle.setText(spannableString);

            spannableString = new SpannableString(note.getExcerpt());
            matcher = pattern.matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            binding.noteExcerpt.setText(spannableString);
        } else {
            binding.noteTitle.setText(note.getTitle());
            binding.noteExcerpt.setText(note.getExcerpt());
        }
    }

    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}