package com.czcz.helperapp

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.czcz.helperapp.itemPackage.Complete
import com.czcz.helperapp.itemPackage.Item.Item
import com.czcz.helperapp.databinding.TimerLayoutBinding

class TimerItemAdapter(private val items: List<Item>, private val context: Context) : RecyclerView.Adapter<TimerItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: TimerLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TimerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val currentusername = context.getSharedPreferences("currentusername", MODE_PRIVATE)
            .getString("currentusername", "") ?: ""

        with(holder.binding) {
            description.text = item.description
            checkbox.isChecked = item.checkbox
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
    override fun getItemCount() = items.size//获取数据的数量
}