package io.github.romantsisyk.nfccardreader.utils

import io.github.romantsisyk.nfccardreader.util.createHexList
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NfcDataMaskerTest {

    @Test
    fun `test maskPan with standard length PAN`() {
        // Given
        val pan = "4111111111111111"
        
        // When
        val result = NfcDataMasker.maskPan(pan)
        
        // Then
        // Should mask all but the last 4 digits
        assertEquals("XXXXXXXXXXXX1111", result)
    }

    @Test
    fun `test maskPan with short PAN`() {
        // Given
        val pan = "411111"
        
        // When
        val result = NfcDataMasker.maskPan(pan)
        
        // Then
        // Should mask all but the last 4 digits (if any)
        assertEquals("XX1111", result)
    }

    @Test
    fun `test maskPan with PAN less than 4 digits`() {
        // Given
        val pan = "123"
        
        // When
        val result = NfcDataMasker.maskPan(pan)
        
        // Then
        // Should return PAN as is, as there are less than 4 digits
        assertEquals("123", result)
    }

    @Test
    fun `test maskPan with empty string`() {
        // Given
        val pan = ""
        
        // When
        val result = NfcDataMasker.maskPan(pan)
        
        // Then
        // Should return empty string
        assertEquals("", result)
    }

    @Test
    fun `test maskTrack2Data with standard track2 data`() {
        // Given
        val track2 = "4111111111111111=25022010000000000000"
        
        // When
        val result = NfcDataMasker.maskTrack2Data(track2)
        
        // Then
        // Should mask PAN and replace equal sign
        assertTrue(result.contains("X"))
        assertTrue(result.contains("X"))
        assertEquals('X', result[result.indexOf('=')])
    }

    @Test
    fun `test maskTrack2Data with HEX track2 data`() {
        // Given
        val track2 = "4111111111111111D25022010000000000000"
        
        // When
        val result = NfcDataMasker.maskTrack2Data(track2)
        
        // Then
        // Should mask PAN and leave the rest unchanged
        assertTrue(result.contains("X"))
        assertTrue(result.contains("D"))
        assertEquals('D', result[result.indexOf('D')])
    }

    @Test
    fun `test maskTrack2Data with empty string`() {
        // Given
        val track2 = ""
        
        // When
        val result = NfcDataMasker.maskTrack2Data(track2)
        
        // Then
        // Should return empty string
        assertEquals("", result)
    }
}