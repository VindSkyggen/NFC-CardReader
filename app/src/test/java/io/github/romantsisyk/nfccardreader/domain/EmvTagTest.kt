package io.github.romantsisyk.nfccardreader.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EmvTagTest {

    @Test
    fun `test fromTag with known tag`() {
        // Given
        val knownTag = "5A" // APPLICATION_PAN

        // When
        val result = EmvTag.fromTag(knownTag)

        // Then
        assertEquals(EmvTag.APPLICATION_PAN, result)
    }

    @Test
    fun `test fromTag with 2-byte known tag`() {
        // Given
        val knownTag = "5F20" // CARDHOLDER_NAME

        // When
        val result = EmvTag.fromTag(knownTag)

        // Then
        assertEquals(EmvTag.CARDHOLDER_NAME, result)
    }

    @Test
    fun `test fromTag with unknown tag`() {
        // Given
        val unknownTag = "9999" // Not defined in EmvTag

        // When
        val result = EmvTag.fromTag(unknownTag)

        // Then
        assertEquals(EmvTag.UNKNOWN, result)
    }

    @Test
    fun `test fromTag with empty string`() {
        // Given
        val emptyTag = ""

        // When
        val result = EmvTag.fromTag(emptyTag)

        // Then
        assertEquals(EmvTag.UNKNOWN, result)
    }

    @Test
    fun `test getDescription with known tag`() {
        // Given
        val knownTag = "5A" // APPLICATION_PAN

        // When
        val result = EmvTag.getDescription(knownTag)

        // Then
        assertEquals("Primary Account Number", result)
    }

    @Test
    fun `test getDescription with unknown tag`() {
        // Given
        val unknownTag = "9999" // Not defined in EmvTag

        // When
        val result = EmvTag.getDescription(unknownTag)

        // Then
        assertEquals("Unknown Tag", result)
    }

    @Test
    fun `test all tags have non-empty descriptions`() {
        // Given
        val allTags = EmvTag.entries

        // When/Then
        for (tag in allTags) {
            assertNotNull("Tag ${tag.name} should have a description", tag.description)
            assert(tag.description.isNotEmpty()) { "Tag ${tag.name} has an empty description" }
        }
    }

    @Test
    fun `test all tags have valid tag values`() {
        // Given
        val allTags = EmvTag.entries.filter { it != EmvTag.UNKNOWN }

        // When/Then
        for (tag in allTags) {
            // Tag values should be valid hexadecimal strings of 2 or 4 characters
            assert(tag.tag.matches(Regex("^[0-9A-Fa-f]{2}([0-9A-Fa-f]{2})?$"))) { 
                "Tag ${tag.name} has an invalid tag value: ${tag.tag}" 
            }
        }
    }

    @Test
    fun `test no duplicate tag values`() {
        // Given
        val allTags = EmvTag.entries.filter { it != EmvTag.UNKNOWN }
        val tagValues = mutableSetOf<String>()
        val duplicates = mutableListOf<String>()

        // When
        for (tag in allTags) {
            if (!tagValues.add(tag.tag)) {
                duplicates.add(tag.tag)
            }
        }

        // Then
        assert(duplicates.isEmpty()) { "Found duplicate tag values: $duplicates" }
    }

    @Test
    fun `test common tags are defined`() {
        // Given
        val commonTagValues = listOf(
            "5A",    // PAN
            "5F20",  // Cardholder Name
            "5F24",  // Expiration Date
            "9F02",  // Amount, Authorized
            "9F03",  // Amount, Other
            "9F06",  // Application Identifier (AID)
            "9F26",  // Application Cryptogram
            "95",    // Terminal Verification Results
            "9F34",  // Cardholder Verification Method Results
            "82",    // Application Interchange Profile
            "9F36",  // Application Transaction Counter
            "9F37",  // Unpredictable Number
            "9F10",  // Issuer Application Data
            "9F1A"   // Terminal Country Code
        )

        // When/Then
        for (tagValue in commonTagValues) {
            val emvTag = EmvTag.fromTag(tagValue)
            assert(emvTag != EmvTag.UNKNOWN) { "Common tag $tagValue is not defined in EmvTag enum" }
        }
    }

    @Test
    fun `test cardholder name tag is well-defined`() {
        // Given/When
        val cardholderNameTag = EmvTag.CARDHOLDER_NAME

        // Then
        assertEquals("5F20", cardholderNameTag.tag)
        assertEquals("Cardholder Name", cardholderNameTag.description)
    }

    @Test
    fun `test application PAN tag is well-defined`() {
        // Given/When
        val panTag = EmvTag.APPLICATION_PAN

        // Then
        assertEquals("5A", panTag.tag)
        assertEquals("Primary Account Number", panTag.description)
    }

    @Test
    fun `test expiration date tag is well-defined`() {
        // Given/When
        val expirationDateTag = EmvTag.EXPIRATION_DATE

        // Then
        assertEquals("5F24", expirationDateTag.tag)
        assertEquals("Expiration Date", expirationDateTag.description)
    }
    
    @Test
    fun `test amount other tag is well-defined`() {
        // Given/When
        val amountOtherTag = EmvTag.AMOUNT_OTHER

        // Then
        assertEquals("9F03", amountOtherTag.tag)
        assertEquals("Amount, Other", amountOtherTag.description)
    }
    
    @Test
    fun `test application identifier additional tag is well-defined`() {
        // Given/When
        val aidTag = EmvTag.APPLICATION_IDENTIFIER_ADDITIONAL

        // Then
        assertEquals("9F06", aidTag.tag)
        assertEquals("Application Identifier (AID)", aidTag.description)
    }
}