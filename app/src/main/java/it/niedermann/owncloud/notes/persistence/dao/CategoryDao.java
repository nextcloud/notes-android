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

    @Query("SELECT * FROM CATEGORY WHERE id = :id")
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
    @Query("DELETE FROM CATEGORY WHERE accountId = :accountId AND id NOT IN (SELECT categoryId FROM NOTE)")
    void removeEmptyCategory(long accountId);

    @Query("SELECT id FROM CATEGORY WHERE accountId = :accountId AND title = :title")
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
    @Query("UPDATE CATEGORY SET sortingMethod = :sortingMethod WHERE id = (SELECT id FROM CATEGORY WHERE accountId = :accountId AND title = :categoryTitle)")
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
    @Query("SELECT sortingMethod FROM CATEGORY WHERE accountId = :accountId AND title = :categoryTitle")
    CategorySortingMethod getCategoryOrderByTitle(long accountId, String categoryTitle);

    @Query("SELECT title FROM CATEGORY WHERE id = :id")
    String getCategoryTitleById(long id);

    @Query("SELECT CATEGORY.id, CATEGORY.title, COUNT(*) as 'totalNotes' FROM CATEGORY INNER JOIN NOTE ON categoryId = CATEGORY.id WHERE STATUS != 'LOCAL_DELETED' AND CATEGORY.accountId = :accountId AND CATEGORY.title LIKE '%' + :categoryTitle + '%' GROUP BY CATEGORY.title")
    List<CategoryWithNotesCount> searchCategories(Long accountId, String categoryTitle);
}
