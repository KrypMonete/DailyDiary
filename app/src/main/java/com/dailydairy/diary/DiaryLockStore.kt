package com.dailydairy.diary

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.lockStore by preferencesDataStore(name = "diary_lock")
private val Context.deviceStore by preferencesDataStore(name = "diary_device")

data class DiaryLock(val hash: String, val hint: String)

class DiaryLockStore(private val context: Context) {
    private val hashKey = stringPreferencesKey("hash")
    private val hintKey = stringPreferencesKey("hint")
    private val fingerprintKey = booleanPreferencesKey("fingerprint")
    private val retiredKey = booleanPreferencesKey("retired_unlocks_cleared")

    val lock: Flow<DiaryLock?> = context.lockStore.data.map { prefs ->
        val hash = prefs[hashKey] ?: return@map null
        DiaryLock(hash = hash, hint = prefs[hintKey].orEmpty())
    }

    val fingerprint: Flow<Boolean> = context.deviceStore.data.map { it[fingerprintKey] == true }

    suspend fun setFingerprint(enabled: Boolean) {
        context.deviceStore.edit { it[fingerprintKey] = enabled }
    }

    suspend fun clearRetiredUnlocks() {
        context.deviceStore.edit {
            if (it[retiredKey] == true) return@edit
            it.remove(booleanPreferencesKey("face"))
            it.remove(stringPreferencesKey("face_print"))
            it.remove(stringPreferencesKey("pattern"))
            it[retiredKey] = true
        }
    }

    suspend fun save(password: String, hint: String) {
        context.lockStore.edit {
            it[hashKey] = hash(password)
            it[hintKey] = hint.trim()
        }
    }

    fun matches(password: String, stored: String): Boolean = hash(password) == stored

    private fun hash(password: String): String {
        val salt = "dailydairy-diary".toByteArray()
        val spec = PBEKeySpec(password.toCharArray(), salt, 20_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
