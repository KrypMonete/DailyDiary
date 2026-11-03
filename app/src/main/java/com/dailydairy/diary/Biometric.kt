package com.dailydairy.diary

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object Biometric {
    fun fingerprintAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    fun askFingerprint(activity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit = {}) {
        ask(activity, BIOMETRIC_STRONG, "Parmak iziyle aç", onSuccess, onFail)
    }

    private fun ask(
        activity: FragmentActivity,
        authenticators: Int,
        subtitle: String,
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        onFail()
                    }
                }

                override fun onAuthenticationFailed() = Unit
            },
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Günlük")
                .setSubtitle(subtitle)
                .setNegativeButtonText("Şifreyle aç")
                .setAllowedAuthenticators(authenticators)
                .setConfirmationRequired(false)
                .build(),
        )
    }
}
