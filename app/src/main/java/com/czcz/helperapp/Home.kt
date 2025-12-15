package com.czcz.helperapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.databinding.ActivityHomeBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.czcz.helperapp.itemPackage.ItemDatabase
import com.czcz.helperapp.itemPackage.Item
import com.czcz.helperapp.itemPackage.ItemAdapter
import com.czcz.helperapp.itemPackage.ItemAdd
import com.czcz.helperapp.itemPackage.ItemDao
import com.czcz.helperapp.user.CompleteMessage
import com.czcz.helperapp.user.UserDao
import com.czcz.helperapp.user.UserDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class Home : AppCompatActivity() {
    private lateinit var currentusername: String
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: ItemDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var userDao: UserDao
    private lateinit var userdatabase: UserDatabase
    private val itemList = mutableListOf<Item>()
    private lateinit var adapter: ItemAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化数据库
        database = ItemDatabase.getDatabase(this)
        itemDao = database.itemDao()
        userdatabase = UserDatabase.getDatabase(this)
        userDao = userdatabase.userDao()
        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("currentusername", "") ?: ""
        //创建适应器实例
        //回调模式：将删除方法调用到适配器中，不用在主线程执行删除，避免阻塞主线程
        adapter = ItemAdapter(null,this, itemList,) { item ->
           loadData()
        }
        binding.recycler.adapter = adapter//设置自定义适配器作为RecyclerView的适配器
        binding.recycler.layoutManager = LinearLayoutManager(this)//设置布局管理器为线性布局

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets//实现自适应窗口，避免内容被遮挡
        }
        loadData()
        judge()
        checkUserInfoComplete(currentusername)

        //底部导航栏
        binding.bottommenu.selectedItemId = R.id.home
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
    }

    //创建Item处理菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)//读取菜单文件
        return super.onCreateOptionsMenu(menu)
    }

    //菜单选项点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                startActivityForResult(Intent(this, ItemAdd::class.java), 1)
                loadData()
            }

            R.id.clean -> {
                lifecycleScope.launch {
                    itemDao.deleteAllItems()
                    loadData()
                }
            }

            R.id.reload -> {
                loadData()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Item事件比较排序
    private fun compareDateTime(dateTime1: String, dateTime2: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val date1 = dateFormat.parse(dateTime1)
            val date2 = dateFormat.parse(dateTime2)
            val currentTime = System.currentTimeMillis()

            val isExpired1 = date1.time < currentTime
            val isExpired2 = date2.time < currentTime

            when {
                isExpired1 && !isExpired2 -> 1       // item1过期，item2未过期，item1排在后面
                !isExpired1 && isExpired2 -> -1      // item1未过期，item2过期，item2排在后面
                else -> date1.compareTo(date2)       // 都过期或都未过期，按时间排序
            }
        } catch (e: Exception) {//捕获可能出现的错误
            0//在出现问题是返回0
        }
    }

    //重新加载数据
    private fun loadData() {
        lifecycleScope.launch {
            val items = itemDao.getAllItemsByUser(currentusername)//从数据库获取所有数据，定义变量
            itemList.clear()//清空列表
            itemList.addAll(items)//添加数据
            itemList.sortWith { item1, item2 ->
                compareDateTime(item1.date, item2.date)
            }
            adapter.notifyDataSetChanged()//通知适配器数据已改变
            adapter.notifyItemRangeChanged(0, itemList.size)
            judge()
            scheduleItemReminders()
        }
    }

    //Item列表为空判断，显示提示
    fun judge() {
        if (itemList.isEmpty()) {
            binding.hint.visibility = View.VISIBLE
        } else {
            binding.hint.visibility = View.GONE
        }
    }

    //重新加载Home时刷新数据
    override fun onResume() {
        super.onResume()
        judge()
        loadData()
    }

    //检查用户信息是否完整
    private fun checkUserInfoComplete(username: String) {
        lifecycleScope.launch {
            val user = userDao.getUserByUsername(username)
            if (user == null ||
                user.name.isNullOrEmpty() ||
                user.Aca_number.isNullOrEmpty() ||
                user.gender.isNullOrEmpty()
            ) {
                val intent = Intent(this@Home, CompleteMessage::class.java)
                //确定该Avtivity为高优先级
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or//新任务
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or//清除栈顶
                            Intent.FLAG_ACTIVITY_SINGLE_TOP//仅有一个
                )
                startActivity(intent)
            }
        }
    }
    //设置Item提醒
    private fun scheduleItemReminders() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager//获取系统闹钟服务

        itemList.forEach { item ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val triggerTime = dateFormat.parse(item.date)?.time ?: return@forEach//跳出一次循环
            val currenttime = System.currentTimeMillis()
            // 提前1小时提醒
            val reminderTime = triggerTime - (60 * 60 * 1000)
            if (reminderTime > currenttime) {
                val intent = Intent(this, ItemReminderReceiver::class.java)
                intent.putExtra("item_id", item.id)
                intent.putExtra("item_description", item.description)
                intent.putExtra("item_type", "before")
                //实例pendingintent：延迟执行
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    item.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                //Rtc_WAKEUP使用实时闹钟，并且唤醒设备，进行pendingintent发送广播信息
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            }
            //到期发送提醒
            if(triggerTime > currenttime){
                val intent = Intent(this, ItemReminderReceiver::class.java)
                intent.putExtra("item_id", item.id)
                intent.putExtra("item_description", item.description)
                intent.putExtra("item_type","deadline")
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    item.id.toInt() + 1,//保证提醒的唯一性
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }
}

