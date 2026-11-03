package com.dailydairy

import android.app.ActivityManager
import android.app.KeyguardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dailydairy.diary.DiaryViewModel
import com.dailydairy.theme.ThemeStore
import com.dailydairy.theme.launchBackground
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val saved = ThemeStore(this).read()
        val background = launchBackground(this, saved.first, saved.second)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(background))
        window.statusBarColor = background
        window.navigationBarColor = background
        super.onCreate(savedInstanceState)
        applyTaskIcon()
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val locked = getSystemService(KeyguardManager::class.java).isKeyguardLocked
                if (locked) diaryViewModel.lockAgain()
            }
        }
        setContent {
            DailyDiaryApp(diaryViewModel = diaryViewModel)
        }
    }

    override fun onStop() {
        diaryViewModel.lockAgain()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        applyTaskIcon()
    }

    private fun applyTaskIcon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setTaskDescription(
                ActivityManager.TaskDescription.Builder()
                    .setLabel(getString(R.string.app_name))
                    .setIcon(R.drawable.icon)
                    .build(),
            )
            return
        }
        val icon = decodeAppIcon() ?: return
        @Suppress("DEPRECATION")
        setTaskDescription(
            ActivityManager.TaskDescription(getString(R.string.app_name), icon),
        )
    }

    private fun decodeAppIcon(): Bitmap? =
        BitmapFactory.decodeResource(resources, R.drawable.icon)
}
