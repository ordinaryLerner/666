package com.czcz.helperapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.core.widget.doOnTextChanged
import com.czcz.helperapp.user.UserDatabase
import com.czcz.helperapp.user.User
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.czcz.helperapp.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    private lateinit var userDatabase: UserDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userDatabase = Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java,
            "user_database"
        ).build()
        binding.wrong.visibility = View.GONE
        binding.register.setOnClickListener {
            val username = binding.usernameedit.text.toString()
            val password = binding.passwordedit.text.toString()
            val confirmPassword = binding.confirmedit.text.toString()
            when {
                username.isEmpty() -> {
                    binding.usernamelayout.error = "请输入账户"
                }

                password.isEmpty() -> {
                    binding.passwordlayout.error = "请输入密码"
                }

                confirmPassword.isEmpty() -> {
                    binding.confirmlayout.error = "请再次输入密码"
                }
            }
            if (password != confirmPassword) {
                binding.confirmlayout.error = "两次输入的密码不一致"
            }
            if (password == confirmPassword && username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = User(username = username, password = password)
                    val userExists = userDatabase.userDao().getUserByUsername( username)
                    if(userExists != null) {
                        binding.wrong.visibility = View.VISIBLE
                        return@launch
                    }
                    else {
                        binding.wrong.visibility = View.GONE
                        userDatabase.userDao().insertUser(user)
                        Toast.makeText(this@Register, "注册成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            //输入时关闭错误提示
            binding.passwordedit.doOnTextChanged { text, start, before, count ->
                binding.passwordlayout.error = null
            }
            binding.confirmedit.doOnTextChanged { text, start, before, count ->
                binding.confirmlayout.error = null
            }
            binding.usernameedit.doOnTextChanged { text, start, before, count ->
                binding.usernamelayout.error = null
            }
        }
    }
}