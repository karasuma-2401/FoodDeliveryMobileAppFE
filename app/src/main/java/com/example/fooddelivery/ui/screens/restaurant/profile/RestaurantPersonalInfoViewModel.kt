package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.fooddelivery.data.remote.dto.RestaurantResponse
import com.example.fooddelivery.domain.repository.AddressRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File

private data class LoadedProfileSnapshot(
    val name: String,
    val phone: String,
    val description: String,
    val addressId: Int?,
    val imageUrl: String?,
)

@HiltViewModel
class RestaurantPersonalInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RestaurantRepository,
    private val addressRepository: AddressRepository
) : ViewModel() {

    var uiState by mutableStateOf(RestaurantPersonalInfoState())
        private set

    private var selectedImageFile: File? = null
    private var loadedSnapshot: LoadedProfileSnapshot? = null
    private var currentRestaurantId: Int = -1

    init {
        val isFromSignUp: Boolean = savedStateHandle["isFromSignUp"] ?: false
        uiState = uiState.copy(isFromSignUp = isFromSignUp)

        if (!isFromSignUp) {
            loadCurrentRestaurantProfile()
        }
    }

    private fun loadCurrentRestaurantProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            if (currentRestaurantId != -1) {
                val result = repository.getRestaurantById(currentRestaurantId)
                result.onSuccess { profile ->
                    applyLoadedProfile(profile)
                    resolveAddressIfMissing()
                    commitSnapshotFromCurrentUi()
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load profile"
                    )
                }
            } else {
                val myRestaurantsResult = repository.getMyRestaurants()
                myRestaurantsResult.onSuccess { list ->
                    val myRestaurant = list.firstOrNull()
                    if (myRestaurant != null) {
                        currentRestaurantId = myRestaurant.id
                        applyLoadedProfile(myRestaurant)
                        resolveAddressIfMissing()
                        commitSnapshotFromCurrentUi()
                    } else {
                        uiState = uiState.copy(isLoading = false)
                    }
                }.onFailure {
                    uiState = uiState.copy(isLoading = false, error = "Failed to load your restaurant")
                }
            }
        }
    }

    private fun applyLoadedProfile(
        profile: RestaurantResponse,
        preservePendingImage: Boolean = uiState.hasNewImage,
    ) {
        val addressText = profile.address?.fullText?.takeIf { it.isNotBlank() }
            ?: profile.address?.title.orEmpty()
        val serverImageUrl = profile.image?.takeIf { it.isNotBlank() }
            ?: profile.coverImage?.takeIf { it.isNotBlank() }
        val previousImageUrl = loadedSnapshot?.imageUrl
            ?: uiState.imageUrl?.takeUnless { it.startsWith("content://") }
        val resolvedImageUrl = when {
            preservePendingImage -> uiState.imageUrl
            serverImageUrl != null -> serverImageUrl
            else -> previousImageUrl
        }
        val resolvedAddressId = profile.address?.id ?: uiState.addressId

        loadedSnapshot = LoadedProfileSnapshot(
            name = profile.name,
            phone = profile.phone.orEmpty(),
            description = profile.description.orEmpty(),
            addressId = resolvedAddressId,
            imageUrl = serverImageUrl ?: previousImageUrl,
        )

        uiState = uiState.copy(
            name = profile.name,
            phone = profile.phone ?: "",
            description = profile.description ?: "",
            imageUrl = resolvedImageUrl,
            addressId = resolvedAddressId,
            selectedAddressText = addressText.ifBlank { uiState.selectedAddressText },
            isLoading = false,
            hasNewImage = preservePendingImage && uiState.hasNewImage,
        )
        refreshUnsavedChangesFlag()
    }

    private fun commitSnapshotFromCurrentUi() {
        loadedSnapshot = LoadedProfileSnapshot(
            name = uiState.name,
            phone = uiState.phone,
            description = uiState.description,
            addressId = uiState.addressId,
            imageUrl = uiState.imageUrl?.takeUnless { it.startsWith("content://") },
        )
        uiState = uiState.copy(hasUnsavedChanges = false)
    }

    private fun refreshUnsavedChangesFlag() {
        val snapshot = loadedSnapshot
        val hasChanges = when {
            uiState.isFromSignUp -> true
            snapshot == null -> uiState.hasNewImage
            else -> uiState.hasNewImage ||
                uiState.name != snapshot.name ||
                uiState.phone != snapshot.phone ||
                uiState.description != snapshot.description ||
                uiState.addressId != snapshot.addressId
        }
        uiState = uiState.copy(hasUnsavedChanges = hasChanges)
    }

    private suspend fun resolveAddressIfMissing() {
        if (uiState.addressId != null) return
        addressRepository.getAddressesForRestaurant()
            .onSuccess { addressList ->
                addressList.maxByOrNull { it.id }?.let { latest ->
                    uiState = uiState.copy(
                        addressId = latest.id,
                        selectedAddressText = latest.detail.ifEmpty { latest.title },
                    )
                }
            }
    }

    fun onImageSelected(file: File, uri: Uri) {
        selectedImageFile = file
        uiState = uiState.copy(
            imageUrl = uri.toString(),
            hasNewImage = true,
            error = null,
        )
        refreshUnsavedChangesFlag()
    }

    fun fetchLatestAddress() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = addressRepository.getAddressesForRestaurant()

            result.onSuccess { addressList ->
                val latestAddress = addressList.maxByOrNull { it.id }

                if (latestAddress != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        addressId = latestAddress.id,
                        selectedAddressText = latestAddress.detail.ifEmpty { latestAddress.title },
                        error = null,
                    )
                    refreshUnsavedChangesFlag()
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "NOT FOUND"
                    )
                }
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = error.message ?: "SEVER ERROR"
                )
            }
        }
    }

    fun onEvent(event: RestaurantPersonalInfoEvent) {
        when (event) {
            is RestaurantPersonalInfoEvent.NameChanged -> {
                uiState = uiState.copy(name = event.name, error = null)
                refreshUnsavedChangesFlag()
            }
            is RestaurantPersonalInfoEvent.PhoneChanged -> {
                uiState = uiState.copy(phone = event.phone, error = null)
                refreshUnsavedChangesFlag()
            }
            is RestaurantPersonalInfoEvent.DescriptionChanged -> {
                uiState = uiState.copy(description = event.description, error = null)
                refreshUnsavedChangesFlag()
            }
            is RestaurantPersonalInfoEvent.AddressSelected -> {
                uiState = uiState.copy(
                    addressId = event.id,
                    selectedAddressText = event.detail,
                    error = null,
                )
                refreshUnsavedChangesFlag()
            }
            RestaurantPersonalInfoEvent.Submit -> updateProfile()
        }
    }

    private fun resolveImageFileForUpload(): File? {
        return selectedImageFile?.takeIf { it.exists() && it.length() > 0L }
    }

    private fun resolveImageUrlFromResponse(
        response: RestaurantResponse,
        fallbackUrl: String?,
    ): String? {
        return response.image?.takeIf { it.isNotBlank() }
            ?: response.coverImage?.takeIf { it.isNotBlank() }
            ?: fallbackUrl
    }

    private suspend fun fetchLatestProfileAfterSave(
        fallback: RestaurantResponse,
    ): RestaurantResponse {
        if (currentRestaurantId == -1) return fallback
        return repository.getRestaurantById(currentRestaurantId).getOrNull() ?: fallback
    }

    private fun updateProfile() {
        viewModelScope.launch {
            if (uiState.name.isBlank() || uiState.phone.isBlank()) {
                uiState = uiState.copy(error = "Name and Phone Number cannot be empty!")
                return@launch
            }

            if (!uiState.isFromSignUp && !uiState.hasUnsavedChanges) {
                uiState = uiState.copy(error = "No changes to save.")
                return@launch
            }

            val imageFile = if (uiState.hasNewImage) resolveImageFileForUpload() else null
            if (uiState.hasNewImage && imageFile == null) {
                uiState = uiState.copy(error = "Selected image is invalid. Please choose another photo.")
                return@launch
            }

            uiState = uiState.copy(isLoading = true, error = null)

            if (uiState.isFromSignUp) {
                if (uiState.addressId == null) {
                    uiState = uiState.copy(isLoading = false, error = "Please select a business address!")
                    return@launch
                }

                val result = repository.createRestaurant(
                    name = uiState.name,
                    phone = uiState.phone,
                    description = uiState.description,
                    addressId = uiState.addressId!!,
                    image = imageFile
                )

                result.onSuccess { response ->
                    selectedImageFile = null
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        hasNewImage = false,
                        hasUnsavedChanges = false,
                        imageUrl = resolveImageUrlFromResponse(response, uiState.imageUrl),
                    )
                }.onFailure { error ->
                    uiState = uiState.copy(isLoading = false, error = error.message ?: "Failed to create restaurant")
                }

            } else {
                if (currentRestaurantId == -1) {
                    uiState = uiState.copy(isLoading = false, error = "Restaurant ID is missing!")
                    return@launch
                }

                val result = repository.updateRestaurantProfile(
                    restaurantId = currentRestaurantId,
                    name = uiState.name,
                    phone = uiState.phone,
                    description = uiState.description,
                    addressId = uiState.addressId,
                    image = imageFile
                )

                result.onSuccess { response ->
                    selectedImageFile = null
                    val refreshedProfile = fetchLatestProfileAfterSave(response)
                    applyLoadedProfile(refreshedProfile, preservePendingImage = false)
                    uiState = uiState.copy(
                        isLoading = false,
                        isSuccess = true,
                        hasNewImage = false,
                        hasUnsavedChanges = false,
                    )
                }.onFailure { error ->
                    uiState = uiState.copy(isLoading = false, error = error.message ?: "Failed to update profile")
                }
            }
        }
    }

    fun resetSuccessState() {
        uiState = uiState.copy(isSuccess = false)
    }
}
