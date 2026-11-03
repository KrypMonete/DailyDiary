package com.dailydairy.shopping

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query(
        """
        SELECT l.*,
            (SELECT COUNT(*) FROM shopping_items i WHERE i.listId = l.id AND i.unit != 'step') AS total,
            (SELECT COUNT(*) FROM shopping_items i WHERE i.listId = l.id AND i.done = 0 AND i.unit != 'step') AS remaining
        FROM shopping_lists l
        ORDER BY l.updatedAt DESC
        """,
    )
    fun observeLists(): Flow<List<ShoppingListCard>>

    @Query("SELECT COUNT(*) FROM shopping_lists")
    suspend fun listCount(): Int

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY done ASC, position ASC, id ASC")
    fun observeItems(listId: Long): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items")
    fun observeAllItems(): Flow<List<ShoppingItem>>

    @Insert
    suspend fun insertList(list: ShoppingList): Long

    @Insert
    suspend fun insertItem(item: ShoppingItem): Long

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Query("UPDATE shopping_lists SET tint = :icon WHERE id = :id")
    suspend fun setIcon(id: Long, icon: Int)

    @Query("UPDATE shopping_lists SET name = :name, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: Long, name: String, now: Long)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM shopping_items WHERE id IN (:ids)")
    suspend fun deleteItemsById(ids: List<Long>)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND done = 1")
    suspend fun clearDone(listId: Long)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteList(id: Long)

    @Query("DELETE FROM shopping_items WHERE listId IN (:ids)")
    suspend fun deleteItemsOf(ids: List<Long>)

    @Query("DELETE FROM shopping_lists WHERE id IN (:ids)")
    suspend fun deleteLists(ids: List<Long>)

    @Query("DELETE FROM shopping_items WHERE listId = :listId")
    suspend fun deleteItems(listId: Long)

    @Query("UPDATE shopping_lists SET coverImage = :image, updatedAt = :now WHERE id = :id")
    suspend fun setCoverImage(id: Long, image: String, now: Long)

    @Query("UPDATE shopping_lists SET updatedAt = :now WHERE id = :id")
    suspend fun touch(id: Long, now: Long)

    @Query("SELECT COALESCE(MAX(position), 0) FROM shopping_items WHERE listId = :listId")
    suspend fun maxPosition(listId: Long): Int
}
