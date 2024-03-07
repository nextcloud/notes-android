/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.exception.tips;

import static it.niedermann.owncloud.notes.exception.ExceptionDialogFragment.INTENT_EXTRA_BUTTON_TEXT;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemTipBinding;

public class TipsViewHolder extends RecyclerView.ViewHolder {
    private final ItemTipBinding binding;

    @SuppressWarnings("WeakerAccess")
    public TipsViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = ItemTipBinding.bind(itemView);
    }

    public void bind(TipsModel tip, Consumer<Intent> actionButtonClickedListener) {
        binding.tip.setText(tip.getText());
        final var intent = tip.getActionIntent();
        if (intent != null && intent.hasExtra(INTENT_EXTRA_BUTTON_TEXT)) {
            binding.actionButton.setVisibility(View.VISIBLE);
            binding.actionButton.setText(intent.getIntExtra(INTENT_EXTRA_BUTTON_TEXT, 0));
            binding.actionButton.setOnClickListener((v) -> actionButtonClickedListener.accept(intent));
        } else {
            binding.actionButton.setVisibility(View.GONE);
        }
    }
}
