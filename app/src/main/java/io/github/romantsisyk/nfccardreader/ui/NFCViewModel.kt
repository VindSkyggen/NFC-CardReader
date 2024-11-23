package io.github.romantsisyk.nfccardreader.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import androidx.lifecycle.ViewModel
import io.github.romantsisyk.nfccardreader.NFCData
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
            Log.d("NFCReader", "NFC Tag detected: $tag")
            processTag(tag)
        } else {
            _error.value = "No NFC tag found in the intent"
            Log.e("NFCReader", "No NFC tag found in the intent")
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
                Log.d(
                    "NFCReader",
                    "Raw Response Received: ${response.joinToString(", ") { "%02X".format(it) }}"
                )

                // Update raw response state
                _rawResponse.value = parseNfcResponse(response)
                Log.d("NFCReader", "Parsed Raw Response: ${_rawResponse.value}")

                // Parse TLV Data
                val parsedTlvData = parseTLV(response)
                _nfcTagData.value = parsedTlvData
                Log.d("NFCReader", "Parsed TLV Data: $parsedTlvData")

                // Parse Additional Information
                val parsedAdditionalInfo = interpretAdditionalNfcData(response)
                _additionalInfo.value = parsedAdditionalInfo
                Log.d("NFCReader", "Parsed Additional NFC Data: $parsedAdditionalInfo")
            } else {
                _error.value = "Unsupported NFC Tag"
                Log.e("NFCReader", "Unsupported NFC Tag")
            }
        } catch (e: Exception) {
            _error.value = "Error reading tag: ${e.message}"
            Log.e("NFCReader", "Error reading tag", e)
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
            when (tag) {
                "5F20" -> result["Cardholder Name"] = value.toString(Charsets.UTF_8)
                "5A" -> result["Application PAN"] = value.joinToString("") { "%02X".format(it) }
                "57" -> result["Track 2 Equivalent Data"] =
                    value.joinToString("") { "%02X".format(it) }

                "9F12" -> result["Application Preferred Name"] = value.toString(Charsets.UTF_8)
                "9F36" -> result["Application Transaction Counter"] =
                    value.joinToString("") { "%02X".format(it) }

                "9F26" -> result["Application Cryptogram"] =
                    value.joinToString("") { "%02X".format(it) }

                "9F10" -> result["Issuer Application Data"] =
                    value.joinToString("") { "%02X".format(it) }

                "9F27" -> result["Cryptogram Information Data"] =
                    value.joinToString("") { "%02X".format(it) }

                "9F34" -> result["Cardholder Verification Method (CVM)"] =
                    value.joinToString("") { "%02X".format(it) }

                else -> result["Tag $tag"] = value.joinToString("") { "%02X".format(it) }
            }
        }

        Log.d("NFCReader", "Final Parsed TLV Map: $result")
        return result
    }

    private fun parseNfcResponse(response: ByteArray): String {
        return try {
            if (response.isNotEmpty()) {
                val asciiRepresentation = response.joinToString("") { byte ->
                    if (byte in 32..126) byte.toInt().toChar().toString() else "."
                }
                val parsed = "ASCII: $asciiRepresentation\nHex: ${
                    response.joinToString(" ") {
                        "%02X".format(it)
                    }
                }"
                Log.d("NFCReader", "ASCII and Hex Parsed Response: $parsed")
                parsed
            } else {
                "Empty response from NFC tag"
            }
        } catch (e: Exception) {
            Log.e("NFCReader", "Error parsing NFC response", e)
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
            // Map tags based on EMVLab's tag list https://emvlab.org/emvtags/all/
            when (tag) {
                "6F" -> if (hexBytes.getOrNull(i + 2) == "A0") cardType = "Visa Debit"
                "50" -> applicationLabel = "FP Visa Debit"
                "9F02" -> transactionAmount = decodeAmount(hexBytes.subList(i + 1, i + 7))
                "5F2A" -> currencyCode = decodeCurrency(hexBytes.subList(i + 1, i + 3))
                "9A" -> transactionDate = decodeDate(hexBytes.subList(i + 1, i + 4))
                "90" -> transactionStatus =
                    if (hexBytes.getOrNull(i + 1) == "00") "Successful" else "Error"
            }
            i++
        }

        val parsedData = NFCData(
            cardType = cardType,
            applicationLabel = applicationLabel,
            transactionAmount = transactionAmount,
            currencyCode = currencyCode,
            transactionDate = transactionDate,
            transactionStatus = transactionStatus
        )

        Log.d("NFCReader", "Interpreted Additional NFC Data: $parsedData")
        return parsedData
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

    fun clearNfcData() {
        _nfcTagData.value = emptyMap()
        _rawResponse.value = ""
        _error.value = null
        _additionalInfo.value = null
        Log.d("NFCReader", "Cleared all NFC data")
    }
}