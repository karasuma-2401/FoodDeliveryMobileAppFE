package com.example.fooddelivery.ui.screens.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.AddAddressUseCase
import com.example.fooddelivery.domain.usecase.GetAddressUseCase
import com.example.fooddelivery.domain.usecase.SearchPlacesUseCase
import com.example.fooddelivery.data.remote.dto.photonPlaceTitle
import com.example.fooddelivery.domain.usecase.UpdateAddressLocationUseCase
import com.example.fooddelivery.domain.usecase.UpdateAddressUseCase
import com.example.fooddelivery.domain.repository.DeliveryLocationRepository
import com.example.fooddelivery.domain.repository.toDisplayAddressType
import com.example.fooddelivery.ui.navigation.AddAddressRoute
import com.example.fooddelivery.util.hasValidCoordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAddressState(
    val title: String = "",
    val fullAddress: String = "",
    val deliveryNote: String = "",
    val type: String = "Home",
    val isEditMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Address> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val noResultsFound: Boolean = false,
    val selectedAddress: Address? = null,
) {
    val hasPinnedLocation: Boolean
        get() = selectedAddress?.hasValidCoordinates() == true && fullAddress.isNotBlank()
}

sealed interface AddAddressEvent {
    data class TitleChanged(val title: String) : AddAddressEvent
    data class DeliveryNoteChanged(val note: String) : AddAddressEvent
    data class TypeChanged(val type: String) : AddAddressEvent
    data class SearchQueryChanged(val query: String) : AddAddressEvent
    data class SearchResultSelected(val address: Address) : AddAddressEvent
    object SaveAddressClicked : AddAddressEvent
    object ResetState : AddAddressEvent
    object ErrorDismissed : AddAddressEvent
}

private data class SavedLocationSnapshot(
    val fullText: String,
    val latitude: Double,
    val longitude: Double,
)

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val updateAddressLocationUseCase: UpdateAddressLocationUseCase,
    private val getAddressUseCase: GetAddressUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val deliveryLocationRepository: DeliveryLocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<AddAddressRoute>()
    private val editingAddressId: Int? = routeArgs.addressId

    private var savedLocationSnapshot: SavedLocationSnapshot? = null

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
                val uiType = address.type.toDisplayAddressType().let { displayType ->
                    when (displayType) {
                        "Home", "Work" -> displayType
                        else -> "Other"
                    }
                }
                savedLocationSnapshot = SavedLocationSnapshot(
                    fullText = address.detail,
                    latitude = address.latitude,
                    longitude = address.longitude,
                )
                _state.update {
                    it.copy(
                        selectedAddress = address,
                        type = uiType,
                        title = if (uiType == "Other") address.type else "",
                        fullAddress = address.detail,
                        deliveryNote = address.deliveryNote,
                        isLoading = false,
                    )
                }
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
            is AddAddressEvent.DeliveryNoteChanged -> {
                _state.update { it.copy(deliveryNote = event.note) }
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
                savedLocationSnapshot = null
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
                    noResultsFound = results.isEmpty(),
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
                noResultsFound = false,
            )
        }
    }

    private fun hasLocationChanged(fullText: String, latitude: Double, longitude: Double): Boolean {
        val snapshot = savedLocationSnapshot ?: return true
        return snapshot.fullText.trim() != fullText.trim() ||
            snapshot.latitude != latitude ||
            snapshot.longitude != longitude
    }

    fun saveAddress() {
        val currentState = _state.value
        if (currentState.isLoading) return

        val pinned = currentState.selectedAddress
        if (pinned == null || !pinned.hasValidCoordinates()) {
            _state.update {
                it.copy(errorMessage = "Please search and confirm your delivery location on the map")
            }
            return
        }
        if (currentState.fullAddress.isBlank()) {
            _state.update { it.copy(errorMessage = "Delivery address is required") }
            return
        }
        if (currentState.type == "Other" && currentState.title.isBlank()) {
            _state.update { it.copy(errorMessage = "Please provide a title for this address") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val finalType = if (currentState.type == "Other") currentState.title else currentState.type
            val physicalTitle = pinned.title.ifBlank { finalType }
            val trimmedNote = currentState.deliveryNote.trim()

            val addressToSave = pinned.copy(
                type = finalType,
                title = physicalTitle,
                detail = currentState.fullAddress,
                deliveryNote = trimmedNote,
            )

            val result = if (currentState.isEditMode && editingAddressId != null) {
                val detailsResult = updateAddressUseCase(addressToSave.copy(id = editingAddressId))
                if (detailsResult.isFailure) {
                    detailsResult
                } else if (
                    hasLocationChanged(
                        fullText = currentState.fullAddress,
                        latitude = pinned.latitude,
                        longitude = pinned.longitude,
                    )
                ) {
                    updateAddressLocationUseCase(
                        userAddressId = editingAddressId,
                        placeTitle = pinned.photonPlaceTitle(),
                        fullText = currentState.fullAddress,
                        latitude = pinned.latitude,
                        longitude = pinned.longitude,
                    )
                } else {
                    detailsResult
                }
            } else {
                addAddressUseCase(addressToSave)
            }

            result.onSuccess {
                deliveryLocationRepository.refreshAddresses()
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
