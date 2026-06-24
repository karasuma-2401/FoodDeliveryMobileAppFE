package com.example.fooddelivery.ui.screens.customer.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.AddAddressUseCase
import com.example.fooddelivery.domain.usecase.GetAddressUseCase
import com.example.fooddelivery.domain.usecase.SearchPlacesUseCase
import com.example.fooddelivery.domain.usecase.UpdateAddressUseCase
import com.example.fooddelivery.ui.navigation.AddAddressRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAddressState (
    val title: String = "",
    val fullAddress: String = "",
    val type: String = "Home",
    val isEditMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Address> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val noResultsFound: Boolean = false,
    val selectedAddress: Address? = null
)

sealed interface AddAddressEvent {
    data class TitleChanged(val title: String) : AddAddressEvent
    data class FullAddressChanged(val address: String) : AddAddressEvent
    data class TypeChanged(val type: String) : AddAddressEvent
    data class SearchQueryChanged(val query: String) : AddAddressEvent
    data class SearchResultSelected(val address: Address) : AddAddressEvent
    object SaveAddressClicked : AddAddressEvent
    object ResetState: AddAddressEvent
    object ErrorDismissed: AddAddressEvent
}

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val getAddressUseCase: GetAddressUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<AddAddressRoute>()
    private val editingAddressId: Int? = routeArgs.addressId

    private val _state = MutableStateFlow(AddAddressState(isEditMode = editingAddressId != null))
    val state: StateFlow<AddAddressState> = _state.asStateFlow()

    init {
        if (editingAddressId != null) {
            loadExistingAddress(editingAddressId)
        }
    }

    private fun loadExistingAddress(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAddressUseCase(id).onSuccess { address ->
                val uiType = when (address.type) {
                    "Home", "Nhà riêng" -> "Home"
                    "Work", "Văn phòng" -> "Work"
                    else -> "Other"
                }
                _state.update { it.copy(
                    selectedAddress = address,
                    type = uiType,
                    title = if (uiType == "Other") address.type else "",
                    fullAddress = address.detail,
                    isLoading = false
                )}
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    private var searchJob: Job? = null

    fun onEvent(event: AddAddressEvent) {
        when (event) {
            is AddAddressEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddAddressEvent.FullAddressChanged -> {
                _state.update { it.copy(fullAddress = event.address) }
            }
            is AddAddressEvent.TypeChanged -> {
                _state.update { it.copy(type = event.type) }
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
        searchPlacesUseCase(query).onSuccess { results ->
            _state.update {
                it.copy(
                    searchResults = results,
                    isSearching = false,
                    noResultsFound = results.isEmpty()
                )
            }
        }.onFailure { error ->
            _state.update { it.copy(isSearching = false, errorMessage = error.message) }
        }
    }

    private fun handleSearchResultSelected(address: Address) {
        _state.update {
            it.copy(
                selectedAddress = address,
                fullAddress = address.detail,
                searchResults = emptyList(),
                searchQuery = "",
                noResultsFound = false
            )
        }
    }

    fun saveAddress() {
        val currentState = _state.value
        if (currentState.fullAddress.isBlank()) {
            _state.update { it.copy(errorMessage = "Delivery address is required") }
            return
        }
        if (currentState.isLoading) return

        if (currentState.type == "Other" && currentState.title.isBlank()) {
            _state.update { it.copy(errorMessage = "Please provide a title for this address") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val finalType = if (currentState.type == "Other") currentState.title else currentState.type
            val physicalTitle = currentState.selectedAddress?.title ?: finalType

            val addressToSave = currentState.selectedAddress?.copy(
                type = finalType,
                title = physicalTitle,
                detail = currentState.fullAddress
            ) ?: Address(
                type = finalType,
                title = physicalTitle,
                detail = currentState.fullAddress
            )

            val result = if (currentState.isEditMode && editingAddressId != null) {
                updateAddressUseCase(addressToSave.copy(id = editingAddressId))
            } else {
                addAddressUseCase(addressToSave)
            }

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
