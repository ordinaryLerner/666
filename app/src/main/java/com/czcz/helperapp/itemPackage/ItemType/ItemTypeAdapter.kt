package com.czcz.helperapp.itemPackage.ItemType

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.czcz.helperapp.TimerItemAdapter
import com.czcz.helperapp.databinding.ItemtypeLayoutBinding
import com.czcz.helperapp.itemPackage.Item.Item

class ItemTypeAdapter(
    private val itemTypes: List<ItemType>,
    private val itemsList: List<Item>,
    private val onItemClick: (ItemType) -> Unit
) : RecyclerView.Adapter<ItemTypeAdapter.ViewHolder>() {
    private var isDeleteMode = false
    private lateinit var timeradapter: TimerItemAdapter
    private val selectedItemTypes = mutableSetOf<ItemType>()
    fun setDeleteMode(deleteMode: Boolean){
        isDeleteMode = deleteMode
        if(!isDeleteMode){
            selectedItemTypes.clear()
        }
        notifyDataSetChanged()
    }


    fun getSelectedItemTypes(): Set<ItemType> = selectedItemTypes.toSet()


    class ViewHolder(val binding: ItemtypeLayoutBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemtypeLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemType = itemTypes[position]

        with(holder.binding) {
            timeradapter = TimerItemAdapter(itemsList, holder.itemView.context)
            itemTypeText.text = itemType.itemType
            root.setOnClickListener {
                onItemClick(itemType)
            }
            if(itemType.itemType != "全部事项"){
                selectitems.visibility = View.VISIBLE
                if (isDeleteMode) {
                    deletecheck.visibility = View.VISIBLE
                    deletecheck.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedItemTypes.add(itemType)
                        } else {
                            selectedItemTypes.remove(itemType)
                        }
                    }
                    root.setOnClickListener(null)//删除模式下取消点击事件,否则点击事件会触发两次
                } else {
                    deletecheck.visibility = View.GONE
                    deletecheck.setOnCheckedChangeListener(null)
                }
            }
            selectitems.setOnClickListener {
                val intent = Intent(holder.itemView.context, SelectItems::class.java)
                intent.putExtra("itemType", itemType.itemType)
                timeradapter.setSelectMode(true)
                holder.itemView.context.startActivity(intent)
            }
    }
}
    override fun getItemCount(): Int = itemTypes.size
}