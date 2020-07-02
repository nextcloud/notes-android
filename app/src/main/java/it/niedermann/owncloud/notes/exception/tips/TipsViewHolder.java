package it.niedermann.owncloud.notes.exception.tips;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemTipBinding;

import static it.niedermann.owncloud.notes.exception.ExceptionDialogFragment.INTENT_EXTRA_BUTTON_TEXT;


public class TipsViewHolder extends RecyclerView.ViewHolder {
    private final ItemTipBinding binding;

    @SuppressWarnings("WeakerAccess")
    public TipsViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemTipBinding.bind(itemView);
    }

    public void bind(TipsModel tip, Consumer<Intent> actionButtonClickedListener) {
        binding.tip.setText(tip.getText());
        final Intent actionIntent = tip.getActionIntent();
        if (actionIntent != null && actionIntent.hasExtra(INTENT_EXTRA_BUTTON_TEXT)) {
            binding.actionButton.setVisibility(View.VISIBLE);
            binding.actionButton.setText(actionIntent.getIntExtra(INTENT_EXTRA_BUTTON_TEXT, 0));
            binding.actionButton.setOnClickListener((v) -> actionButtonClickedListener.accept(actionIntent));
        } else {
            binding.actionButton.setVisibility(View.GONE);
        }
    }
}