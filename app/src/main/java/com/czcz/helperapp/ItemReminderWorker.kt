package com.czcz.helperapp

import android.content.Context
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.czcz.helperapp.R

class ItemReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // 从 inputData 中获取参数
        val itemId = inputData.getInt("item_id", -1)
        val description = inputData.getString("item_description") ?: ""
        val type = inputData.getString("item_type") ?: ""
        val name = inputData.getString("item_name") ?: ""
        val gender = inputData.getString("item_gender") ?: ""

        // 发送通知
        sendNotification(itemId, description, type, name, gender)

        return Result.success()
    }

    private fun sendNotification(itemId: Int, description: String, type: String, name: String, gender: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "item_reminder",
                "Item Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val contextText = when(gender){
            "male" -> "$name 先生，"
            "female" -> "$name 女士，"
            else -> "$name ,"
        } +
        when (type) {
            "before" -> "您的事项 $description 准备到截止时间了！"
            "deadline" -> "您的事项 $description 已经到截止时间了！"
            else -> "您的有事项待办，请查看！"
        }

        val intent = Intent(applicationContext, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("item_id", itemId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            itemId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "item_reminder")
            .setContentTitle("待办事项提醒")
            .setContentText(contextText)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(itemId, notification)
    }
}
