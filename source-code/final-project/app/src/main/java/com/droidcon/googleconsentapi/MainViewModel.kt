package com.droidcon.googleconsentapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _otpText = MutableStateFlow<String?>(null)
    val otpText: StateFlow<String?>
        get() = _otpText

    fun setOTP(otp: String) {
        viewModelScope.launch {
            _otpText.value = otp
        }
    }
}
