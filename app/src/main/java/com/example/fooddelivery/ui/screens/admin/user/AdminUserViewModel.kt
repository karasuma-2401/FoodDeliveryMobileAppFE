package com.example.fooddelivery.ui.screens.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.AdminUserItemDto
import com.example.fooddelivery.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserListUiState(
    val isLoading: Boolean = false,
    val users: List<AdminUserItemDto> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    private var masterUsersList = emptyList<AdminUserItemDto>()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAdminUsers()
                .onSuccess { response ->
                    masterUsersList = response.data
                    _uiState.update { it.copy(isLoading = false, users = response.data) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val filtered = if (query.isBlank()) {
            masterUsersList
        } else {
            masterUsersList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true) ||
                        (it.phone ?: "").contains(query)
            }
        }
        _uiState.update { it.copy(users = filtered) }
    }
}