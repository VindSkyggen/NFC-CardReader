package io.github.romantsisyk.nfccardreader.domain.usecase

import io.github.romantsisyk.nfccardreader.util.createByteArrayFromHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ParseTLVUseCaseTest {

    private lateinit var parseTLVUseCase: ParseTLVUseCase

    @Before
    fun setup() {
        parseTLVUseCase = ParseTLVUseCase()
    }

    @Test
    fun `test execute with empty data`() {
        // Given
        val emptyData = ByteArray(0)

        // When
        val result = parseTLVUseCase.execute(emptyData)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test execute with multiple tags`() {
        // Given
        val data = createByteArrayFromHex(
            "5A 08 41 11 11 11 11 11 11 11 " +  // PAN
            "5F 24 03 25 02 28"                 // Expiration date
        )

        // When
        val result = parseTLVUseCase.execute(data)

        // Then
        assertTrue(result.size >= 1)
    }
}