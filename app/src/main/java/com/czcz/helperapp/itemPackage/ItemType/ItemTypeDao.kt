package com.czcz.helperapp.itemPackage.ItemType

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemTypeDao {
    @Insert
    suspend fun insertItemType(itemType: ItemType)

    @Query("DELETE FROM itemtypes WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("SELECT * FROM itemtypes WHERE username = :username")
    suspend fun getAllItemTypesByUser(username: String): List<ItemType>
}