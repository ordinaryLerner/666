package com.czcz.helperapp.itemPackage.ItemType

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.czcz.helperapp.databinding.ItemtypeLayoutBinding

class ItemTypeAdapter(
    private val itemTypes: List<ItemType>,
    private val onItemClick: (ItemType) -> Unit
) : RecyclerView.Adapter<ItemTypeAdapter.ViewHolder>() {
    private var isDeleteMode = false
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
            itemTypeText.text = itemType.itemType
            root.setOnClickListener {
                onItemClick(itemType)
            }
            if(itemType.itemType != "全部事项"){
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
                    root.setOnClickListener { // 正常模式下设置点击事件
                        onItemClick(itemType)
                    }
                }
            }
    }
}
    override fun getItemCount(): Int = itemTypes.size
}