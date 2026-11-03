package com.dailydairy

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.dailydairy.backup.Backup
import com.dailydairy.notification.NotificationScheduler
import com.dailydairy.notification.NotificationStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DailyDiaryApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createChannel(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            if (Backup.hasKept(this@DailyDiaryApplication) && Backup.empty(this@DailyDiaryApplication)) {
                runCatching { Backup.restoreKept(this@DailyDiaryApplication) }
            }
            val enabled = NotificationStore.getEnabled(this@DailyDiaryApplication)
            if (enabled) {
                val hour = NotificationStore.getHour(this@DailyDiaryApplication)
                val minute = NotificationStore.getMinute(this@DailyDiaryApplication)
                NotificationScheduler.schedule(this@DailyDiaryApplication, hour, minute)
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
}
