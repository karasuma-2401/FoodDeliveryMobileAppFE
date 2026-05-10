package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.fastCbrt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.AddAddressUseCase
import com.example.fooddelivery.domain.usecase.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AddAddressState (
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
sealed interface AddAddressEvent {
    data class TitleChanged(val title: String) : AddAddressEvent
    data class CityChanged(val city: String) : AddAddressEvent
    data class StreetNameChanged(val street: String) : AddAddressEvent
    data class TypeChanged(val type: String) : AddAddressEvent
    data class DefaultChanged(val isDefault: Boolean) : AddAddressEvent
    data class SearchQueryChanged(val query: String) : AddAddressEvent
    data class SearchResultSelected(val address: Address) : AddAddressEvent
    object SaveAddressClicked : AddAddressEvent
    object ResetState: AddAddressEvent
    object ErrorDismissed: AddAddressEvent
}

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    private val addAddressUseCase: AddAddressUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddAddressState())
    val state: StateFlow<AddAddressState> = _state.asStateFlow()
    private var searchJob: Job?= null

    fun onEvent(event: AddAddressEvent) {
        when (event) {
            is AddAddressEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddAddressEvent.CityChanged -> {
                _state.update { it.copy(city = event.city) }
            }
            is AddAddressEvent.StreetNameChanged -> {
                _state.update { it.copy(streetName = event.street) }
            }
            is AddAddressEvent.TypeChanged -> {
                _state.update { it.copy(type = event.type) }
            }
            is AddAddressEvent.DefaultChanged -> {
                _state.update { it.copy(isDefault = event.isDefault) }
            }
            is AddAddressEvent.SearchQueryChanged -> {
                handleSearchQueryChange(event.query)
            }
            is AddAddressEvent.SearchResultSelected -> {
                handleSearchResultSelected(event.address)
            }
            AddAddressEvent.SaveAddressClicked -> {
                saveAddress()
            }
            AddAddressEvent.ResetState -> {
                _state.value = AddAddressState()
            }
            is AddAddressEvent.ErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }
    private fun handleSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query, noResultsFound = false) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (query.isNotBlank()) {
                performSearch(query)
            } else {
                _state.update { it.copy(searchResults = emptyList(), noResultsFound = false) }
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _state.update { it.copy(isSearching = true) }
        val result = searchPlacesUseCase(query)
        result.onSuccess { list ->
            _state.update {
                it.copy(
                    searchResults = list,
                    isSearching = false,
                    noResultsFound = list.isEmpty()
                )
            }
        }.onFailure {
            _state.update { it.copy(isSearching = false, noResultsFound = true) }
        }
    }

    private fun handleSearchResultSelected(address: Address) {
        _state.update {
            it.copy(
                streetName = address.streetName.ifBlank { address.title },
                city = address.city,
                searchResults = emptyList(),
                searchQuery = "",
                noResultsFound = false
            )
        }
    }

    fun saveAddress() {
        val currentState = _state.value

        if (currentState.isLoading) {
            return
        }

        if (currentState.streetName.isBlank()) {
            _state.update {  it.copy(errorMessage = "Street name is required") }
            return
        }

        if (currentState.type == "Other" && currentState.title.isBlank()) {
            _state.update { it.copy(errorMessage = "Please provide a title for this address") }
            return
        }

        _state.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
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
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to save address"
                    )
                }
            }
        }
    }
}
