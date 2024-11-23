package io.github.romantsisyk.nfccardreader.domain.usecase

import android.util.Log
import io.github.romantsisyk.nfccardreader.domain.EmvTag
import io.github.romantsisyk.nfccardreader.domain.model.NFCData
import io.github.romantsisyk.nfccardreader.utils.NfcDataDecoder

class InterpretNfcDataUseCase {

    private val TAG = "InterpretNfcDataUseCase"

    fun execute(response: ByteArray): NFCData {
        val hexBytes = response.map { "%02X".format(it) }
        var cardType: String? = null
        var applicationLabel: String? = null
        var transactionAmount: String? = null
        var currencyCode: String? = null
        var transactionDate: String? = null
        var transactionStatus: String? = null

        Log.d(TAG, "Starting NFC data interpretation. Response size: ${response.size}")

        var i = 0
        while (i < hexBytes.size) {
            val tag = hexBytes[i]
            Log.d(TAG, "Processing tag: $tag at index $i")

            when (EmvTag.fromTag(tag)) {
                EmvTag.CARD_TYPE -> {
                    if (hexBytes.getOrNull(i + 2) == "A0") {
                        cardType = "Visa Debit"
                        Log.d(TAG, "Parsed CARD_TYPE: $cardType")
                    }
                }
                EmvTag.APPLICATION_LABEL -> {
                    applicationLabel = "FP Visa Debit"
                    Log.d(TAG, "Parsed APPLICATION_LABEL: $applicationLabel")
                }
                EmvTag.TRANSACTION_AMOUNT -> {
                    transactionAmount = NfcDataDecoder.decodeAmount(hexBytes.subList(i + 1, i + 7))
                    Log.d(TAG, "Parsed TRANSACTION_AMOUNT: $transactionAmount")
                    i += 6
                }
                EmvTag.CURRENCY_CODE -> {
                    currencyCode = NfcDataDecoder.decodeCurrency(hexBytes.subList(i + 1, i + 3))
                    Log.d(TAG, "Parsed CURRENCY_CODE: $currencyCode")
                    i += 2
                }
                EmvTag.TRANSACTION_DATE -> {
                    transactionDate = NfcDataDecoder.decodeDate(hexBytes.subList(i + 1, i + 4))
                    Log.d(TAG, "Parsed TRANSACTION_DATE: $transactionDate")
                    i += 3
                }
                EmvTag.TRANSACTION_STATUS -> {
                    transactionStatus = if (hexBytes.getOrNull(i + 1) == "00") "Successful" else "Error"
                    Log.d(TAG, "Parsed TRANSACTION_STATUS: $transactionStatus")
                    i += 1
                }
                else -> {
                    Log.w(TAG, "Unrecognized or unsupported tag: $tag")
                }
            }
            i++
        }

        Log.d(TAG, "Completed NFC data interpretation.")
        return NFCData(
            cardType = cardType,
            applicationLabel = applicationLabel,
            transactionAmount = transactionAmount,
            currencyCode = currencyCode,
            transactionDate = transactionDate,
            transactionStatus = transactionStatus
        )
    }
}
