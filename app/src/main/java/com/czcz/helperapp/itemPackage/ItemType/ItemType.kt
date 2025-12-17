package com.czcz.helperapp.itemPackage.ItemType

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itemtypes")
data class ItemType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemType: String,
    val username: String
)
