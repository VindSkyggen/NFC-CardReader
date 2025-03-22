package io.github.romantsisyk.nfccardreader.domain.usecase

import io.github.romantsisyk.nfccardreader.domain.EmvTag
import io.github.romantsisyk.nfccardreader.util.createByteArrayFromHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class InterpretNfcDataUseCaseTest {

    private lateinit var interpretNfcDataUseCase: InterpretNfcDataUseCase

    @Before
    fun setup() {
        interpretNfcDataUseCase = InterpretNfcDataUseCase()
    }

    @Test
    fun `test execute with empty response`() {
        // Given
        val emptyResponse = ByteArray(0)

        // When
        val result = interpretNfcDataUseCase.execute(emptyResponse)

        // Then
        assertEquals("", result.rawResponse)
    }

    @Test
    fun `test execute with card type tag`() {
        // Given
        val response = createByteArrayFromHex("6F 04 A0 00 00 03")

        // When
        val result = interpretNfcDataUseCase.execute(response)

        // Then
        assertEquals("6F 04 A0 00 00 03", result.rawResponse)
        assertEquals("EMV Payment Card", result.cardType)
    }

    @Test
    fun `test execute with application label tag`() {
        // Given
        val response = createByteArrayFromHex("50 04 56 49 53 41") // "VISA" in ASCII

        // When
        val result = interpretNfcDataUseCase.execute(response)

        // Then
        assertEquals("50 04 56 49 53 41", result.rawResponse)
        assertEquals("VISA", result.applicationLabel)
    }

    @Test
    fun `test execute with multiple tags`() {
        // Given
        val response = createByteArrayFromHex("6F 04 A0 00 00 03 50 04 56 49 53 41")

        // When
        val result = interpretNfcDataUseCase.execute(response)

        // Then
        assertEquals("6F 04 A0 00 00 03 50 04 56 49 53 41", result.rawResponse)
        assertEquals("EMV Payment Card", result.cardType)
        assertEquals("VISA", result.applicationLabel)
    }
}