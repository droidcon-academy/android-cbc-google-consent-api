package com.droidcon.googleconsentapi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import com.droidcon.googleconsentapi.ui.screens.OtpScreen
import com.droidcon.googleconsentapi.ui.theme.GoogleConsentApiTheme
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val smsConsentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                val otp = message?.filter { it.isDigit() } ?: ""
                mainViewModel.setOTP(otp)
            }
        }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                val smsRetrieverStatus = IntentCompat.getParcelableExtra(
                    intent, SmsRetriever.EXTRA_STATUS, Status::class.java
                )
                when (smsRetrieverStatus?.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent = IntentCompat.getParcelableExtra(
                            intent, SmsRetriever.EXTRA_CONSENT_INTENT, Intent::class.java
                        )
                        try {
                            smsConsentLauncher.launch(consentIntent)
                        } catch (e: ActivityNotFoundException) {
                            Log.e(MainActivity::class.java.simpleName, e.message, e)
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        Toast.makeText(this@MainActivity, "Timeout", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoogleConsentApiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OtpScreen(
                        mainViewModel.otpText.collectAsState().value,
                        onValueChange = { otp ->
                            mainViewModel.setOTP(otp)
                        },
                    )
                }
            }
        }
        startListenForOTP()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun startListenForOTP() {
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= 34 && applicationInfo.targetSdkVersion >= 34) {
            registerReceiver(smsReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(smsReceiver, intentFilter)
        }
        SmsRetriever.getClient(this).startSmsUserConsent(null)
    }
}