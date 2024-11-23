package io.github.romantsisyk.nfccardreader.model

data class NFCData(
    val rawResponse: String = "",
    val cardType: String? = null,
    val applicationLabel: String? = null,
    val transactionAmount: String? = null,
    val currencyCode: String? = null,
    val transactionDate: String? = null,
    val transactionStatus: String? = null,
    val parsedTlvData: Map<String, String> = emptyMap() // Add this property
)
