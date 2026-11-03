package com.dailydairy.notification

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notifDataStore by preferencesDataStore(name = "notification_prefs")

object NotificationStore {
    private val KEY_ENABLED = booleanPreferencesKey("notif_enabled")
    private val KEY_HOUR = intPreferencesKey("notif_hour")
    private val KEY_MINUTE = intPreferencesKey("notif_minute")

    fun observeEnabled(context: Context): Flow<Boolean> =
        context.notifDataStore.data.map { it[KEY_ENABLED] ?: false }

    fun observeHour(context: Context): Flow<Int> =
        context.notifDataStore.data.map { it[KEY_HOUR] ?: 20 }

    fun observeMinute(context: Context): Flow<Int> =
        context.notifDataStore.data.map { it[KEY_MINUTE] ?: 0 }

    suspend fun setEnabled(context: Context, enabled: Boolean) {
        context.notifDataStore.edit { it[KEY_ENABLED] = enabled }
    }

    suspend fun setTime(context: Context, hour: Int, minute: Int) {
        context.notifDataStore.edit {
            it[KEY_HOUR] = hour
            it[KEY_MINUTE] = minute
        }
    }

    suspend fun getEnabled(context: Context): Boolean =
        context.notifDataStore.data.map { it[KEY_ENABLED] ?: false }.first()

    suspend fun getHour(context: Context): Int =
        context.notifDataStore.data.map { it[KEY_HOUR] ?: 20 }.first()

    suspend fun getMinute(context: Context): Int =
        context.notifDataStore.data.map { it[KEY_MINUTE] ?: 0 }.first()
}
