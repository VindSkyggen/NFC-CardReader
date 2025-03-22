package io.github.romantsisyk.nfccardreader.domain.usecase

import android.util.Log
import io.github.romantsisyk.nfccardreader.domain.EmvTag
import io.github.romantsisyk.nfccardreader.util.createByteArrayFromHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InterpretNfcDataUseCaseTest {

    private lateinit var interpretNfcDataUseCase: InterpretNfcDataUseCase

    @Before
    fun setup() {
        interpretNfcDataUseCase = InterpretNfcDataUseCase()
        // We can't easily mock Log due to mockito version limitations in tests
        // Just proceed without mocking it
    }

    @Test
    fun `test execute with empty response`() {
        // Given
        val emptyResponse = ByteArray(0)

        // When
        val result = interpretNfcDataUseCase.execute(emptyResponse)

        // Then
        assertEquals("", result.rawResponse)
        assertEquals(emptyMap<EmvTag, String>(), result.parsedTlvData)
    }

}