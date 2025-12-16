package com.czcz.helperapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.itemPackage.Item.ItemDatabase
import com.czcz.helperapp.itemPackage.Item.Item
import com.czcz.helperapp.itemPackage.Item.ItemDao
import com.czcz.helperapp.databinding.ActivityTimerBinding
import kotlinx.coroutines.launch

class Timer : AppCompatActivity() {
    lateinit var binding: ActivityTimerBinding
    private lateinit var currentusername: String
    private lateinit var database: ItemDatabase
    private lateinit var itemDao: ItemDao
    private var countDownTimer: CountDownTimer? = null
    private var originalTime: Long = 25 * 60 * 1000
    private var timeLeftInMillis: Long = 25 * 60 * 1000  // 剩余时间(毫秒)
    private val itemList = mutableListOf<Item>()
    private var isTimerRunning = false                   // 计时器运行状态
    private var isWorkSession = true
    private var isUpdatingText = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = ItemDatabase.getDatabase(this)
        itemDao = database.itemDao()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()    // 设置按钮点击监听器
        updateTimerDisplay()     // 初始化时间显示
        limit()

        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("currentusername", "") ?: ""

        setupRecyclerView()
        loadData()

        binding.bottommenu.selectedItemId = R.id.timer
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

        binding.modelchange.setOnClickListener { menuItem ->
            val popupMenu = PopupMenu(this, binding.modelchange)
            popupMenu.menuInflater.inflate(R.menu.timer_model, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.worktime -> {
                        isWorkSession = true
                        binding.statusText.text = "工作时间"
                        timeLeftInMillis = 25 * 60 * 1000
                        updateTimerDisplay()
                        true
                    }

                    R.id.resttime -> {
                        isWorkSession = false
                        binding.statusText.text = "休息时间"
                        timeLeftInMillis = 5 * 60 * 1000
                        updateTimerDisplay()
                        true
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun setupRecyclerView() {
        // 从数据库加载数据
        lifecycleScope.launch {
            val items = itemDao.getAllItemsByUser(currentusername)//从数据库获取所有数据，定义变量
            itemList.clear()//清空列表
            itemList.addAll(items)//添加数据

            // 设置适配器
            binding.recycler.apply {
                adapter = TimerItemAdapter(itemList, this@Timer)
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@Timer)
            }
        }
    }

    private fun switchSession() {
        isWorkSession = !isWorkSession  // 切换模式

        // 根据当前模式设置时间和状态显示
        timeLeftInMillis = if (isWorkSession) {
            binding.statusText.text = "工作时间"
            25 * 60 * 1000
        }

        else {
            binding.statusText.text = "休息时间"
            5 * 60 * 1000
        }

        updateTimerDisplay()  // 更新时间显示
    }

    private fun updateTimerDisplay() {
        // 将毫秒转换为分钟和秒
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        if (isTimerRunning) {
            // 计时器运行时，显示 TextView，隐藏输入框
            binding.minutes.visibility = android.view.View.VISIBLE
            binding.seconds.visibility = android.view.View.VISIBLE
            binding.input1.visibility = android.view.View.GONE
            binding.input2.visibility = android.view.View.GONE

            // 在 TextView 中显示时间
            binding.minutes.text = String.format("%02d", minutes)
            binding.seconds.text = String.format("%02d", seconds)
        }

        else {
            // 计时器未运行时，显示输入框，隐藏 TextView
            binding.minutes.visibility = android.view.View.GONE
            binding.seconds.visibility = android.view.View.GONE
            binding.input1.visibility = android.view.View.VISIBLE
            binding.input2.visibility = android.view.View.VISIBLE

            // 在输入框中显示时间
            binding.input1.setText(String.format("%02d", minutes))
            binding.input2.setText(String.format("%02d", seconds))
        }
    }

    private fun setupClickListeners() {
        binding.controlButton.setOnClickListener {
            if (!isTimerRunning) {
                val changedminutes = binding.input1.text.toString()
                val changedseconds = binding.input2.text.toString()

                if (changedminutes.isNotEmpty() && changedseconds.isNotEmpty()) {
                    timeLeftInMillis = changedminutes.toLong() * 60 * 1000 + changedseconds.toLong() * 1000
                    originalTime = changedminutes.toLong() * 60 * 1000 + changedseconds.toLong() * 1000
                    updateTimerDisplay()
                }
            }

            if (isTimerRunning) {
                pauseTimer()
                binding.controlButton.text = "继续"
            }

            else {
                startTimer()
                binding.controlButton.text = "暂停"
            }
        }

        binding.resetButton.setOnClickListener {
            resetTimer()
            binding.controlButton.text = "开始"
        }   // 重置按钮
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()  // 取消计时器
        isTimerRunning = false    // 更新运行状态
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            updateTimerDisplay()
            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerDisplay()
                }

                override fun onFinish() {
                    isTimerRunning = false
                    switchSession()
                    startTimer()
                }
            }.start()

            isTimerRunning = true
        }
    }

    // 限制时间输入
    private fun limit() {
        binding.input1.doAfterTextChanged { editable ->
            editable?.toString()?.let { text ->//将edit输入转换为字符串，并且赋值给text，便于使用

                if (text.isNotEmpty() && text != (binding.input1.tag as? String)) {
                    val value = text.toIntOrNull()

                    if (value == null) {
                        isUpdatingText = true
                        binding.input1.setText("00")
                        binding.input1.setSelection(binding.input1.text?.length ?: 0)
                        binding.input1.tag = "00"
                        isUpdatingText = false
                    }

                    else {
                        val maxValue = if (isWorkSession) 25 else 10

                        if (value > maxValue) {
                            isUpdatingText = true
                            binding.input1.tag = maxValue.toString() // 使用 tag 避免循环
                            binding.input1.setText(maxValue.toString())
                            binding.input1.setSelection(binding.input1.text?.length ?: 0)
                            Toast.makeText(this, "超出范围", Toast.LENGTH_SHORT).show()
                            binding.input1.tag = ""
                            isUpdatingText = false
                        }
                    }
                }
            }
        }

        binding.input2.doAfterTextChanged { editable ->
            editable?.toString()?.let { text ->
                if (text.isNotEmpty() && text != (binding.input2.tag as? String)) {
                    val value = text.toIntOrNull()

                    if (value == null) {
                        isUpdatingText = true
                        binding.input2.setText("00")
                        binding.input2.setSelection(binding.input2.text?.length ?: 0)
                        isUpdatingText = false
                    }

                    else if (value >= 60) {
                        isUpdatingText = true
                        binding.input2.tag = "59" // 使用 tag 避免循环
                        binding.input2.setText("59")
                        binding.input2.setSelection(binding.input2.text?.length ?: 0)
                        Toast.makeText(this, "秒数不能大于59", Toast.LENGTH_SHORT).show()
                        binding.input2.tag = ""
                        isUpdatingText = false
                    }
                }
            }
        }

        binding.input1.doBeforeTextChanged { text, start, count, after ->
            if(binding.input2.text.toString().isEmpty()){
                isUpdatingText = true
                binding.input2.setText("00")
                isUpdatingText = false
            }
        }

        binding.input1.doBeforeTextChanged { text, start, count, after ->
            if(binding.input2.text.toString().isEmpty()){
                isUpdatingText = true
                binding.input2.setText("00")
                isUpdatingText = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isWorkSession = true
        timeLeftInMillis = originalTime
        updateTimerDisplay()
        binding.statusText.text = "工作时间"
    }

    private fun loadData() {
        lifecycleScope.launch {
            val items = itemDao.getAllItemsByUser(currentusername)//从数据库获取所有数据，定义变量
            itemList.clear()//清空列表
            itemList.addAll(items)//添加数据
            binding.recycler.adapter?.notifyDataSetChanged()
        }
    }
}