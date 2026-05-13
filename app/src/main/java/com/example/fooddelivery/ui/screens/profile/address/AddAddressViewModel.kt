package com.example.fooddelivery.ui.screens.profile.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.AddAddressUseCase
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
    val buildingNote: String = "",
    val type: String = "Home",
    val isDefault: Boolean = false,
    val isEditMode: Boolean = false,
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
    data class FullAddressChanged(val address: String) : AddAddressEvent
    data class BuildingNoteChanged(val note: String) : AddAddressEvent
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
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<AddAddressRoute>()
    private val editingAddressId: String? = routeArgs.addressId

    private val _state = MutableStateFlow(AddAddressState(isEditMode = editingAddressId != null))
    val state: StateFlow<AddAddressState> = _state.asStateFlow()

    init {
        if (editingAddressId != null) {
            loadExistingAddress(editingAddressId)
        }
    }

    private fun loadExistingAddress(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000)
            val mockAddress = Address(
                id = id, 
                type = "WORK", 
                title = "My Office", 
                streetName = "Bitexco Financial Tower", 
                detail = "Floor 25, Bitexco Financial Tower", 
                isDefault = true
            )
            _state.update { it.copy(
                type = when(mockAddress.type.uppercase()) {
                    "HOME" -> "Home"
                    "WORK" -> "Work"
                    else -> "Other"
                },
                title = mockAddress.title,
                fullAddress = mockAddress.streetName,
                isDefault = mockAddress.isDefault,
                isLoading = false
            )}
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
            is AddAddressEvent.BuildingNoteChanged -> {
                _state.update { it.copy(buildingNote = event.note) }
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
        
        // Mocking Search API
        delay(800)
        val mockResults = listOf(
            Address(id = "m1", title = "Search Result 1", detail = "123 District 1, HCM", streetName = "123 District 1", city = "HCM", type = "OTHER", isDefault = false),
            Address(id = "m2", title = "Search Result 2", detail = "456 District 3, HCM", streetName = "456 District 3", city = "HCM", type = "OTHER", isDefault = false)
        ).filter { it.detail.contains(query, ignoreCase = true) }

        _state.update {
            it.copy(
                searchResults = mockResults,
                isSearching = false,
                noResultsFound = mockResults.isEmpty()
            )
        }
    }

    private fun handleSearchResultSelected(address: Address) {
        _state.update {
            it.copy(
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
            delay(1500)
            
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
