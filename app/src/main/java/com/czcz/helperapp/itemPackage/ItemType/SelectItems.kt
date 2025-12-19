package com.czcz.helperapp.itemPackage.ItemType

import android.os.Bundle
import androidx.core.content.edit
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import com.czcz.helperapp.TimerItemAdapter
import com.czcz.helperapp.databinding.ActivitySelectItemsBinding
import com.czcz.helperapp.itemPackage.Item.Item
import com.czcz.helperapp.itemPackage.Item.ItemDao
import com.czcz.helperapp.itemPackage.Item.ItemDatabase
import kotlinx.coroutines.launch

class SelectItems : AppCompatActivity() {
    lateinit var binding: ActivitySelectItemsBinding
    private lateinit var currentusername: String
    private lateinit var adapter: TimerItemAdapter
    private lateinit var database: ItemDatabase
    private lateinit var itemDao: ItemDao
    private val itemsList = mutableListOf<Item>()
    private var isSelectMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

       adapter = TimerItemAdapter(itemsList, this@SelectItems)
       binding.recycler.adapter = adapter
       binding.recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@SelectItems)


        database = ItemDatabase.getDatabase(this)
        itemDao = database.itemDao()

        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("currentusername", "") ?: ""

        lifecycleScope.launch {
            val items = ItemDatabase.getDatabase(this@SelectItems).itemDao().getAllItemsByUser(currentusername)
            itemsList.clear()
            itemsList.addAll(items)
        }

        binding.add.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            val unselectedItems = adapter.getUnselectedItems()
            val curType = intent.getStringExtra("itemType")
            getSharedPreferences("curType", MODE_PRIVATE).edit {
                putString("curType", curType)
            }

            lifecycleScope.launch {
               selectedItems.forEach {
                   if(it.itemType != curType) {
                       val updatedselecteditem = it.copy(itemType = curType)
                       itemDao.updateItem(updatedselecteditem)
                   }
               }

                unselectedItems.forEach {
                    if(it.itemType == curType){
                        val updatedunselecteditem = it.copy(itemType = "全部事项")
                        itemDao.updateItem(updatedunselecteditem)
                    }
                }
            }

            isSelectMode = false
            adapter.setSelectMode(isSelectMode)
            adapter.notifyDataSetChanged()
            Toast.makeText(this,"编辑成功", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.cancel.setOnClickListener {
            isSelectMode = false
            adapter.setSelectMode(isSelectMode)
            adapter.notifyDataSetChanged()
            finish()
        }
    }
}