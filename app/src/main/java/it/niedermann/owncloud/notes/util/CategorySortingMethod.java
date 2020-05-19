package it.niedermann.owncloud.notes.util;

public enum CategorySortingMethod {
    SORT_LEXICOGRAPHICAL_ASC(0),
    SORT_MODIFIED_DESC(1);

    private int smid;   // sorting method id

    /***
     * Constructor
     * @param smid given sorting method id
     */
    CategorySortingMethod(int smid) {
        this.smid = smid;
    }

    /***
     * Retrieve the sorting method id represented in database
     * @return the sorting method id for the enum item
     */
    public int getCSMID() {
        return this.smid;
    }
}
