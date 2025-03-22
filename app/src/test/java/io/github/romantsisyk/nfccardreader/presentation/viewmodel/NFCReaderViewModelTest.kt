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
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class NFCReaderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var processNfcIntentUseCase: ProcessNfcIntentUseCase

    @Mock
    private lateinit var intent: Intent

    private lateinit var viewModel: NFCReaderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NFCReaderViewModel(processNfcIntentUseCase)
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
    fun `test processNfcIntent success`() = runTest {
        // Given
        val mockTlvData = mapOf(
            "Cardholder Name" to "JOHN DOE",
            "Application PAN" to "XXXXXXXXXXXX1234"
        )
        
        val mockNfcData = NFCData(
            rawResponse = "90 00 5F 20",
            cardType = "Test Card",
            applicationLabel = "VISA",
            parsedTlvData = mockTlvData
        )
        
        Mockito.`when`(processNfcIntentUseCase.execute(intent)).thenReturn(mockNfcData)

        // When
        viewModel.processNfcIntent(intent)

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        Mockito.verify(processNfcIntentUseCase).execute(intent)
        assertEquals(mockTlvData, viewModel.nfcTagData.first())
        assertEquals("90 00 5F 20", viewModel.rawResponse.first())
        assertEquals("Test Card", viewModel.additionalInfo.first()?.cardType)
        assertEquals("VISA", viewModel.additionalInfo.first()?.applicationLabel)
        assertNull(viewModel.error.first())
    }

    @Test
    fun `test processNfcIntent error`() = runTest {
        // Given
        val errorMessage = "Failed to read NFC data"
        Mockito.`when`(processNfcIntentUseCase.execute(intent)).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.processNfcIntent(intent)

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        Mockito.verify(processNfcIntentUseCase).execute(intent)
        assertEquals(errorMessage, viewModel.error.first())
    }

    @Test
    fun `test clearNfcData`() = runTest {
        // Given
        val mockTlvData = mapOf(
            "Cardholder Name" to "JOHN DOE",
            "Application PAN" to "XXXXXXXXXXXX1234"
        )
        
        val mockNfcData = NFCData(
            rawResponse = "90 00 5F 20",
            cardType = "Test Card",
            applicationLabel = "VISA",
            parsedTlvData = mockTlvData
        )
        
        Mockito.`when`(processNfcIntentUseCase.execute(intent)).thenReturn(mockNfcData)
        viewModel.processNfcIntent(intent)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearNfcData()

        // Then
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
}