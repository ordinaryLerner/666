package com.czcz.helperapp.itemPackage.ItemType

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [ItemType::class], version = 1)
abstract class ItemTypeDatabase : RoomDatabase() {
    abstract fun itemTypeDao(): ItemTypeDao

    companion object {
        @Volatile
        private var INSTANCE: ItemTypeDatabase? = null//延迟加载

        fun getDatabase(context: Context): ItemTypeDatabase {
            //互斥锁
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,//获取上下文，联系整体代码
                    ItemTypeDatabase::class.java,
                    "item_type_database"
                ).build()
                INSTANCE = instance
                instance//更新数据库实例
            }
        }
    }
}