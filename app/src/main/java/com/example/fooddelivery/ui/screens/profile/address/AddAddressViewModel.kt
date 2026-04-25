package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.AddAddressUseCase
import com.example.fooddelivery.domain.usecase.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AddAddressState(
    val title: String = "",
    val city: String = "",
    val streetName: String = "",
    val type: String = "Home",
    val isDefault: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Address> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val noResultsFound: Boolean = false
)

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    private val addAddressUseCase: AddAddressUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase
) : ViewModel() {

    private val _state = mutableStateOf(AddAddressState())
    val state: State<AddAddressState> = _state

    fun onTitleChange(newTitle: String) {
        _state.value = _state.value.copy(title = newTitle)
    }

    fun onCityChange(newCity: String) {
        _state.value = _state.value.copy(city = newCity)
    }

    fun onStreetNameChange(newStreet: String) {
        _state.value = _state.value.copy(streetName = newStreet)
    }

    fun onTypeChange(newType: String) {
        _state.value = _state.value.copy(type = newType)
    }

    fun onDefaultChange(isDefault: Boolean) {
        _state.value = _state.value.copy(isDefault = isDefault)
    }

    fun resetState() {
        _state.value = AddAddressState()
    }

    private var searchJob: Job? = null
    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query, noResultsFound = false)
        searchJob?.cancel()
        searchJob = viewModelScope.launch { 
            delay(500)
            if(query.isNotBlank()) {
                performSearch(query) 
            } else {
                _state.value = _state.value.copy(searchResults = emptyList(), noResultsFound = false)
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _state.value = _state.value.copy(isSearching = true)
        val result = searchPlacesUseCase(query)
        result.onSuccess { list ->
            _state.value = _state.value.copy(
                searchResults = list, 
                isSearching = false,
                noResultsFound = list.isEmpty()
            )
        }.onFailure {
            _state.value = _state.value.copy(isSearching = false, noResultsFound = true)
        }
    }

    fun onSearchResultSelected(address: Address) {
        _state.value = _state.value.copy(
            streetName = address.streetName.ifBlank { address.title },
            city = address.city,
            searchResults = emptyList(),
            searchQuery = "",
            noResultsFound = false
        )
    }

    fun saveAddress() {
        val currentState = _state.value

        if (currentState.streetName.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Street name is required")
            return
        }
        
        if (currentState.type == "Other" && currentState.title.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Please provide a title for this address")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val result = withContext(Dispatchers.IO) {
                addAddressUseCase(
                    Address(
                        title = if (currentState.type == "Other") currentState.title else currentState.type,
                        streetName = currentState.streetName,
                        city = currentState.city,
                        detail = "${currentState.streetName}${if (currentState.city.isNotBlank()) ", ${currentState.city}" else ""}",
                        type = currentState.type.uppercase(),
                        isDefault = currentState.isDefault
                    )
                )
            }

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to save address"
                )
            }
        }
    }
}
