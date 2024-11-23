package io.github.romantsisyk.nfccardreader.model

data class NFCData(
    val cardType: String?,
    val applicationLabel: String?,
    val transactionAmount: String?,
    val currencyCode: String?,
    val transactionDate: String?,
    val transactionStatus: String?
)