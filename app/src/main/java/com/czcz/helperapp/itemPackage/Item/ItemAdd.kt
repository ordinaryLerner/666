package com.czcz.helperapp.itemPackage.Item

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.czcz.helperapp.R
import com.czcz.helperapp.databinding.ActivityItemAddBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class ItemAdd : AppCompatActivity() {
    private lateinit var database: ItemDatabase
    private lateinit var itemDao: ItemDao
    lateinit var binding: ActivityItemAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val  currentUsername = getSharedPreferences("currentusername", MODE_PRIVATE)
        val currentusername = currentUsername.getString("currentusername", "") ?: ""

        database = ItemDatabase.getDatabase(this)
        itemDao = database.itemDao()

        binding.dateedit.setOnClickListener {
            showDateTimePicker()
        }

        binding.add.setOnClickListener {
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

            val item = Item(
                description = binding.descriptionlayout.editText?.text.toString(),
                date = binding.datelayout.editText?.text.toString(),
                username = currentusername,
                itemType = "全部事项"
            )

            lifecycleScope.launch {
                itemDao.insertItem(item)
            }

            val resualtIntent = Intent().apply {
                putExtra("description", binding.descriptionlayout.editText.toString())
                putExtra("date", binding.datelayout.editText.toString())
            }

            setResult(RESULT_OK, resualtIntent)
            Toast.makeText(this, "添加成功,将于临期发送提醒", Toast.LENGTH_SHORT).show()
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

    //日期时间选择器
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

                    val selectedDateTime =
                        formatDateTime(year, month + 1, dayOfMonth, hourOfDay, minute)
                    binding.dateedit.setText(selectedDateTime)
                },
                currentHour,
                currentMinute,
                true
            )
            timePickerDialog.show()
        },
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