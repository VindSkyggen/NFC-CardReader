package io.github.romantsisyk.nfccardreader.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import androidx.lifecycle.ViewModel
import io.github.romantsisyk.nfccardreader.EmvTag
import io.github.romantsisyk.nfccardreader.model.NFCData
import io.github.romantsisyk.nfccardreader.EmvTag.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NFCReaderViewModel : ViewModel() {

    private val _nfcTagData = MutableStateFlow<Map<String, String>>(emptyMap())
    val nfcTagData: StateFlow<Map<String, String>> = _nfcTagData

    private val _rawResponse = MutableStateFlow("Empty NFC Response")
    val rawResponse: StateFlow<String> = _rawResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _additionalInfo = MutableStateFlow<NFCData?>(null)
    val additionalInfo: StateFlow<NFCData?> = _additionalInfo

    fun processNfcIntent(intent: Intent) {
        val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        if (tag != null) {
            processTag(tag)
        } else {
            _error.value = "No NFC tag found in the intent"
        }
    }

    private fun processTag(tag: Tag) {
        try {
            val isoDep = IsoDep.get(tag)
            if (isoDep != null) {
                isoDep.connect()
                Log.d("NFCReader", "IsoDep connection established")

                // https://shift4.zendesk.com/hc/en-us/articles/4406720359955-Application-Identifier-Card-Type-Definitions-for-EMV-Configuration
                val selectVisaCommand = byteArrayOf(
                    0x00.toByte(),
                    0xA4.toByte(),
                    0x04.toByte(),
                    0x00.toByte(),
                    0x07.toByte(),
                    0xA0.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x03.toByte(),
                    0x10.toByte(),
                    0x10.toByte(),
                    0x00.toByte()
                )
                Log.d(
                    "NFCReader",
                    "APDU Command Sent: ${selectVisaCommand.joinToString(", ") { "%02X".format(it) }}"
                )

                val response = isoDep.transceive(selectVisaCommand)

                // Update raw response state
                _rawResponse.value = parseNfcResponse(response)

                Log.d("NFCReader", "APDU Command Sent: ${selectVisaCommand.joinToString(", ") { "%02X".format(it) }}")
                Log.d("NFCReader", "Raw Response: ${response.joinToString(", ") { "%02X".format(it) }}")

                // Parse TLV Data
                val parsedTlvData = parseTLV(response)
                _nfcTagData.value = parsedTlvData

                // Parse Additional Information
                val parsedAdditionalInfo = interpretAdditionalNfcData(response)
                _additionalInfo.value = parsedAdditionalInfo
                Log.d("NFCReader", "_additionalInfo: $parsedAdditionalInfo")
            } else {
                _error.value = "Unsupported NFC Tag"
            }
        } catch (e: Exception) {
            _error.value = "Error reading tag: ${e.message}"
        }
    }

    private fun parseTLV(data: ByteArray): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var index = 0

        while (index < data.size) {
            val tag = data[index++].toInt().and(0xFF).toString(16).padStart(2, '0').uppercase()
            if (index >= data.size) break
            val length = data[index++].toInt().and(0xFF)
            if (index + length > data.size) break
            val value = data.sliceArray(index until index + length)
            index += length

            // Map tags based on EMVLab's tag list https://emvlab.org/emvtags/all/
            when (val emvTag = EmvTag.fromTag(tag)) {
                CARDHOLDER_NAME -> result[emvTag.name] = value.toString(Charsets.UTF_8)
                APPLICATION_PAN -> result[emvTag.name] = maskPan(value.joinToString("") { "%02X".format(it) })
                TRACK2_EQUIVALENT_DATA -> result[emvTag.name] = maskTrack2Data(value.joinToString("") { "%02X".format(it) })
                EXPIRATION_DATE -> result[emvTag.name] = value.joinToString("") { "%02X".format(it) }
                APPLICATION_PREFERRED_NAME -> result[emvTag.name] = value.toString(Charsets.UTF_8)
                UNKNOWN -> result["Tag $tag"] = value.joinToString("") { "%02X".format(it) }
                else -> Unit
            }
        }
        return result
    }

    private fun parseNfcResponse(response: ByteArray): String {
        return try {
            if (response.isNotEmpty()) {
                val asciiRepresentation = response.joinToString("") { byte ->
                    if (byte in 32..126) byte.toInt().toChar().toString() else "."
                }
                "ASCII: $asciiRepresentation\nHex: ${response.joinToString(" ") { "%02X".format(it) }}"
            } else {
                "Empty response from NFC tag"
            }
        } catch (e: Exception) {
            "Error parsing NFC response: ${e.message}"
        }
    }

    private fun interpretAdditionalNfcData(response: ByteArray): NFCData {
        val hexBytes = response.map { "%02X".format(it) }
        var cardType: String? = null
        var applicationLabel: String? = null
        var transactionAmount: String? = null
        var currencyCode: String? = null
        var transactionDate: String? = null
        var transactionStatus: String? = null

        var i = 0
        while (i < hexBytes.size) {
            val tag = hexBytes[i]
            when (EmvTag.fromTag(tag)) {
                CARD_TYPE -> {
                    if (hexBytes.getOrNull(i + 2) == "A0") {
                        cardType = "Visa Debit"
                    }
                }
                APPLICATION_LABEL -> {
                    applicationLabel = "FP Visa Debit"
                }
                TRANSACTION_AMOUNT -> {
                    transactionAmount = decodeAmount(hexBytes.subList(i + 1, i + 7))
                    i += 6
                }
                CURRENCY_CODE -> {
                    currencyCode = decodeCurrency(hexBytes.subList(i + 1, i + 3))
                    i += 2
                }
                // TRANSACTION_DATE AS EXAMPLE(Tag: 9A, Length: 3 bytes)
                // Extracts the transaction date in the format YYMMDD (e.g., "2024-11-23").
                // - `i + 1`: Skips the tag byte (9A).
                // - `i + Length + 1`: Reads the next 3 bytes (Length = 3) for the date value.
                // - `i += Length`: Advances the index by 3 to continue parsing the next tag.

                TRANSACTION_DATE -> {
                    transactionDate = decodeDate(hexBytes.subList(i + 1, i + 4))
                    i += 3
                }
                TRANSACTION_STATUS -> {
                    transactionStatus = if (hexBytes.getOrNull(i + 1) == "00") "Successful" else "Error"
                    i += 1
                }
                else -> Unit
            }
            i++
        }

        return NFCData(
            cardType = cardType,
            applicationLabel = applicationLabel,
            transactionAmount = transactionAmount,
            currencyCode = currencyCode,
            transactionDate = transactionDate,
            transactionStatus = transactionStatus
        )
    }

    private fun decodeAmount(bytes: List<String>): String {
        val hexValue = bytes.joinToString("")
        return "${hexValue.toLong(16) / 100.0} USD"
    }

    private fun decodeCurrency(bytes: List<String>): String {
        val hexValue = bytes.joinToString("")
        return when (hexValue) {
            "0840" -> "USD"
            "0978" -> "EUR"
            else -> "Unknown Currency"
        }
    }

    private fun decodeDate(bytes: List<String>): String {
        val year = "20${bytes[0]}"
        val month = bytes[1]
        val day = bytes[2]
        return "$year-$month-$day"
    }

    private fun maskPan(pan: String): String {
        return pan.replace(Regex("\\d(?=\\d{4})"), "X")
    }

    private fun maskTrack2Data(track2: String): String {
        return track2.replace(Regex("\\d(?=\\d{4})"), "X").replace("=", "X")
    }

    fun clearNfcData() {
        _nfcTagData.value = emptyMap()
        _rawResponse.value = ""
        _error.value = null
        _additionalInfo.value = null
    }
}
