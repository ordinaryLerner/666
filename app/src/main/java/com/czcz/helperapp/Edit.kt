package com.czcz.helperapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.databinding.ActivityEditBinding
import com.czcz.helperapp.user.UserDao
import com.czcz.helperapp.user.UserDatabase
import kotlinx.coroutines.launch

class Edit : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    lateinit var userdatabase: UserDatabase
    private lateinit var currentusername: String
    private lateinit var currentpassword: String
    private lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userdatabase = UserDatabase.getDatabase(this)
        userDao = userdatabase.userDao()

        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("currentusername", "") ?: ""

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.complete.setOnClickListener {
            val password = binding.passwordedit.text.toString()
            val confirm = binding.confirmedit.text.toString()

            binding.passwordlayout.error = null
            binding.confirmedit.error =  null

            when{
                password.isEmpty() -> {
                    binding.passwordlayout.error = "密码不能为空"
                }

                confirm.isEmpty() -> {
                    binding.confirmedit.error = "请再次输入密码"
                }

                password.isNotEmpty() && confirm.isNotEmpty() -> {
                    if (password == confirm) {
                        lifecycleScope.launch{
                            val user = userDao.getUserByUsername(currentusername)
                            val updateduser = user?.copy(password = password)

                            if (updateduser != null) {
                                userDao.updateUser(updateduser)
                            }

                            val currentpassword = getSharedPreferences("currentpassword", MODE_PRIVATE)
                            currentpassword.edit().putString("currentpassword", password).apply()
                        }

                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    else {
                        binding.confirmedit.error = "密码不一致"
                    }
                }
            }
        }
    }
}