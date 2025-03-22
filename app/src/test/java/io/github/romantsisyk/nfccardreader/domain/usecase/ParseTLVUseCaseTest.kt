package io.github.romantsisyk.nfccardreader.domain.usecase

import android.util.Log
import io.github.romantsisyk.nfccardreader.domain.EmvTag
import io.github.romantsisyk.nfccardreader.util.createByteArrayFromHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
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
}