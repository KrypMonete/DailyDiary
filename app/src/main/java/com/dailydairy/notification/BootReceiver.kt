package com.dailydairy.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val result = goAsync()

        scope.launch {
            try {
                val enabled = NotificationStore.getEnabled(context)
                if (enabled) {
                    val hour = NotificationStore.getHour(context)
                    val minute = NotificationStore.getMinute(context)
                    NotificationScheduler.schedule(context, hour, minute)
                }
            } finally {
                result.finish()
            }
        }
    }
}
