package it.niedermann.android.markdown.model;

public enum EListType {
    STAR('*'),
    DASH('-'),
    PLUS('+');

    public final String listSymbol;
    public final String listSymbolWithTrailingSpace;
    public final String checkboxChecked;
    public final String checkboxUnchecked;
    public final String checkboxUncheckedWithTrailingSpace;

    EListType(char listSymbol) {
        this.listSymbol = String.valueOf(listSymbol);
        this.listSymbolWithTrailingSpace = listSymbol + " ";
        this.checkboxChecked = listSymbolWithTrailingSpace + "[x]";
        this.checkboxUnchecked = listSymbolWithTrailingSpace + "[ ]";
        this.checkboxUncheckedWithTrailingSpace = checkboxUnchecked + " ";
    }
}