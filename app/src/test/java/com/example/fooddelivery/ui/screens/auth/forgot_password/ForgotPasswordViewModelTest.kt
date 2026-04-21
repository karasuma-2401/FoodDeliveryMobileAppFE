package com.example.fooddelivery.ui.screens.auth.forgot_password

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
class ForgotPasswordViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ForgotPasswordViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Initial state ----

    @Test
    fun `initial state has empty email`() {
        assertEquals("", viewModel.state.value.email)
    }

    @Test
    fun `initial state isLoading is false`() {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state isSuccess is false`() {
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `initial state errorMessage is null`() {
        assertNull(viewModel.state.value.errorMessage)
    }

    // ---- onEmailChange ----

    @Test
    fun `onEmailChange updates email in state`() {
        viewModel.onEmailChange("user@example.com")

        assertEquals("user@example.com", viewModel.state.value.email)
    }

    @Test
    fun `onEmailChange clears emailError`() {
        // Trigger error by sending blank email
        viewModel.sendResetCode()
        assertEquals("Please enter a valid email address", viewModel.state.value.emailError)

        // Now change email - error should be cleared
        viewModel.onEmailChange("valid@test.com")

        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun `onEmailChange with empty string stores empty email`() {
        viewModel.onEmailChange("some@email.com")
        viewModel.onEmailChange("")

        assertEquals("", viewModel.state.value.email)
    }

    @Test
    fun `onEmailChange does not affect isLoading`() {
        viewModel.onEmailChange("test@test.com")

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `onEmailChange does not affect isSuccess`() {
        viewModel.onEmailChange("test@test.com")

        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `onEmailChange multiple times keeps last value`() {
        viewModel.onEmailChange("first@email.com")
        viewModel.onEmailChange("second@email.com")
        viewModel.onEmailChange("third@email.com")

        assertEquals("third@email.com", viewModel.state.value.email)
    }

    // ---- sendResetCode() validation (blank email - safe on JVM) ----

    @Test
    fun `sendResetCode with blank email sets emailError`() {
        viewModel.sendResetCode()

        assertEquals("Please enter a valid email address", viewModel.state.value.emailError)
    }

    @Test
    fun `sendResetCode with blank email does not set isLoading`() {
        viewModel.sendResetCode()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `sendResetCode with blank email does not set isSuccess`() {
        viewModel.sendResetCode()

        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `sendResetCode with whitespace-only email sets emailError`() {
        viewModel.onEmailChange("   ")
        viewModel.sendResetCode()

        assertEquals("Please enter a valid email address", viewModel.state.value.emailError)
    }

    @Test
    fun `sendResetCode with whitespace-only email does not set isLoading`() {
        viewModel.onEmailChange("   ")
        viewModel.sendResetCode()

        assertFalse(viewModel.state.value.isLoading)
    }

    // ---- ForgotPasswordState data class ----

    @Test
    fun `ForgotPasswordState default values are correct`() {
        val state = ForgotPasswordState()

        assertEquals("", state.email)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun `ForgotPasswordState copy works correctly`() {
        val original = ForgotPasswordState(email = "user@example.com")
        val updated = original.copy(isLoading = true, errorMessage = "Error occurred")

        assertEquals("user@example.com", updated.email)
        assertTrue(updated.isLoading)
        assertEquals("Error occurred", updated.errorMessage)
        assertFalse(updated.isSuccess)
    }

    @Test
    fun `ForgotPasswordState equality with same values`() {
        val state1 = ForgotPasswordState(email = "test@test.com", isSuccess = true)
        val state2 = ForgotPasswordState(email = "test@test.com", isSuccess = true)

        assertEquals(state1, state2)
    }

    // ---- After sendResetCode with blank - subsequent onEmailChange resets errors ----

    @Test
    fun `after blank email error, changing email clears emailError and keeps new email`() {
        viewModel.sendResetCode() // sets emailError
        viewModel.onEmailChange("newuser@domain.com")

        assertEquals("newuser@domain.com", viewModel.state.value.email)
        assertNull(viewModel.state.value.emailError)
    }
}