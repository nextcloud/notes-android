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

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.text.TextFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.ExceptionDialogFragment;
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

    private static final String TAG = FormattingHelpActivity.class.getSimpleName();
    private ActivityFormattingHelpBinding binding;
    private String content;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFormattingHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        final StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.formatting_help)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
        }

        content = stringBuilder.toString();

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

    @Override
    public void applyBrand(int mainColor, int textColor) {

    }
}
