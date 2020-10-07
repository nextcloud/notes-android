package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.Category;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;

@Dao
public interface CategoryDao {

    @Insert
    Long addCategory(Category entity);

    @Query("SELECT * FROM CATEGORIES WHERE CATEGORY_ID = :id")
    Category getCategory(long id);

    /**
     * This function will be called when the category or note is updated.
     * Because sometime we will remove some notes in categories.
     * Such that there must be such a category without any note.
     * For these useless category, it is better to remove.
     * Move a note from a category to another may also lead to the same result.
     *
     * @param accountId The user accountId
     */
    @Query("DELETE FROM CATEGORIES WHERE CATEGORY_ACCOUNT_ID = :accountId AND CATEGORY_ID NOT IN (SELECT CATEGORY_ID FROM NOTES)")
    void removeEmptyCategory(long accountId);

    @Query("SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_ACCOUNT_ID = :accountId AND CATEGORY_TITLE = :title")
    Long getCategoryIdByTitle(long accountId, String title);


    /**
     * This method is used to modify the sorting method for one category by title.
     * The user can determine use which sorting method to show the notes for a category.
     * When the user changes the sorting method, this method should be called.
     *
     * @param accountId     The user accountID
     * @param categoryTitle The category title
     * @param sortingMethod The sorting method in {@link CategorySortingMethod} enum format
     */
    @Query("UPDATE CATEGORIES SET CATEGORY_SORTING_METHOD = :sortingMethod WHERE CATEGORY_ID = (SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_ACCOUNT_ID = :accountId AND CATEGORY_TITLE = :categoryTitle)")
    void modifyCategoryOrderByTitle(long accountId, String categoryTitle, CategorySortingMethod sortingMethod);

    /**
     * This function is used to get the sorting method of a category by title.
     * The sorting method of the category can be used to decide
     * to use which sorting method to show the notes for each categories.
     *
     * @param accountId     The user accountID
     * @param categoryTitle The category title
     * @return The sorting method in {@link CategorySortingMethod} enum format
     */
    @Query("SELECT CATEGORY_SORTING_METHOD FROM CATEGORIES WHERE CATEGORY_ACCOUNT_ID = :accountId AND CATEGORY_TITLE = :categoryTitle")
    CategorySortingMethod getCategoryOrderByTitle(long accountId, String categoryTitle);

    @Query("SELECT CATEGORY_TITLE FROM CATEGORIES WHERE CATEGORY_ID = :categoryId")
    String getCategoryTitleById(long categoryId);

    @Query("SELECT CATEGORY_ID, CATEGORY_TITLE, COUNT(*) as 'totalNotes' FROM CATEGORIES INNER JOIN NOTES ON CATEGORY_ID = CATEGORY" +
            " WHERE STATUS != 'LOCAL_DELETED' AND NOTES.ACCOUNT_ID = :accountId AND CATEGORY_TITLE LIKE '%' + :categoryTitle + '%' GROUP BY CATEGORY_TITLE")
    List<CategoryWithNotesCount> searchCategories(Long accountId, String categoryTitle);
}
