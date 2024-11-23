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
        _rawResponse.value = ""
        _error.value = null
        _additionalInfo.value = null
    }

    fun processMockNfcIntent() {
        viewModelScope.launch {
            val mockData = NFCData(
                rawResponse = "5F20 0A4A6F686E20446F655A08123456789012345F24032401019F12065669736120",
                parsedTlvData = mapOf(
                    "Cardholder Name" to "John Doe",
                    "Application PAN" to "1234 5678 9012 3456",
                    "Track2 Equivalent Data" to "1234567890123456D240101234567890",
                    "Expiration Date" to "03/24",
                    "Application Preferred Name" to "Visa Debit"
                ),
                cardType = "Visa Debit",
                applicationLabel = "FP Visa Debit",
                transactionAmount = "123.45",
                currencyCode = "USD",
                transactionDate = "24-01-2023",
                transactionStatus = "Successful"
            )
            _nfcTagData.value = mockData.parsedTlvData
            _rawResponse.value = mockData.rawResponse
            _additionalInfo.value = mockData
            _error.value = null // Clear errors
        }
    }

}
