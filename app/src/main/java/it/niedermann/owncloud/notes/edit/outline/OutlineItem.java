package it.niedermann.owncloud.notes.edit.outline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OutlineItem {
    @NonNull
    public String type;
    @NonNull
    public String label;

    @Nullable
    public Integer textOffset;



    public OutlineItem(@NonNull String type, @NonNull String label, @Nullable Integer lineNumber) {
        this.type = type;
        this.label = label;
        this.type = type;
       this.textOffset = lineNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutlineItem)) return false;

        OutlineItem that = (OutlineItem) o;


        if (!type.equals(that.type)) return false;
        if (!label.equals(that.label)) return false;
        if (textOffset != null ? !textOffset.equals(that.textOffset) : that.textOffset != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + (textOffset != null ? textOffset.hashCode() : 0);
        return result;
    }
}
