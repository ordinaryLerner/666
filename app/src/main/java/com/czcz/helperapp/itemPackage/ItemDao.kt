package com.czcz.helperapp.itemPackage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {
    //suspend使函数在协程中运行，避免堵塞主线程
    //为该用户添加Item
    @Insert
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    //定义且使用SQL语法
    //删除该用户的所有Item
    @Query("DELETE FROM items WHERE username = :username")
    suspend fun deleteAllItemsByUser(username: String)
    //删除该Item
    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItem(id: Int)
    //获取该Item
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): Item?
    //获取用户的所有Item
    @Query("SELECT * FROM items WHERE username = :username")
    suspend fun getItemByUser(username: String): Item?

    //创建一个删除所有Item
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM items WHERE username = :username")
    suspend fun getAllItemsByUser(username: String): List<Item>


}