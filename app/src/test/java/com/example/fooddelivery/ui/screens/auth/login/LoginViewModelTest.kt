package com.example.fooddelivery.ui.screens.auth.login

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Initial state ----

    @Test
    fun `initial state has empty phone and password`() {
        val state = viewModel.state.value

        assertEquals("", state.phone)
        assertEquals("", state.password)
    }

    @Test
    fun `initial state has rememberMe false`() {
        assertFalse(viewModel.state.value.rememberMe)
    }

    @Test
    fun `initial state is not loading`() {
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `initial state isSuccess is false`() {
        assertFalse(viewModel.state.value.isSuccess)
    }

    // ---- onPhoneChange ----

    @Test
    fun `onPhoneChange updates phone in state`() {
        viewModel.onPhoneChange("0867070087")

        assertEquals("0867070087", viewModel.state.value.phone)
    }

    @Test
    fun `onPhoneChange clears phoneError`() {
        // First trigger an error by calling login with blank phone
        viewModel.login()
        assertNotNull(viewModel.state.value.phoneError)

        // Now change phone - error should be cleared
        viewModel.onPhoneChange("0867070087")

        assertNull(viewModel.state.value.phoneError)
    }

    @Test
    fun `onPhoneChange clears errorMessage`() {
        viewModel.onPhoneChange("0867070087")

        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `onPhoneChange with empty string sets empty phone`() {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPhoneChange("")

        assertEquals("", viewModel.state.value.phone)
    }

    // ---- onPasswordChange ----

    @Test
    fun `onPasswordChange updates password in state`() {
        viewModel.onPasswordChange("secret123")

        assertEquals("secret123", viewModel.state.value.password)
    }

    @Test
    fun `onPasswordChange clears passwordError`() {
        // Trigger password error
        viewModel.onPhoneChange("0867070087")
        viewModel.login()
        assertNotNull(viewModel.state.value.passwordError)

        // Change password - error should be cleared
        viewModel.onPasswordChange("mypassword")

        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `onPasswordChange clears errorMessage`() {
        viewModel.onPasswordChange("newPass")

        assertNull(viewModel.state.value.errorMessage)
    }

    // ---- onRememberMeChange ----

    @Test
    fun `onRememberMeChange sets rememberMe to true`() {
        viewModel.onRememberMeChange(true)

        assertTrue(viewModel.state.value.rememberMe)
    }

    @Test
    fun `onRememberMeChange sets rememberMe to false`() {
        viewModel.onRememberMeChange(true)
        viewModel.onRememberMeChange(false)

        assertFalse(viewModel.state.value.rememberMe)
    }

    @Test
    fun `onRememberMeChange does not affect other state fields`() {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPasswordChange("somepass")
        viewModel.onRememberMeChange(true)

        assertEquals("0867070087", viewModel.state.value.phone)
        assertEquals("somepass", viewModel.state.value.password)
    }

    // ---- login() validation ----

    @Test
    fun `login with blank phone sets phoneError`() {
        viewModel.onPasswordChange("somepassword")
        viewModel.login()

        assertEquals("Phone number cannot be empty", viewModel.state.value.phoneError)
    }

    @Test
    fun `login with blank password sets passwordError`() {
        viewModel.onPhoneChange("0867070087")
        viewModel.login()

        assertEquals("Password cannot be empty", viewModel.state.value.passwordError)
    }

    @Test
    fun `login with both blank sets both errors`() {
        viewModel.login()

        assertEquals("Phone number cannot be empty", viewModel.state.value.phoneError)
        assertEquals("Password cannot be empty", viewModel.state.value.passwordError)
    }

    @Test
    fun `login with blank inputs does not set isLoading`() {
        viewModel.login()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with blank inputs does not change isSuccess`() {
        viewModel.login()

        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `login with whitespace-only phone sets phoneError`() {
        viewModel.onPhoneChange("   ")
        viewModel.onPasswordChange("validpass")
        viewModel.login()

        assertEquals("Phone number cannot be empty", viewModel.state.value.phoneError)
    }

    @Test
    fun `login with whitespace-only password sets passwordError`() {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPasswordChange("   ")
        viewModel.login()

        assertEquals("Password cannot be empty", viewModel.state.value.passwordError)
    }

    // ---- login() with valid inputs (coroutine path) ----

    @Test
    fun `login with valid inputs sets isLoading true during execution`() = runTest {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPasswordChange("123")

        viewModel.login()

        // After calling login() but before coroutine completes, loading should be true
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with correct credentials sets isSuccess to true after completion`() = runTest {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPasswordChange("123")

        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with wrong credentials sets errorMessage`() = runTest {
        viewModel.onPhoneChange("0999999999")
        viewModel.onPasswordChange("wrongpass")

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSuccess)
        assertEquals("Invalid phone number or password", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with correct phone but wrong password sets errorMessage`() = runTest {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPasswordChange("wrongpassword")

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSuccess)
        assertEquals("Invalid phone number or password", viewModel.state.value.errorMessage)
    }

    @Test
    fun `login with wrong phone but correct password sets errorMessage`() = runTest {
        viewModel.onPhoneChange("0999999999")
        viewModel.onPasswordChange("123")

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSuccess)
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `login clears phoneError and passwordError when starting coroutine`() = runTest {
        viewModel.onPhoneChange("0867070087")
        viewModel.onPasswordChange("123")

        viewModel.login()

        // After launching coroutine, errors should be cleared
        assertNull(viewModel.state.value.phoneError)
        assertNull(viewModel.state.value.passwordError)
    }
}