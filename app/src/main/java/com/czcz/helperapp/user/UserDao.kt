package com.czcz.helperapp.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface UserDao {
    // 创建一个查询所有用户的方法
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    // 创建一个获取一个用户的方法
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Insert
    suspend fun insertUser(user: User): Long

    // 创建一个更新用户的方法
    @Update
    suspend fun updateUser(user: User)

    // 创建一个删除用户
    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUserByName(username: String)
}

