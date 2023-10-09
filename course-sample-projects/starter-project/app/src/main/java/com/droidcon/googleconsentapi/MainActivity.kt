package com.droidcon.googleconsentapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.droidcon.googleconsentapi.ui.screens.OtpScreen
import com.droidcon.googleconsentapi.ui.theme.GoogleConsentApiTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

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
    }
}