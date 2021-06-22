package it.niedermann.owncloud.notes.edit.outline;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemCategoryBinding;
import it.niedermann.owncloud.notes.databinding.ItemOutlineBinding;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class OutlineAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String clearItemId = "clear_item";
    private static final String addItemId = "add_item";
    @NonNull
    private List<OutlineItem> headers = new ArrayList<>();
    @NonNull
    private final OutlineAdapter.OutlineListener listener;
    private final Context context;

    OutlineAdapter(@NonNull Context context, @NonNull OutlineAdapter.OutlineListener l) {
        this.context = context;
        this.listener = l;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outline, parent, false);
        return new OutlineAdapter.OutlineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OutlineItem outline = headers.get(position);
        OutlineAdapter.OutlineViewHolder ovh = (OutlineAdapter.OutlineViewHolder) holder;


        ovh.getWrapper().setOnClickListener((v) -> listener.onOutlineChosen(outline));

        //ovh.getType().setText(String.valueOf(outline.type));

        TextView tv = ovh.getTitle();

        tv.setText(String.valueOf(outline.label));

        float defaults = 14;

        switch (outline.type){
            case "header1":


               tv.setTextSize(defaults * 2);
                break;
            case "header2":
                tv.setTextSize((float) (defaults * 1.5));
                break;
            case "header3":
                tv.setTextSize((float) (defaults * 1.1));
                break;
            case "header4":
                //tv.setTextSize(tv.getTextSize()+ 3);
                break;
            case "header5":
                tv.setTextSize((float) (defaults * 0.8));
                break;
            case "header6":
                tv.setTextSize((float) (defaults * 0.5));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return headers.size();
    }

    static class OutlineViewHolder extends RecyclerView.ViewHolder {
        private final ItemOutlineBinding binding;

        private OutlineViewHolder(View view) {
            super(view);
            binding = ItemOutlineBinding.bind(view);
        }

        private View getWrapper() {
            return binding.outlineWrapper;
        }


        //private TextView getType() {
        //    return binding.headerType;
       // }

        private TextView getTitle() {
            return binding.headerTitle;
        }
    }

    void setList(List<OutlineItem> l) {
        this.headers.clear();
        this.headers.addAll(l);


        notifyDataSetChanged();
    }

    public interface OutlineListener {
        void onOutlineChosen(OutlineItem oi);


    }
}
