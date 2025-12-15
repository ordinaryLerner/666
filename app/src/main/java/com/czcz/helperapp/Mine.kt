package com.czcz.helperapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.czcz.helperapp.user.UserDatabase
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.user.ChangeMessage
import com.czcz.helperapp.user.UserDao
import com.czcz.helperapp.databinding.ActivityMineBinding
import kotlinx.coroutines.launch

class Mine : AppCompatActivity() {
    private lateinit var currentusername: String
    lateinit var binding: ActivityMineBinding
    lateinit var userdatabase: UserDatabase
    private lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userdatabase = UserDatabase.getDatabase(this)
        userDao = userdatabase.userDao()
        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("currentusername", "") ?: ""
        refreshUserInfo()
        binding.bottommenu.selectedItemId = R.id.mine
        binding.bottommenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    if (this::class.java != Home::class.java) {
                        startActivity(Intent(this, Home::class.java))
                        finish()
                    }
                }
                R.id.timer -> {
                    if (this::class.java != Timer::class.java) {
                        startActivity(Intent(this, Timer::class.java))
                        finish()
                    }
                }
                R.id.mine -> {
                    if (this::class.java != Mine::class.java) {
                        startActivity(Intent(this, Mine::class.java))
                        finish()
                    }
                }
            }
            true
        }
        //跳转退出确认
        binding.quit.setOnClickListener {
            startActivityForResult(Intent(this, Quit::class.java),2)
        }
        binding.changemessage.setOnClickListener {
            startActivityForResult(Intent(this, ChangeMessage::class.java),3)
        }
        //修改座右铭，切换状态
        binding.changemotto.setOnClickListener {
            binding.mottoedit.visibility = TextView.VISIBLE
            binding.mottotext.visibility = TextView.INVISIBLE
            binding.changemotto.visibility = TextView.INVISIBLE
            binding.changeright.visibility = TextView.VISIBLE
            lifecycleScope.launch {
                val user = userDao.getUserByUsername(currentusername)
                if (user != null) {
                    if(!user.motto.isNullOrBlank())
                        binding.mottoedit.setText(user.motto)
                }
            }
        }
        binding.changeright.setOnClickListener {
            val motto = binding.mottoedit.text.toString()
            lifecycleScope.launch {
                val user = userDao.getUserByUsername(currentusername)
                if (user != null) {
                        if (motto != user.motto) {
                            user.motto = motto
                            userDao.updateUser(user)
                            if (user.motto.isNullOrBlank())
                                binding.mottotext.text = "未设置座右铭"
                            else {
                                binding.mottotext.text = motto
                                Toast.makeText(this@Mine, "修改成功", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    binding.mottotext.visibility = TextView.VISIBLE
                    binding.mottoedit.visibility = TextView.INVISIBLE
                    binding.changemotto.visibility = TextView.VISIBLE
                    binding.changeright.visibility = TextView.INVISIBLE
            }
        }
    }
    //检测是否退出
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) { finish() }//退出数据更改
        if (requestCode == 3 && resultCode == RESULT_OK) { refreshUserInfo() }//刷新数据
    }
    //刷新数据
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val user = userDao.getUserByUsername(currentusername)
            if( user != null)
            userDao.updateUser(user)
        }
    }
    //个人信息提醒
    private fun refreshUserInfo() {
    lifecycleScope.launch {
        val user = userDao.getUserByUsername(currentusername)
        if(user != null) {
            if(user.name.isNullOrBlank()){binding.name.text = "未设置姓名"
                binding.hello.visibility = TextView.INVISIBLE}
            else{binding.name.text = user.name
                binding.hello.visibility = TextView.VISIBLE}
            binding.AcaNumber.text = if (user.Aca_number.isNullOrBlank()) "未设置学号" else user.Aca_number
            binding.mottotext.text = if (user.motto.isNullOrBlank()) "未设置座右铭" else user.motto
        }
      }
    }
}