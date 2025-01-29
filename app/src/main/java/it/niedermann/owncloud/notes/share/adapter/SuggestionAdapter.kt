package it.niedermann.owncloud.notes.share.adapter

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.cursoradapter.widget.CursorAdapter
import it.niedermann.owncloud.notes.R

class SuggestionAdapter(context: Context, cursor: Cursor?) : CursorAdapter(context, cursor, false) {
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.item_suggestion_adapter, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val suggestion = cursor.getString(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1))
        view.findViewById<TextView>(R.id.suggestion_text).text = suggestion

        val icon = view.findViewById<ImageView>(R.id.suggestion_icon)

        val iconId = cursor.getIntOrNull(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1))
        if (iconId != null) {
            icon.setImageDrawable(ContextCompat.getDrawable(context, iconId))
        } else {
            val iconURIAsString = cursor.getStringOrNull(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1)) ?: return
            val iconURI = Uri.parse(iconURIAsString)
            icon.setImageURI(iconURI)
        }
    }
}
