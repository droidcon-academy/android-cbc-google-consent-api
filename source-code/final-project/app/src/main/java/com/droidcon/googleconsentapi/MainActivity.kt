package com.droidcon.googleconsentapi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
                val smsRetrieverStatus = intent.extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent =
                            intent.extras?.get(SmsRetriever.EXTRA_CONSENT_INTENT) as Intent
                        try {
                            smsConsentLauncher.launch(consentIntent)
                        } catch (e: ActivityNotFoundException) {
                            // Record the exception ...
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        // Timeout
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
        registerReceiver(smsReceiver, intentFilter)
        SmsRetriever.getClient(this).startSmsUserConsent(null)
    }
}