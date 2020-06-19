package it.niedermann.owncloud.notes.util;

public enum CategorySortingMethod {
    SORT_MODIFIED_DESC("MODIFIED DESC"),
    SORT_LEXICOGRAPHICAL_ASC("TITLE COLLATE NOCASE ASC");

    private String sorder;  // sorting method OrderBy for SQL

    /***
     * Constructor
     * @param orderby given sorting method OrderBy
     */
    CategorySortingMethod(String orderby) {
        this.sorder = orderby;
    }

    /***
     * Retrieve the sorting method id represented in database
     * @return the sorting method id for the enum item
     */
    public int getCSMID() {
        return this.ordinal();
    }

    /***
     * Retrieve the sorting method order for SQL
     * @return the sorting method order for the enum item
     */
    public String getSorder() {
        return this.sorder;
    }

    /***
     * Retrieve the corresponding enum value with given the index (ordinal)
     * @param index the index / ordinal of the corresponding enum value stored in DB
     * @return the corresponding enum item with the index (ordinal)
     */
    public static CategorySortingMethod getCSM(int index) {
        return CategorySortingMethod.values()[index];
    }
}
