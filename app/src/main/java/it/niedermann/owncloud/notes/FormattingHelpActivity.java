/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes;

import static it.niedermann.owncloud.notes.shared.util.NoteUtil.getFontSizeFromPreferences;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import it.niedermann.owncloud.notes.branding.BrandedActivity;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityFormattingHelpBinding;

public class FormattingHelpActivity extends BrandedActivity {

    private ActivityFormattingHelpBinding binding;

    private static final String lineBreak = "\n";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFormattingHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.contentContextBasedFormatting.setMarkdownString(buildContextBasedFormattingHelp());
        binding.content.setMovementMethod(LinkMovementMethod.getInstance());
        binding.content.setMarkdownString(buildFormattingHelp());

        final var sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        binding.content.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(this, sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.content.setTypeface(Typeface.MONOSPACE);
        }
    }

    @NonNull
    private String buildContextBasedFormattingHelp() {
        return getString(R.string.formatting_help_title, getString(R.string.formatting_help_cbf_title)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_cbf_body_1) + lineBreak +
                getString(R.string.formatting_help_cbf_body_2,
                        getString(R.string.formatting_help_codefence_inline, getString(android.R.string.cut)),
                        getString(R.string.formatting_help_codefence_inline, getString(android.R.string.copy)),
                        getString(R.string.formatting_help_codefence_inline, getString(android.R.string.selectAll)),
                        getString(R.string.formatting_help_codefence_inline, getString(R.string.simple_link)),
                        getString(R.string.formatting_help_codefence_inline, getString(R.string.simple_checkbox))
                );
    }

    @NonNull
    private String buildFormattingHelp() {
        final String indention = "  ";
        final String divider = getString(R.string.formatting_help_divider);
        final String codefence = getString(R.string.formatting_help_codefence);
        final String outerCodefence = getString(R.string.formatting_help_codefence_outer);

        int numberedListItem = 1;
        final String lists = getString(R.string.formatting_help_lists_body_1) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_ol, numberedListItem++, getString(R.string.formatting_help_lists_body_2)) + lineBreak +
                getString(R.string.formatting_help_ol, numberedListItem++, getString(R.string.formatting_help_lists_body_3)) + lineBreak +
                getString(R.string.formatting_help_ol, numberedListItem, getString(R.string.formatting_help_lists_body_4)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_lists_body_5) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_ul, getString(R.string.formatting_help_lists_body_6)) + lineBreak +
                getString(R.string.formatting_help_ul, getString(R.string.formatting_help_lists_body_7)) + lineBreak +
                indention + getString(R.string.formatting_help_ul, getString(R.string.formatting_help_lists_body_8)) + lineBreak +
                indention + getString(R.string.formatting_help_ul, getString(R.string.formatting_help_lists_body_9)) + lineBreak;

        final String checkboxes = getString(R.string.formatting_help_checkboxes_body_1) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_checkbox_checked, getString(R.string.formatting_help_checkboxes_body_2)) + lineBreak +
                getString(R.string.formatting_help_checkbox_unchecked, getString(R.string.formatting_help_checkboxes_body_3)) + lineBreak;

        final String structuredDocuments = getString(R.string.formatting_help_structured_documents_body_1, "`#`", "`##`") + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title_level_3, getString(R.string.formatting_help_structured_documents_body_2)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_structured_documents_body_3, "`#`", "`######`") + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_structured_documents_body_4, getString(R.string.formatting_help_quote_keyword)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_quote, getString(R.string.formatting_help_structured_documents_body_5)) + lineBreak +
                getString(R.string.formatting_help_quote, getString(R.string.formatting_help_structured_documents_body_6)) + lineBreak;

        final String javascript = getString(R.string.formatting_help_javascript_1) + lineBreak +
                indention + indention + getString(R.string.formatting_help_javascript_2) + lineBreak +
                getString(R.string.formatting_help_javascript_3) + lineBreak;

        final int column_count = 3;
        final int row_count = 3;
        final StringBuilder table = new StringBuilder();
        table.append("|");
        for (int i = 1; i <= column_count; i++) {
            table.append(" ").append(getString(R.string.formatting_help_tables_column, i)).append(" |");
        }
        table.append("\n");
        table.append("|");
        for (int i = 0; i < column_count; i++) {
            table.append(" --- |");
        }
        table.append("\n");
        for (int i = 1; i <= row_count; i++) {
            table.append("|");
            for (int j = 1; j <= column_count; j++) {
                table.append(" ").append(getString(R.string.formatting_help_tables_value, i * j)).append(" |");
            }
            table.append("\n");
        }

        return divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_text_title)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_text_body,
                        getString(R.string.formatting_help_bold),
                        getString(R.string.formatting_help_italic),
                        getString(R.string.formatting_help_strike_through)
                ) + lineBreak +
                lineBreak +
                codefence + lineBreak +
                getString(R.string.formatting_help_text_body,
                        getString(R.string.formatting_help_bold),
                        getString(R.string.formatting_help_italic),
                        getString(R.string.formatting_help_strike_through)
                ) + lineBreak +
                codefence + lineBreak +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_lists_title)) + lineBreak +
                lineBreak +
                lists +
                lineBreak +
                codefence + lineBreak +
                lists +
                codefence + lineBreak +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_checkboxes_title)) + lineBreak +
                lineBreak +
                checkboxes +
                lineBreak +
                codefence + lineBreak +
                checkboxes +
                codefence + lineBreak +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_structured_documents_title)) + lineBreak +
                lineBreak +
                structuredDocuments +
                lineBreak +
                codefence + lineBreak +
                structuredDocuments +
                codefence + lineBreak +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_code_title)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_code_body_1) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_codefence_inline_escaped, getString(R.string.formatting_help_code_javascript_inline)) + "  " + lineBreak +
                getString(R.string.formatting_help_codefence_inline, getString(R.string.formatting_help_code_javascript_inline)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_code_body_2) + lineBreak +
                lineBreak +
                outerCodefence + lineBreak +
                codefence + lineBreak +
                javascript +
                codefence + lineBreak +
                outerCodefence + lineBreak +
                lineBreak +
                codefence + lineBreak +
                javascript +
                codefence + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_code_body_3) + lineBreak +
                lineBreak +
                outerCodefence + lineBreak +
                getString(R.string.formatting_help_codefence_javascript) + lineBreak +
                javascript +
                codefence + lineBreak +
                outerCodefence + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_codefence_javascript) + lineBreak +
                javascript +
                codefence + lineBreak +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_tables_title)) + lineBreak +
                lineBreak +
                codefence + lineBreak +
                table +
                codefence + lineBreak +
                lineBreak +
                table +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_images_title)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_images_body_1, getString(R.string.formatting_help_codefence_inline, getString(R.string.formatting_help_images_slash))) + lineBreak +
                getString(R.string.formatting_help_images_body_2, getString(R.string.formatting_help_codefence_inline, getString(R.string.formatting_help_images_escaped_space))) + lineBreak +
                lineBreak +
                codefence + lineBreak +
                getString(R.string.formatting_help_image, getString(R.string.formatting_help_images_alt), getString(R.string.formatting_help_images_escaped_space)) + lineBreak +
                codefence + lineBreak;
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.platform.themeStatusBar(this);
        util.material.themeToolbar(binding.toolbar);
    }
}
