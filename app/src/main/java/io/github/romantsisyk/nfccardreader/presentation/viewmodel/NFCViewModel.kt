package io.github.romantsisyk.nfccardreader.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.romantsisyk.nfccardreader.domain.model.NFCData
import io.github.romantsisyk.nfccardreader.domain.usecase.ProcessNfcIntentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NFCReaderViewModel @Inject constructor(
    private val processNfcIntentUseCase: ProcessNfcIntentUseCase
) : ViewModel() {

    private val _nfcTagData = MutableStateFlow<Map<String, String>>(emptyMap())
    val nfcTagData: StateFlow<Map<String, String>> get() = _nfcTagData

    private val _rawResponse = MutableStateFlow("Empty NFC Response")
    val rawResponse: StateFlow<String> get() = _rawResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _additionalInfo = MutableStateFlow<NFCData?>(null)
    val additionalInfo: StateFlow<NFCData?> get() = _additionalInfo

    fun processNfcIntent(intent: Intent) {
        viewModelScope.launch {
            try {
                val data = processNfcIntentUseCase.execute(intent)
                _nfcTagData.value = data.parsedTlvData
                _rawResponse.value = data.rawResponse
                _additionalInfo.value = data
                _error.value = null // Clear errors
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearNfcData() {
        _nfcTagData.value = emptyMap()
        _rawResponse.value = "Empty NFC Response"
        _error.value = null
        _additionalInfo.value = null
    }

    fun processMockNfcIntent() {
        viewModelScope.launch {
            val mockData = NFCData(
                rawResponse = "6F 20 84 0E 325041592E5359532E4444463031 A5 0E 88 01 01 5F 2D 02 7265 9F 11 01 01 " +
                        "5F 20 0A 4A6F686E20446F65 5A 08 123456789012345F 24 03 240101 9F 12 06 56697361 " +
                        "57 13 1234567890123456D240101234567890 5F 28 02 0804 5F 30 02 0201 " +
                        "9F 02 06 000000012345 5F 2A 02 0840 9A 03 220306 90 00",
                parsedTlvData = mapOf(
                    "Cardholder Name" to "John Doe",
                    "Application PAN" to "1234 5678 9012 3456",
                    "Track2 Equivalent Data" to "1234567890123456D240101234567890",
                    "Expiration Date" to "01/24",
                    "Application Preferred Name" to "Visa Debit",
                    "Service Code" to "201",
                    "Issuer Country Code" to "USA",
                    "Dedicated File Name" to "2PAY.SYS.DDF01"
                ),
                cardType = "Visa Debit",
                applicationLabel = "Visa",
                transactionAmount = "123.45",
                currencyCode = "USD",
                transactionDate = "06.03.2022",
                transactionStatus = "Successful",
                applicationIdentifier = "Visa",
                dedicatedFileName = "2PAY.SYS.DDF01",
                issuerCountryCode = "USA",
                serviceCode = "International interchange, By issuer, No restrictions",
                formFactorIndicator = "Physical card with contact chip and contactless",
                applicationTemplate = "A5 0E 88 01 01 5F 2D 02 7265 9F 11 01 01",
                unpredictableNumber = "FE12CD45",
                cardholderVerificationMethodResults = "Online PIN verified",
                applicationCryptogram = "A1B2C3D4"
            )
            _nfcTagData.value = mockData.parsedTlvData
            _rawResponse.value = mockData.rawResponse
            _additionalInfo.value = mockData
            _error.value = null // Clear errors
        }
    }
}
