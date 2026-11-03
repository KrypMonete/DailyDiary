package com.dailydairy.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dailydairy.MainActivity
import com.dailydairy.R
import com.dailydairy.diary.DiaryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    private val messages = listOf(
        "Günün nasıl geçti?",
        "N'aber, nasıl geçti günün?",
        "Bugün bir şeyler yazmadın.",
        "Hey! Nasılsın?",
        "Bugünü unutma, birkaç satır yaz.",
        "Günü kapatmadan önce ne düşündün?",
        "Bugün ne oldu? Anlat bakalım.",
        "Bir dakikan var mı? Günlüğünü açık bıraktın.",
    )

    override fun onReceive(context: Context, intent: Intent) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val result = goAsync()

        scope.launch {
            try {
                val enabled = NotificationStore.getEnabled(context)
                if (!enabled) return@launch

                val now = Calendar.getInstance()
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val dao = DiaryDatabase.get(context).diaryDao()
                val countToday = dao.countUpdatedBetween(
                    startOfDay.timeInMillis,
                    endOfDay.timeInMillis,
                )

                if (countToday == 0) {
                    showNotification(context)
                }

                val hour = NotificationStore.getHour(context)
                val minute = NotificationStore.getMinute(context)
                NotificationScheduler.schedule(context, hour, minute)
            } finally {
                result.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        val message = messages.random()

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Günlük")
            .setContentText(message)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context)
                .notify(NotificationScheduler.NOTIF_ID, notification)
        }
    }
}