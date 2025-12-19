package com.czcz.helperapp.itemPackage.ItemType

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import com.czcz.helperapp.databinding.ActivityItemTypeAddBinding
import kotlinx.coroutines.launch

class ItemTypeAdd : AppCompatActivity() {
    private lateinit var binding: ActivityItemTypeAddBinding
    private lateinit var typedatabase: ItemTypeDatabase
    private lateinit var typeDao: ItemTypeDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemTypeAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val  currentUsername = getSharedPreferences("currentusername", MODE_PRIVATE)
        val currentusername = currentUsername.getString("currentusername", "") ?: ""

        binding.add.setOnClickListener {
            if(binding.typelayout.editText?.text.isNullOrBlank()){
                binding.typelayout.error = "请填写类型"
                Toast.makeText(this, "请填写类型", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val itemType = ItemType(
                itemType = binding.typelayout.editText?.text.toString(),
                username = currentusername
            )

            typedatabase = ItemTypeDatabase.getDatabase(this)
            typeDao = typedatabase.itemTypeDao()

            lifecycleScope.launch {
                typeDao.insertItemType(itemType)
            }

            finish()
        }

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.typeedit.doOnTextChanged { text, start, before, count ->
            binding.typelayout.error = null
        }
    }
}