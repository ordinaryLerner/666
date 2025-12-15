package com.czcz.helperapp

import android.os.Bundle
import androidx.room.Room
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.user.UserDatabase
import kotlinx.coroutines.launch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.czcz.helperapp.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var userDatabase: UserDatabase
    override fun onCreate(savedInstanceState: Bundle?) {//保存数据，用于恢复Activity
        super.onCreate(savedInstanceState)//引用父类oncreate
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)//引用viewbinding
        setContentView(binding.root)//设置viewbinding为contentview

        userDatabase = Room.databaseBuilder(
            applicationContext,//联系上下文，防止内存泄露
            UserDatabase::class.java,
            "user_database"
        ).build()//构建并初始化数据库

        val userDao = UserDatabase.getDatabase(applicationContext).userDao()
        val data = getSharedPreferences("data", MODE_PRIVATE)//以轻量化数据库存储保存的账号和密码，将多用户的账户和密码存储到数据库里
        val remember = data.getBoolean("remember", false)
        var auto = data.getBoolean("auto", false)
        var skipAutoLogin = intent.getBooleanExtra("跳过自动登录", false)
        val dataeditor = data.edit()//创建editor来修改保存的账户和密码，用于保存账户和密码
        val savedusername = data.getString("username", "")
        val savedpassword = data.getString("password", "")

        if(skipAutoLogin){
            dataeditor.putBoolean("auto", false)
            dataeditor.apply()
        }

        if (remember) {
            binding.usernameedit.setText(savedusername)
            binding.passwordedit.setText(savedpassword)
            binding.remember.isChecked = true
        }

        if (auto && !skipAutoLogin) {
            lifecycleScope.launch{
                val user = userDao.getUserByUsername(savedusername!!)
                var name = user?.name

                if(name == null) {
                    name = savedusername
                }

                //在协程中，表明login，明确指向login板块
                startActivity(Intent(this@Login, Home::class.java))
                Toast.makeText(this@Login, "登录成功,欢迎$name", Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }

            binding.register.setOnClickListener {
                val intent = Intent(this, Register::class.java)
                startActivity(intent)
            }

            binding.login.setOnClickListener {
                val username = binding.usernameedit.text.toString()//将输入的账号和密码保存为字符串，定义到变量里
                val password = binding.passwordedit.text.toString()
                val auto = binding.auto.isChecked
                val remember = binding.remember.isChecked

                when {
                    username.isEmpty() -> {
                        binding.usernameLayout.error = "请输入账名"
                    }

                    password.isEmpty() -> {
                        binding.passwordLayout.error = "请输入密码"
                    }
                }

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    lifecycleScope.launch {//使用协程处理数据库验证，防止主线程卡顿，保证流畅
                        val users = userDatabase.userDao().getAllUsers()
                        val user = users.find { it.username == username && it.password == password }

                        if (user != null) {
                            if (user.username == username && user.password == password) {//find寻找数据库中对应的账户和密码

                                val currentusername = getSharedPreferences("currentusername", MODE_PRIVATE)
                                currentusername.edit().putString("currentusername", username).apply()

                                val currentpassword = getSharedPreferences("currentpassword", MODE_PRIVATE)
                                currentpassword.edit().putString("currentpassword", password).apply()

                                userDao.updateUser(user)

                                val name = user.name
                                val intent = Intent(this@Login, Home::class.java)

                                if(name != null)
                                    Toast.makeText(this@Login, "登录成功,欢迎$name", Toast.LENGTH_SHORT).show()

                                else
                                    Toast.makeText(this@Login, "登录成功,欢迎$username", Toast.LENGTH_SHORT).show()

                                if (auto) {
                                    dataeditor.putBoolean("auto", binding.auto.isChecked)
                                    dataeditor.putBoolean("skipAutoLogin", false)
                                    dataeditor.putBoolean("remember", binding.remember.isChecked)
                                    dataeditor.apply()
                                }
                                startActivity(intent)
                                finish()
                            }
                        }

                        else {
                            binding.wrong.visibility = TextView.VISIBLE
                        }
                    }
                    if (remember)
                        dataeditor.putBoolean("remember", binding.remember.isChecked)

                    else
                        dataeditor.putBoolean("remember", false)

                    dataeditor.putString("username", username)
                    dataeditor.putString("password", password)
                    dataeditor.apply()
                }

                binding.usernameedit.doOnTextChanged { text, start, before, count ->
                    binding.wrong.visibility = TextView.INVISIBLE
                    binding.usernameLayout.error = null
                }

                binding.passwordedit.doOnTextChanged { text, start, before, count ->
                    binding.wrong.visibility = TextView.INVISIBLE
                    binding.passwordLayout.error = null
                }
            }
        }
    }

