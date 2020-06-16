package it.niedermann.owncloud.notes.formattinghelp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedActivity;
import it.niedermann.owncloud.notes.databinding.ActivityFormattingHelpBinding;

import static it.niedermann.owncloud.notes.util.MarkDownUtil.CHECKBOX_CHECKED_MINUS;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.CHECKBOX_CHECKED_STAR;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.CHECKBOX_UNCHECKED_MINUS;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.CHECKBOX_UNCHECKED_STAR;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.getMarkDownConfiguration;
import static it.niedermann.owncloud.notes.util.MarkDownUtil.parseCompat;
import static it.niedermann.owncloud.notes.util.NoteUtil.getFontSizeFromPreferences;

public class FormattingHelpActivity extends BrandedActivity {

    private ActivityFormattingHelpBinding binding;
    private String content;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFormattingHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        content = buildFormattingHelp();

        final MarkdownProcessor markdownProcessor = new MarkdownProcessor(this);
        markdownProcessor.factory(TextFactory.create());
        markdownProcessor.config(getMarkDownConfiguration(binding.content.getContext())
                .setOnTodoClickCallback((view, line, lineNumber) -> {
                            try {
                                String[] lines = TextUtils.split(content, "\\r?\\n");
                                /*
                                 * Workaround for RxMarkdown-bug:
                                 * When (un)checking a checkbox in a note which contains code-blocks, the "`"-characters get stripped out in the TextView and therefore the given lineNumber is wrong
                                 * Find number of lines starting with ``` before lineNumber
                                 */
                                boolean inCodefence = false;
                                for (int i = 0; i < lines.length; i++) {
                                    if (lines[i].startsWith("```")) {
                                        inCodefence = !inCodefence;
                                        lineNumber++;
                                    }
                                    if (inCodefence && TextUtils.isEmpty(lines[i])) {
                                        lineNumber++;
                                    }
                                    if (i == lineNumber) {
                                        break;
                                    }
                                }

                                /*
                                 * Workaround for multiple RxMarkdown-bugs:
                                 * When (un)checking a checkbox which is in the last line, every time it gets toggled, the last character of the line gets lost.
                                 * When (un)checking a checkbox, every markdown gets stripped in the given line argument
                                 */
                                if (lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_MINUS) || lines[lineNumber].startsWith(CHECKBOX_UNCHECKED_STAR)) {
                                    lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_MINUS, CHECKBOX_CHECKED_MINUS);
                                    lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_UNCHECKED_STAR, CHECKBOX_CHECKED_STAR);
                                } else {
                                    lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_MINUS, CHECKBOX_UNCHECKED_MINUS);
                                    lines[lineNumber] = lines[lineNumber].replace(CHECKBOX_CHECKED_STAR, CHECKBOX_UNCHECKED_STAR);
                                }

                                content = TextUtils.join("\n", lines);
                                binding.content.setText(parseCompat(markdownProcessor, content));
                            } catch (IndexOutOfBoundsException e) {
                                Toast.makeText(this, R.string.checkbox_could_not_be_toggled, Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            return line;
                        }
                )
                .setOnLinkClickCallback((view, link) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))))
                .build());
        binding.content.setMovementMethod(LinkMovementMethod.getInstance());
        binding.content.setText(parseCompat(markdownProcessor, content));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        binding.content.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFontSizeFromPreferences(this, sp));
        if (sp.getBoolean(getString(R.string.pref_key_font), false)) {
            binding.content.setTypeface(Typeface.MONOSPACE);
        }
    }

    @NonNull
    private String buildFormattingHelp() {
        final String lineBreak = "\n";
        final String indention = "  ";
        final String divider = getString(R.string.formatting_help_divider);
        final String codefence = getString(R.string.formatting_help_codefence);

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

        return getString(R.string.formatting_help_title, getString(R.string.formatting_help_cbf_title)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_cbf_body_1) + lineBreak +
                getString(R.string.formatting_help_cbf_body_2,
                        getString(R.string.formatting_help_codefence_inline, getString(android.R.string.cut)),
                        getString(R.string.formatting_help_codefence_inline, getString(android.R.string.copy)),
                        getString(R.string.formatting_help_codefence_inline, getString(android.R.string.selectAll)),
                        getString(R.string.formatting_help_codefence_inline, getString(R.string.simple_link)),
                        getString(R.string.formatting_help_codefence_inline, getString(R.string.simple_checkbox))
                ) + lineBreak +
                lineBreak +
                divider + lineBreak +
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
                getString(R.string.formatting_help_codefence_inline_escaped, getString(R.string.formatting_help_code_javascript_inline)) + lineBreak +
                getString(R.string.formatting_help_codefence_inline, getString(R.string.formatting_help_code_javascript_inline)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_code_body_2) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_codefence_escaped) + lineBreak +
                javascript +
                getString(R.string.formatting_help_codefence_escaped) + lineBreak +
                lineBreak +
                codefence + lineBreak +
                javascript +
                codefence + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_code_body_3) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_codefence_javascript_escaped) + lineBreak +
                javascript +
                getString(R.string.formatting_help_codefence_escaped) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_codefence_javascript) + lineBreak +
                javascript +
                codefence + lineBreak +
                lineBreak +
                divider + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_title, getString(R.string.formatting_help_unsupported_title)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_unsupported_body_1) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_ul, getString(R.string.formatting_help_unsupported_body_2)) + lineBreak +
                getString(R.string.formatting_help_ul, getString(R.string.formatting_help_unsupported_body_3)) + lineBreak +
                lineBreak +
                getString(R.string.formatting_help_unsupported_body_4) + lineBreak;
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.toolbar);
    }
}
