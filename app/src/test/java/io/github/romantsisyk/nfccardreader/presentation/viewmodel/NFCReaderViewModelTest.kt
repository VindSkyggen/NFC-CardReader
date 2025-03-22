package io.github.romantsisyk.nfccardreader.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.romantsisyk.nfccardreader.domain.model.NFCData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Simple test for NFCReaderViewModel that doesn't use any mocking.
 * Tests only the state management of the ViewModel.
 */
@ExperimentalCoroutinesApi
class NFCReaderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: NFCReaderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Create a view model without dependency injection
        // We don't need a real ProcessNfcIntentUseCase as we're only testing state
        viewModel = NFCReaderViewModel(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state`() = runTest {
        // Initial state should have empty data
        assertTrue(viewModel.nfcTagData.first().isEmpty())
        assertEquals("Empty NFC Response", viewModel.rawResponse.first())
        assertNull(viewModel.additionalInfo.first())
        assertNull(viewModel.error.first())
    }

    @Test
    fun `test clearNfcData`() = runTest {
        // First load some mock data
        viewModel.processMockNfcIntent()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then clear it
        viewModel.clearNfcData()

        // Check if data was cleared
        assertTrue(viewModel.nfcTagData.first().isEmpty())
        assertEquals("Empty NFC Response", viewModel.rawResponse.first())
        assertNull(viewModel.additionalInfo.first())
        assertNull(viewModel.error.first())
    }

    @Test
    fun `test processMockNfcIntent`() = runTest {
        // When
        viewModel.processMockNfcIntent()

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify the mock data contains expected fields
        assertFalse(viewModel.nfcTagData.first().isEmpty())
        assertNotEquals("Empty NFC Response", viewModel.rawResponse.first())
        
        val mockData = viewModel.additionalInfo.first()
        assertNotNull(mockData)
        assertEquals("MasterCard Credit", mockData?.cardType)
        assertEquals("MasterCard", mockData?.applicationLabel)
        assertEquals("25.00", mockData?.transactionAmount)
        assertEquals("USD", mockData?.currencyCode)
    }
}