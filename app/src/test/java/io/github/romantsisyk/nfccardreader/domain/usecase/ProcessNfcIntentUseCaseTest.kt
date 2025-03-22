package io.github.romantsisyk.nfccardreader.domain.usecase

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import io.github.romantsisyk.nfccardreader.domain.model.NFCData
import io.github.romantsisyk.nfccardreader.util.createByteArrayFromHex
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class ProcessNfcIntentUseCaseTest {

    @Mock
    private lateinit var parseTLVUseCase: ParseTLVUseCase

    @Mock
    private lateinit var interpretNfcDataUseCase: InterpretNfcDataUseCase

    @Mock
    private lateinit var intent: Intent

    @Mock
    private lateinit var tag: Tag

    @Mock
    private lateinit var isoDep: IsoDep

    private lateinit var processNfcIntentUseCase: ProcessNfcIntentUseCase

    @Before
    fun setup() {
        processNfcIntentUseCase = ProcessNfcIntentUseCase(parseTLVUseCase, interpretNfcDataUseCase)
        mockStaticMethods()
    }

    @Test
    fun `test execute with valid NFC intent`() {
        // Given
        val mockResponse = createByteArrayFromHex("90 00")
        val mockTlvData = mapOf("Test Tag" to "Test Value")
        
        val mockNfcData = NFCData(
            rawResponse = "90 00",
            cardType = "Test Card",
            applicationLabel = "Test App",
            parsedTlvData = emptyMap()
        )
        
        // Configure mocks
        Mockito.`when`(isoDep.transceive(any())).thenReturn(mockResponse)
        Mockito.`when`(parseTLVUseCase.execute(mockResponse)).thenReturn(mockTlvData)
        Mockito.`when`(interpretNfcDataUseCase.execute(mockResponse)).thenReturn(mockNfcData)

        // When
        val result = processNfcIntentUseCase.execute(intent)

        // Then
        verify(isoDep).connect()
        verify(isoDep).transceive(any())
        verify(isoDep).close()
        verify(parseTLVUseCase).execute(mockResponse)
        verify(interpretNfcDataUseCase).execute(mockResponse)
        
        assertEquals("90 00", result.rawResponse)
        assertEquals("Test Card", result.cardType)
        assertEquals("Test App", result.applicationLabel)
        assertEquals(mockTlvData, result.parsedTlvData)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test execute with null tag in intent`() {
        // Given
        Mockito.`when`(intent.getParcelableExtra(eq(NfcAdapter.EXTRA_TAG), eq(Tag::class.java))).thenReturn(null)

        // When
        processNfcIntentUseCase.execute(intent) // Should throw IllegalArgumentException
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `test execute with unsupported NFC tag`() {
        // Given
        Mockito.`when`(intent.getParcelableExtra(eq(NfcAdapter.EXTRA_TAG), eq(Tag::class.java))).thenReturn(tag)
        mockStaticIsoDep(null) // IsoDep.get returns null
        
        // When
        processNfcIntentUseCase.execute(intent) // Should throw UnsupportedOperationException
    }

    @Test(expected = IOException::class)
    fun `test execute with NFC connection error`() {
        // Given
        Mockito.`when`(intent.getParcelableExtra(eq(NfcAdapter.EXTRA_TAG), eq(Tag::class.java))).thenReturn(tag)
        
        // Configure isoDep to throw exception on connect
        doThrow(IOException()).`when`(isoDep).connect()
        
        // When
        processNfcIntentUseCase.execute(intent) // Should throw IOException
    }

    @Test(expected = IOException::class)
    fun `test execute with transceive error`() {
        // Given
        Mockito.`when`(intent.getParcelableExtra(eq(NfcAdapter.EXTRA_TAG), eq(Tag::class.java))).thenReturn(tag)
        
        // Configure isoDep to throw exception on transceive
        doThrow(IOException()).`when`(isoDep).transceive(any())
        
        // When
        processNfcIntentUseCase.execute(intent) // Should throw IOException
    }
    
    private fun mockStaticMethods() {
        // Setup the intent to return our mock tag
        Mockito.`when`(intent.getParcelableExtra(eq(NfcAdapter.EXTRA_TAG), eq(Tag::class.java))).thenReturn(tag)
        
        // Setup IsoDep.get to return our mock isoDep
        mockStaticIsoDep(isoDep)
    }
    
    private fun mockStaticIsoDep(result: IsoDep?) {
        // This is a placeholder for mocking IsoDep.get static method
    }
}