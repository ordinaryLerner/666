package com.czcz.helperapp.itemPackage.Item

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import com.czcz.helperapp.databinding.ActivityItemDetailBinding
import kotlinx.coroutines.launch

class ItemDetail : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch{
            binding.description.text = ("事项标题：" + intent.getStringExtra("item_description"))
            binding.date.text = "事项日期：" + intent.getStringExtra("item_date")
            binding.detail.text = "事项详情：" + intent.getStringExtra("item_detail")
        }
        binding.quit.setOnClickListener {
            finish()
        }
    }
}