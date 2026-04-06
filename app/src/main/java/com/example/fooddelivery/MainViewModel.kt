package com.example.fooddelivery

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.ui.navigation.LoginRoute
import com.example.fooddelivery.ui.navigation.OnboardingRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private  val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _startDestination = mutableStateOf<Any>(OnboardingRoute)
    val startDestination: State<Any> = _startDestination

    init {
        viewModelScope.launch {
            dataStoreManager.readOnboardingState().collect { completed ->
                if (completed) {
                    _startDestination.value = LoginRoute
                }
                else {
                    _startDestination.value = OnboardingRoute
                }

                delay(300)
                _isLoading.value = false
            }
        }
    }
}