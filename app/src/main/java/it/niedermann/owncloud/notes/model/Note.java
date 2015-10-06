package it.niedermann.owncloud.notes.model;

import android.text.Html;

import com.commonsware.cwac.anddown.AndDown;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

@SuppressWarnings("serial")
public class Note implements Serializable {

    private static final AndDown and_down = new AndDown();
	private long id = 0;
	private String title = "";
	private Calendar modified = null;
	private String content = "";
    private String htmlContent = null;

	public Note(long id, Calendar modified, String title, String content) {
		this.id = id;
        if(title != null)
        this.title = Html.fromHtml(and_down.markdownToHtml(title)).toString().trim();
        setTitle(title);
        setContent(content);
        this.modified = modified;
    }

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

    public void setTitle(String title) {
        this.title = Html.fromHtml(and_down.markdownToHtml(title)).toString().trim();
    }

	@SuppressWarnings("WeakerAccess")
    public Calendar getModified() {
		return modified;
	}

	public String getModified(String format) {
		return new SimpleDateFormat(format, Locale.GERMANY)
				.format(this.getModified().getTime());
	}

	public String getContent() {
		return content;
	}

    public void setContent(String content) {
        this.content = content;
        this.htmlContent = null;
    }

    public String getHtmlContent() {
        if(htmlContent == null && getContent() != null) {
            htmlContent = and_down.markdownToHtml(getContent());
        }
        return htmlContent;
    }

	@Override
	public String toString() {
		return "#" + getId() + " " + getTitle() + " (" + getModified(NoteSQLiteOpenHelper.DATE_FORMAT) + ")";
	}
}