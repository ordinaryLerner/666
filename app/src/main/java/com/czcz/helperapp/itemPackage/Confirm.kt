package com.czcz.helperapp.itemPackage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import android.widget.Toast
import com.czcz.helperapp.databinding.ActivityConfirmBinding
import kotlinx.coroutines.launch

class Confirm : AppCompatActivity() {
    private lateinit var database: ItemDatabase
    private lateinit var itemDao: ItemDao

    lateinit var binding: ActivityConfirmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = ItemDatabase.getDatabase(this)
        itemDao = database.itemDao()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.yes.setOnClickListener {
            deleteItem()
            finish()
        }

        binding.no.setOnClickListener {
            finish()
        }
    }

    private fun deleteItem() {
        // 从Intent中获取Item信息
        val itemId = intent.getIntExtra("item_id", -1)
        val itemusername = intent.getStringExtra("item_username")
        if (itemId != -1 && itemusername != null) {
            lifecycleScope.launch {
                // 执行删除操作
                itemDao.deleteItem(itemId)
                Toast.makeText(this@Confirm, "已删除该事项", Toast.LENGTH_SHORT).show()
                // 返回并刷新列表
                setResult(RESULT_OK)
            }
        }
    }
}