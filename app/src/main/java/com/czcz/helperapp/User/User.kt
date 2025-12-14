package com.czcz.helperapp.User

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String?=null,
    var name: String?=null,
    var Aca_number: String?=null,
    var gender: String?=null,
    var motto: String?=null
)