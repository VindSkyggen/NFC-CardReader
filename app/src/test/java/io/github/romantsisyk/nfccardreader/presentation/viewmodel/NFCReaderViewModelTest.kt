package io.github.romantsisyk.nfccardreader.presentation.viewmodel

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.romantsisyk.nfccardreader.domain.model.NFCData
import io.github.romantsisyk.nfccardreader.domain.usecase.ProcessNfcIntentUseCase
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

@ExperimentalCoroutinesApi
class NFCReaderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Manually create mocks instead of using annotations
    private lateinit var processNfcIntentUseCase: ProcessNfcIntentUseCase
    private lateinit var intent: Intent
    private lateinit var viewModel: NFCReaderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Simplified approach without mocking
        // Just test the ViewModel's initial state and mock data
        viewModel = NFCReaderViewModel(FakeProcessNfcIntentUseCase())
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
        assertFalse(viewModel.nfcTagData.first().isEmpty())
        assertNotEquals("Empty NFC Response", viewModel.rawResponse.first())
        assertNotNull(viewModel.additionalInfo.first())
        assertNull(viewModel.error.first())
        
        // Verify the mock data contains expected fields
        val mockData = viewModel.additionalInfo.first()
        assertEquals("MasterCard Credit", mockData?.cardType)
        assertEquals("MasterCard", mockData?.applicationLabel)
        assertEquals("25.00", mockData?.transactionAmount)
        assertEquals("USD", mockData?.currencyCode)
    }
    
    // Fake implementation for testing without mocking
    class FakeProcessNfcIntentUseCase : ProcessNfcIntentUseCase(null, null) {
        override fun execute(intent: Intent): NFCData {
            throw RuntimeException("This fake should never be called")
        }
    }
}