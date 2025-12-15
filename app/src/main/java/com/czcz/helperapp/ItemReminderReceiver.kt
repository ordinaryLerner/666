package com.czcz.helperapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import com.czcz.helperapp.R

class ItemReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getIntExtra("item_id", -1)
        val description = intent.getStringExtra("item_description") ?: ""
        val type = intent.getStringExtra("item_type") ?: ""
        // 发送通知
        sendNotification(context, itemId, description,type)
    }

    private fun sendNotification(context: Context, itemId: Int, description: String,type: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "item_reminder",
                "Item Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val contextText = when (type) {
            "before" -> "您的事项 $description 准备到截止时间了！"
            "deadline" -> "您的事项 $description 已经到截止时间了！"
            else -> ""
        }

        val notification = NotificationCompat.Builder(context, "item_reminder")
            .setContentTitle("待办事项提醒")
            .setContentText(contextText)
            .setSmallIcon(R.drawable.ic_notification)//设置图标
            .setAutoCancel(true)//设置点击后自动取消
            .build()

        notificationManager.notify(itemId, notification)
    }
}