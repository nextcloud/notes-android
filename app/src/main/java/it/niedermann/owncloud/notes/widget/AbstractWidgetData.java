package it.niedermann.owncloud.notes.widget;

import androidx.annotation.IntRange;
import androidx.room.PrimaryKey;

public abstract class AbstractWidgetData {

    @PrimaryKey
    private int id;
    private long accountId;
    @IntRange(from = 0, to = 2)
    private int themeMode;

    protected AbstractWidgetData() {

    }

    protected AbstractWidgetData(int id, long accountId, @IntRange(from = 0, to = 2) int themeMode) {
        this.id = id;
        this.accountId = accountId;
        this.themeMode = themeMode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @IntRange(from = 0, to = 2)
    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(@IntRange(from = 0, to = 2) int themeMode) {
        this.themeMode = themeMode;
    }
}
