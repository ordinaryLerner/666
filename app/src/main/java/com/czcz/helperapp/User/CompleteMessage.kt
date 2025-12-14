package com.czcz.helperapp.User

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import androidx.room.Room
import com.czcz.helperapp.User.UserDatabase
import com.czcz.helperapp.databinding.ActivityCompleteMessageBinding
import kotlinx.coroutines.launch

class CompleteMessage : AppCompatActivity() {
    private lateinit var currentusername: String
    private lateinit var currentpassword: String
    lateinit var userdatabase: UserDatabase
    private lateinit var userDao: UserDao
    lateinit var binding: ActivityCompleteMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompleteMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userdatabase = UserDatabase.getDatabase(this)
        userDao = userdatabase.userDao()
        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("currentusername", "") ?: ""
        currentpassword = getSharedPreferences("currentpassword", MODE_PRIVATE).getString("currentpassword", "") ?: ""
        binding.complete.setOnClickListener {
            val name = binding.nameedit.text.toString()  //
            val Aca_number = binding.AcaNumberedit.text.toString()
            val gender = if(binding.male.isChecked) {
                "男"
            }
                         else if(binding.female.isChecked) {
                "女"
            }
                         else {
                "未填写"
            }
            binding.namelayout.error = null
            binding.AcaNumberlayout.error = null
            if(name.isEmpty()){
                binding.namelayout.error = "请输入姓名"
                return@setOnClickListener
            }
            if(Aca_number.isEmpty()){
                binding.AcaNumberlayout.error = "请输入学号"
                return@setOnClickListener
            }
            if(gender == "未填写") {
                Toast.makeText(this, "请选择性别", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
                lifecycleScope.launch {
                    val user = userDao.getUserByUsername(currentusername)
                    if(user != null) {
                        user.name = name
                        user.Aca_number = Aca_number
                        user.gender = gender
                        userDao.updateUser(user)
                    }
                    setResult(RESULT_OK)
                    finish()
            }
        }
        binding.cancel.setOnClickListener {
            finish()
        }
    }
}