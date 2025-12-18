package com.czcz.helperapp.itemPackage.Item

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.czcz.helperapp.Home
import com.czcz.helperapp.R
import com.czcz.helperapp.databinding.ItemLayoutBinding
import com.czcz.helperapp.itemPackage.ChangeItem
import com.czcz.helperapp.itemPackage.Complete
import com.czcz.helperapp.itemPackage.Confirm
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var currentusername: String

class ItemAdapter(
    var topitem: Int?,//标记置顶Item
    private val context: Home,
    private val items: MutableList<Item>,
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    // ...
    class ViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false) // 替换为你的 item 布局文件,不添加到父容器
        return ViewHolder(binding = ItemLayoutBinding.bind(view))
    }

    // 绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        currentusername = context.getSharedPreferences("currentusername", MODE_PRIVATE)
            .getString("currentusername", "") ?: ""
        //作用域函数，减少重复代码
        with(holder.binding) {
            description.text = item.description
            date.text = item.date
            checkbox.isChecked = item.checkbox
            if(topitem == item.id){ itemtop.visibility = View.VISIBLE }
            //判断是否超时
            if(ItemGone(item)){
                date.setTextColor("#FF0000".toColorInt())
            }
            else{
                date.setTextColor("#000000".toColorInt())
            }
            //Item设置菜单
            menuButton.setOnClickListener { view ->
                val popupMenu = PopupMenu(context, view)
                popupMenu.menuInflater.inflate(R.menu.item_more, popupMenu.menu)

                if(topitem == item.id){
                    val itemtop = popupMenu.menu.findItem(R.id.item_Top)
                    itemtop.title="取消置顶"
                }

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.item_change -> {
                            ChangeItem(item)
                            true
                        }

                        R.id.item_delete -> {
                            deleteItemByUser(currentusername, position)
                            true
                        }
                        //置顶操作
                        R.id.item_Top -> {
                            if(topitem != item.id) {
                                topitem = item.id
                            }
                            else{
                                topitem = null
                            }
                            sortItems()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }

            if (position == 0) {
                will.visibility = View.VISIBLE
                send.visibility = View.VISIBLE
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val intent = Intent(context, Complete::class.java)
                    intent.putExtra("item_id", item.id)
                    intent.putExtra("item_username", currentusername)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    //删除对应的Item
    private fun deleteItemByUser(username: String, position: Int) {
        val item = items[position]
        val intent = Intent(context, Confirm::class.java)
        intent.putExtra("item_id", item.id)
        intent.putExtra("item_username", username)
        context.startActivity(intent)
    }
    //修改对应的Item
    private fun ChangeItem(item: Item) {
        val intent = Intent(context, ChangeItem::class.java)
        intent.putExtra("item_id", item.id)
        intent.putExtra("item_description",item.description)
        intent.putExtra("item_date",item.date)
        context.startActivityForResult(intent, 5)
    }
    //判断Item是否超时
    private fun ItemGone(item: Item): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val date = dateFormat.parse(item.date)

            if(date != null){
                val currentTime = System.currentTimeMillis()
                val timeDifference = date.time - currentTime
                timeDifference <= 0
            }
            else{false}

        } catch (e: Exception) {
            false
        }
    }
    fun sortItems() {
        val originalItems = items.toList() // 保存原始顺序

        items.sortWith { item1, item2 ->
            when {
                topitem != null && item1.id == topitem -> -1
                topitem != null && item2.id == topitem -> 1
                else -> {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    try {
                        val date1 = dateFormat.parse(item1.date)
                        val date2 = dateFormat.parse(item2.date)
                        val currentTime = System.currentTimeMillis()

                        if (date1 != null && date2 != null) {
                            val isExpired1 = date1.time < currentTime
                            val isExpired2 = date2.time < currentTime
                            when {
                                isExpired1 && !isExpired2 -> 1
                                !isExpired1 && isExpired2 -> -1
                                else -> date1.compareTo(date2)
                            }
                        } else { 0 }

                    }catch (e: Exception) { 0 }
                }
            }
        }
        // 比较前后顺序变化并发送精确通知
        originalItems.forEachIndexed { index, item ->
            val newIndex = items.indexOf(item)

            if (index != newIndex) {

                if (newIndex >= 0) {
                    notifyItemMoved(index, newIndex)
                }

                else {
                    notifyItemRemoved(index)
                }
            }
        }
        // 通知可见范围内的项目更新
        notifyItemRangeChanged(0, items.size)
    }
}


