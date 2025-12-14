package com.czcz.helperapp.ItemPackage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.czcz.helperapp.ItemPackage.Item
import com.czcz.helperapp.ItemPackage.ItemDao

@Database(entities = [Item::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null//延迟加载

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,//获取上下文，联系整体代码
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance//更新数据库实例
            }
        }
    }
}
