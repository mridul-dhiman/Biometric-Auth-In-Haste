package com.example.biometricauth

import android.Manifest
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var biometricManager: BiometricManager
    private lateinit var keyguardManager: KeyguardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        biometricManager = getSystemService(BIOMETRIC_SERVICE) as BiometricManager
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        Log.e(TAG, "onCreate: ${keyguardManager.isKeyguardSecure}")
        if (!keyguardManager.isKeyguardSecure) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Lock screen security not enabled",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.USE_BIOMETRIC
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Permission enabled (Biometric)",
                Snackbar.LENGTH_SHORT
            ).show()

            Log.d(TAG, "onCreate: Permission enabled (Biometric)")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val canAuth =
                biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            Log.i(TAG, "onCreate: $canAuth")

            val bioPrompt: BiometricPrompt =
                BiometricPrompt.Builder(this).setTitle("BioTest")
                    .setNegativeButton("Cancel",Executors.newSingleThreadExecutor(),
                        { dialog, which -> Log.d(TAG, "onClick: cancel clicked") })
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

            val cancellationSignal = CancellationSignal()

            cancellationSignal.setOnCancelListener {
                Log.d(TAG, "onCreate: cancelled")
            }

            val biometricPromptAuthenticationCallback: BiometricPrompt.AuthenticationCallback =
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.d(TAG, "onAuthenticationError: $errString code $errorCode")
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpCode, helpString)
                        Log.d(TAG, "onAuthenticationHelp: $helpString code $helpCode")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        Log.d(TAG, "onAuthenticationSucceeded: ${result?.authenticationType}")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d(TAG, "onAuthenticationFailed:")
                    }
                }

            bioPrompt.authenticate(
                cancellationSignal,
                Executors.newSingleThreadExecutor(), biometricPromptAuthenticationCallback
            )
        }
    }
}