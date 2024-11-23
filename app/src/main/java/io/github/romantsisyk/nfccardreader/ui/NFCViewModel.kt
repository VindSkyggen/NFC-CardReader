package io.github.romantsisyk.nfccardreader.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.romantsisyk.nfccardreader.model.NFCData
import io.github.romantsisyk.nfccardreader.usecase.ProcessNfcIntentUseCase
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
}
