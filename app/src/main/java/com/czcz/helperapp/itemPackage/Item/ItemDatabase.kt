package com.czcz.helperapp.itemPackage.Item

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ItemDatabase? = null//延迟加载

        fun getDatabase(context: Context): ItemDatabase {
            //互斥锁
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,//获取上下文，联系整体代码
                    ItemDatabase::class.java,
                    "item_database"
                ).build()
                INSTANCE = instance
                instance//更新数据库实例
            }
        }
    }
}