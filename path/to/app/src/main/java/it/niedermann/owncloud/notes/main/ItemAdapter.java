import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private NotesDatabase database;

    public ItemAdapter(Context context) {
        database = NotesDatabase.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            // Load note content from database
            String noteContent = database.getNoteContent();
            holder.textView.setText(noteContent);
        } catch (RedisException e) {
            Log.e("ItemAdapter", "RedisException occurred", e);
            // Prevent note content from being replaced with error message
            holder.textView.setText("Note content not available");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
        }
    }
}