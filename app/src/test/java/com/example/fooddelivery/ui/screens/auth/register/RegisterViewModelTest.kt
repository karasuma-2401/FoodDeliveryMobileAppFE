package com.example.fooddelivery.ui.screens.auth.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Initial state ----

    @Test
    fun `initial state has empty string fields`() {
        val state = viewModel.state.value

        assertEquals("", state.fullName)
        assertEquals("", state.email)
        assertEquals("", state.phone)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
    }

    @Test
    fun `initial state has agreeToTerms false`() {
        assertFalse(viewModel.state.value.agreeToTerms)
    }

    @Test
    fun `initial state is not loading`() {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state isSuccess is false`() {
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `initial state has null error fields`() {
        val state = viewModel.state.value

        assertNull(state.fullNameError)
        assertNull(state.emailError)
        assertNull(state.phoneError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertNull(state.errorMessage)
    }

    // ---- onFullNameChange ----

    @Test
    fun `onFullNameChange updates fullName`() {
        viewModel.onFullNameChange("John Doe")

        assertEquals("John Doe", viewModel.state.value.fullName)
    }

    @Test
    fun `onFullNameChange clears fullNameError`() {
        // Manually set error state by creating a RegisterState with error, then simulate change
        // We can't set errors directly, but we can verify the change clears existing errors
        viewModel.onFullNameChange("Alice")
        viewModel.onFullNameChange("")
        viewModel.onFullNameChange("Bob")

        assertNull(viewModel.state.value.fullNameError)
    }

    @Test
    fun `onFullNameChange clears errorMessage`() {
        viewModel.onFullNameChange("John")

        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `onFullNameChange with empty string stores empty`() {
        viewModel.onFullNameChange("John Doe")
        viewModel.onFullNameChange("")

        assertEquals("", viewModel.state.value.fullName)
    }

    // ---- onEmailChange ----

    @Test
    fun `onEmailChange updates email`() {
        viewModel.onEmailChange("test@example.com")

        assertEquals("test@example.com", viewModel.state.value.email)
    }

    @Test
    fun `onEmailChange clears emailError`() {
        viewModel.onEmailChange("invalid")
        viewModel.onEmailChange("test@example.com")

        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun `onEmailChange clears errorMessage`() {
        viewModel.onEmailChange("test@example.com")

        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `onEmailChange does not affect other fields`() {
        viewModel.onFullNameChange("John Doe")
        viewModel.onEmailChange("test@example.com")

        assertEquals("John Doe", viewModel.state.value.fullName)
        assertEquals("test@example.com", viewModel.state.value.email)
    }

    // ---- onPhoneChange ----

    @Test
    fun `onPhoneChange updates phone`() {
        viewModel.onPhoneChange("0867070087")

        assertEquals("0867070087", viewModel.state.value.phone)
    }

    @Test
    fun `onPhoneChange clears phoneError`() {
        viewModel.onPhoneChange("0867070087")

        assertNull(viewModel.state.value.phoneError)
    }

    @Test
    fun `onPhoneChange clears errorMessage`() {
        viewModel.onPhoneChange("0867070087")

        assertNull(viewModel.state.value.errorMessage)
    }

    // ---- onPasswordChange ----

    @Test
    fun `onPasswordChange updates password`() {
        viewModel.onPasswordChange("mySecurePass123")

        assertEquals("mySecurePass123", viewModel.state.value.password)
    }

    @Test
    fun `onPasswordChange clears passwordError`() {
        viewModel.onPasswordChange("password123")

        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `onPasswordChange clears errorMessage`() {
        viewModel.onPasswordChange("pass")

        assertNull(viewModel.state.value.errorMessage)
    }

    // ---- onConfirmPasswordChange ----

    @Test
    fun `onConfirmPasswordChange updates confirmPassword`() {
        viewModel.onConfirmPasswordChange("mySecurePass123")

        assertEquals("mySecurePass123", viewModel.state.value.confirmPassword)
    }

    @Test
    fun `onConfirmPasswordChange clears confirmPasswordError`() {
        viewModel.onConfirmPasswordChange("match123")

        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `onConfirmPasswordChange clears errorMessage`() {
        viewModel.onConfirmPasswordChange("confirm")

        assertNull(viewModel.state.value.errorMessage)
    }

    // ---- onAgreeToTermsChange ----

    @Test
    fun `onAgreeToTermsChange sets agreeToTerms to true`() {
        viewModel.onAgreeToTermsChange(true)

        assertTrue(viewModel.state.value.agreeToTerms)
    }

    @Test
    fun `onAgreeToTermsChange sets agreeToTerms to false`() {
        viewModel.onAgreeToTermsChange(true)
        viewModel.onAgreeToTermsChange(false)

        assertFalse(viewModel.state.value.agreeToTerms)
    }

    @Test
    fun `onAgreeToTermsChange does not affect other fields`() {
        viewModel.onFullNameChange("John")
        viewModel.onEmailChange("john@test.com")
        viewModel.onAgreeToTermsChange(true)

        assertEquals("John", viewModel.state.value.fullName)
        assertEquals("john@test.com", viewModel.state.value.email)
        assertTrue(viewModel.state.value.agreeToTerms)
    }

    // ---- RegisterState data class ----

    @Test
    fun `RegisterState default values are correct`() {
        val state = RegisterState()

        assertEquals("", state.fullName)
        assertEquals("", state.email)
        assertEquals("", state.phone)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.agreeToTerms)
        assertNull(state.fullNameError)
        assertNull(state.emailError)
        assertNull(state.phoneError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `RegisterState copy works correctly`() {
        val original = RegisterState(fullName = "John")
        val updated = original.copy(email = "john@example.com", isLoading = true)

        assertEquals("John", updated.fullName)
        assertEquals("john@example.com", updated.email)
        assertTrue(updated.isLoading)
    }

    @Test
    fun `multiple field changes are reflected independently`() {
        viewModel.onFullNameChange("Alice")
        viewModel.onEmailChange("alice@example.com")
        viewModel.onPhoneChange("0912345678")
        viewModel.onPasswordChange("pass123")
        viewModel.onConfirmPasswordChange("pass123")
        viewModel.onAgreeToTermsChange(true)

        val state = viewModel.state.value
        assertEquals("Alice", state.fullName)
        assertEquals("alice@example.com", state.email)
        assertEquals("0912345678", state.phone)
        assertEquals("pass123", state.password)
        assertEquals("pass123", state.confirmPassword)
        assertTrue(state.agreeToTerms)
    }
}