package io.github.romantsisyk.nfccardreader.usecase

import io.github.romantsisyk.nfccardreader.EmvTag
import io.github.romantsisyk.nfccardreader.model.NFCData
import io.github.romantsisyk.nfccardreader.utils.NfcDataDecoder

class InterpretNfcDataUseCase {

    fun execute(response: ByteArray): NFCData {
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
                EmvTag.CARD_TYPE -> {
                    if (hexBytes.getOrNull(i + 2) == "A0") {
                        cardType = "Visa Debit"
                    }
                }
                EmvTag.APPLICATION_LABEL -> {
                    applicationLabel = "FP Visa Debit"
                }
                EmvTag.TRANSACTION_AMOUNT -> {
                    transactionAmount = NfcDataDecoder.decodeAmount(hexBytes.subList(i + 1, i + 7))
                    i += 6
                }
                EmvTag.CURRENCY_CODE -> {
                    currencyCode = NfcDataDecoder.decodeCurrency(hexBytes.subList(i + 1, i + 3))
                    i += 2
                }
                EmvTag.TRANSACTION_DATE -> {
                    transactionDate = NfcDataDecoder.decodeDate(hexBytes.subList(i + 1, i + 4))
                    i += 3
                }
                EmvTag.TRANSACTION_STATUS -> {
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
}
