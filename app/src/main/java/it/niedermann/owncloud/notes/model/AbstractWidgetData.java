package it.niedermann.owncloud.notes.model;

public abstract class AbstractWidgetData {

    private int appWidgetId;
    private long accountId;
    private int themeMode;

    protected AbstractWidgetData() {

    }

    protected AbstractWidgetData(int appWidgetId, long accountId, int themeMode) {
        this.appWidgetId = appWidgetId;
        this.accountId = accountId;
        this.themeMode = themeMode;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public void setAppWidgetId(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }
}
