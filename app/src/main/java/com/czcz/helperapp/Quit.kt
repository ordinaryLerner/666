package com.czcz.helperapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.czcz.helperapp.databinding.ActivityQuitBinding

class Quit : AppCompatActivity() {
    lateinit var binding: ActivityQuitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQuitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.yes.setOnClickListener {
            intent.putExtra("跳过自动登录", true)
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        binding.no.setOnClickListener {
            startActivity(Intent(this, Mine::class.java))
            finish()
        }
    }
}