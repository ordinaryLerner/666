package com.czcz.helperapp.ItemPackage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val description: String,
    val date: String,
    val checkbox: Boolean = false,
    val itemTop: Boolean = false
)