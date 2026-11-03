package com.dailydairy.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NotificationScheduler {
    const val CHANNEL_ID = "diary_reminder"
    const val NOTIF_ID = 1001
    private const val REQUEST_CODE = 2001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Günlük hatırlatıcısı",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "O gün hiç yazmadıysan sana hatırlatır."
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = pendingIntent(context)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}