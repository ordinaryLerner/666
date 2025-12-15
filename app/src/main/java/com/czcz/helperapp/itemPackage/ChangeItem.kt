package com.czcz.helperapp.itemPackage

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import com.czcz.helperapp.databinding.ActivityChanegItemBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class ChangeItem : AppCompatActivity() {
    private lateinit var database: ItemDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var currentusername: String
    lateinit var binding: ActivityChanegItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChanegItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        currentusername = getSharedPreferences("currentusername", MODE_PRIVATE).getString("username", "") ?: ""
        binding.descriptionedit.setText(intent.getStringExtra("item_description"))
        database = ItemDatabase.getDatabase(this)
        itemDao = database.itemDao()
        binding.dateedit.setText(intent.getStringExtra("item_date"))
        //日期选择器
        binding.dateedit.setOnClickListener {
            showDateTimePicker()
        }
        binding.change.setOnClickListener {
            if(binding.descriptionlayout.editText?.text.isNullOrBlank()){
                binding.descriptionlayout.error = "请填写具体内容"
                Toast.makeText(this, "请填写具体内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(binding.datelayout.editText?.text.isNullOrBlank()){
                binding.datelayout.error = "请选择日期"
                Toast.makeText(this, "请选择日期", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val itemId = intent.getIntExtra("item_id", -1)
                val item = itemDao.getItemById(itemId)
                if(item !=  null) {
                    val updateditem = item.copy(
                        description = binding.descriptionedit.text.toString(),
                        date = binding.dateedit.text.toString()
                    )
                    itemDao.updateItem(updateditem)
                }
            }
            Toast.makeText(this, "修改成功,将于临期1小时发送提醒", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.cancel.setOnClickListener {
            finish()
        }
        binding.descriptionedit.doOnTextChanged { text, start, before, count ->
            binding.descriptionlayout.error = null
            binding.datelayout.error = null
        }
        binding.dateedit.doOnTextChanged { text, start, before, count ->
            binding.descriptionlayout.error = null
            binding.datelayout.error = null
        }
    }
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // 日期选择器
        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->
                // 检查选择的日期是否在当前日期之后
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val currentDate = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, currentDay)
                }

                if (selectedDate.before(currentDate)) {
                    // 如果选择的日期早于当前日期，显示提示或重新选择
                    showDateTimePicker() // 重新显示选择器
                    return@DatePickerDialog
                }

                // 时间选择器
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        // 如果选择的是当天，需要检查时间是否在当前时间之后
                        if (year == currentYear && month == currentMonth && dayOfMonth == currentDay) {
                            if (hourOfDay < currentHour || (hourOfDay == currentHour && minute <= currentMinute)) {
                                // 时间早于或等于当前时间，重新选择时间
                                showDateTimePicker()
                                Toast.makeText(this, "请选择正确的时间", Toast.LENGTH_SHORT).show()
                                return@TimePickerDialog
                            }
                        }
                        //Kotlin的月份范围为0-11，故月份要加1
                        val selectedDateTime = formatDateTime(year, month + 1, dayOfMonth, hourOfDay, minute)
                        binding.dateedit.setText(selectedDateTime)
                    },
                    // 初始化时间选择器
                    currentHour,
                    currentMinute,
                    true
                )
                timePickerDialog.show()
            },
            // 初始化日期选择器
            currentYear,
            currentMonth,
            currentDay
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    //格式化时间
    private fun formatDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
        return String.format("%d-%d-%d %d:%02d", year, month, day, hour, minute)
    }
}