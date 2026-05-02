package com.example.fooddelivery.ui.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalSnackBarManager @Inject constructor() {
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    suspend fun showSnackBar(message: String) {
        _messages.emit(message)
    }
}
