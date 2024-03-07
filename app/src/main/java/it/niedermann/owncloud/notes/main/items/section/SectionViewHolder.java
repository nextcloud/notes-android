/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.section;

import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.databinding.ItemNotesListSectionItemBinding;

public class SectionViewHolder extends RecyclerView.ViewHolder {
    private final ItemNotesListSectionItemBinding binding;

    public SectionViewHolder(ItemNotesListSectionItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SectionItem item) {
        binding.sectionTitle.setText(item.getTitle());
    }
}