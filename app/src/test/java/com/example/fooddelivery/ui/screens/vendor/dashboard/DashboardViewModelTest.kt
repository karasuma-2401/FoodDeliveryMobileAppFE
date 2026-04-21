package com.example.fooddelivery.ui.screens.vendor.dashboard

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- Initial state (before coroutine runs) ----

    @Test
    fun `immediately after creation state has isLoading true`() {
        val viewModel = DashboardViewModel()

        // loadDashboard() is called in init and immediately sets isLoading = true
        // The coroutine has been launched but delay hasn't completed yet
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `immediately after creation runningOrders defaults to 0`() {
        val viewModel = DashboardViewModel()

        assertEquals(0, viewModel.state.value.runningOrders)
    }

    @Test
    fun `immediately after creation orderRequest defaults to 0`() {
        val viewModel = DashboardViewModel()

        assertEquals(0, viewModel.state.value.orderRequest)
    }

    @Test
    fun `immediately after creation revenue defaults to 0 0`() {
        val viewModel = DashboardViewModel()

        assertEquals(0.0, viewModel.state.value.revenue, 0.001)
    }

    @Test
    fun `immediately after creation rating defaults to 0 0`() {
        val viewModel = DashboardViewModel()

        assertEquals(0.0, viewModel.state.value.rating, 0.001)
    }

    @Test
    fun `immediately after creation totalReviews defaults to 0`() {
        val viewModel = DashboardViewModel()

        assertEquals(0, viewModel.state.value.totalReviews)
    }

    // ---- State after loadDashboard() coroutine completes ----

    @Test
    fun `after loading completes runningOrders is 20`() = runTest {
        val viewModel = DashboardViewModel()
        advanceUntilIdle()

        assertEquals(20, viewModel.state.value.runningOrders)
    }

    @Test
    fun `after loading completes orderRequest is 5`() = runTest {
        val viewModel = DashboardViewModel()
        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.orderRequest)
    }

    @Test
    fun `after loading completes revenue is 2241 0`() = runTest {
        val viewModel = DashboardViewModel()
        advanceUntilIdle()

        assertEquals(2241.0, viewModel.state.value.revenue, 0.001)
    }

    @Test
    fun `after loading completes rating is 4 9`() = runTest {
        val viewModel = DashboardViewModel()
        advanceUntilIdle()

        assertEquals(4.9, viewModel.state.value.rating, 0.001)
    }

    @Test
    fun `after loading completes totalReviews is 20`() = runTest {
        val viewModel = DashboardViewModel()
        advanceUntilIdle()

        assertEquals(20, viewModel.state.value.totalReviews)
    }

    @Test
    fun `after loading completes isLoading is false`() = runTest {
        val viewModel = DashboardViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }

    // ---- DashboardState data class ----

    @Test
    fun `DashboardState default values are all zero`() {
        val state = DashboardState()

        assertFalse(state.isLoading)
        assertEquals(0, state.runningOrders)
        assertEquals(0, state.orderRequest)
        assertEquals(0.0, state.revenue, 0.001)
        assertEquals(0.0, state.rating, 0.001)
        assertEquals(0, state.totalReviews)
    }

    @Test
    fun `DashboardState copy creates new instance with overridden fields`() {
        val original = DashboardState()
        val updated = original.copy(runningOrders = 10, revenue = 500.0, isLoading = true)

        assertEquals(10, updated.runningOrders)
        assertEquals(500.0, updated.revenue, 0.001)
        assertTrue(updated.isLoading)
        // Other fields unchanged
        assertEquals(0, updated.orderRequest)
        assertEquals(0.0, updated.rating, 0.001)
        assertEquals(0, updated.totalReviews)
    }

    @Test
    fun `DashboardState equality with same values`() {
        val state1 = DashboardState(runningOrders = 5, revenue = 100.0)
        val state2 = DashboardState(runningOrders = 5, revenue = 100.0)

        assertEquals(state1, state2)
    }

    @Test
    fun `DashboardState with all fields populated`() {
        val state = DashboardState(
            isLoading = false,
            runningOrders = 20,
            orderRequest = 5,
            revenue = 2241.0,
            rating = 4.9,
            totalReviews = 20
        )

        assertFalse(state.isLoading)
        assertEquals(20, state.runningOrders)
        assertEquals(5, state.orderRequest)
        assertEquals(2241.0, state.revenue, 0.001)
        assertEquals(4.9, state.rating, 0.001)
        assertEquals(20, state.totalReviews)
    }
}