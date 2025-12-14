package com.czcz.helperapp.ItemPackage

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.czcz.helperapp.ItemPackage.Confirm
import com.czcz.helperapp.ItemPackage.ChangeItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.czcz.helperapp.Home
import com.czcz.helperapp.R
import com.czcz.helperapp.databinding.ItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var currentusername: String

class ItemAdapter(
    var topitem: Int?,
    private val context: Home,
    private val items: MutableList<Item>,
    private val onItemDeleteListener: (Item) -> Unit,
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    // ...
    class ViewHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false) // 替换为你的 item 布局文件
        return ViewHolder(binding = ItemBinding.bind(view))
    }

    // 绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        currentusername = context.getSharedPreferences("currentusername", MODE_PRIVATE)
            .getString("currentusername", "") ?: ""
        with(holder.binding) {
            description.text = item.description
            date.text = item.date
            checkbox.isChecked = item.checkbox
            //判断是否超时
            if(ItemGone(item)){
                date.setTextColor((android.graphics.Color.parseColor("#FF0000")))
            }
            else{
                date.setTextColor((android.graphics.Color.parseColor("#000000")))
            }
            //Item设置菜单
            menuButton.setOnClickListener { view ->
                val popupMenu = android.widget.PopupMenu(context, view)
                popupMenu.menuInflater.inflate(R.menu.item_more, popupMenu.menu)
                if(topitem == item.id){
                    val itemtop = popupMenu.menu.findItem(R.id.item_Top)
                    itemtop.setTitle("取消置顶")
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
                        R.id.item_Top -> {
                            if(topitem != item.id) {
                                moveItem(position)
                                topitem = item.id
                            }
                            else{
                                topitem = null
                            }
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
            if(topitem == item.id){
                itemtop.visibility = View.VISIBLE
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
    private fun deleteItemByUser(username: String, position: Int) {
        val item = items[position]
        val intent = Intent(context, Confirm::class.java)
        intent.putExtra("item_id", item.id)
        intent.putExtra("item_username", username)
        context.startActivity(intent)
    }
    private fun ChangeItem(item: Item) {
        val intent = Intent(context, ChangeItem::class.java)
        intent.putExtra("item_id", item.id)
        intent.putExtra("item_description",item.description)
        intent.putExtra("item_date",item.date)
        context.startActivityForResult(intent, 5)
    }
    private fun ItemGone(item: Item): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val date = dateFormat.parse(item.date)
            val currentTime = System.currentTimeMillis()
            val timeDifference = date.time - currentTime
            timeDifference <= 0
        } catch (e: Exception) {
            false
        }
    }
    private fun moveItem(position: Int) {
        if(position > 0) {
                val item = items.removeAt(position)
                items.add(0, item)
                notifyItemMoved(position, 0)
                notifyItemRangeChanged(0, items.size)
            }
    }
}


