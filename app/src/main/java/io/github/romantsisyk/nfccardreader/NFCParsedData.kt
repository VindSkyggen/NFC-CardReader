package io.github.romantsisyk.nfccardreader

data class NFCParsedData(
    val asciiRepresentation: String?,
    val hexRepresentation: String?,
    val tlvData: Map<String, String>?,
    val additionalInfo: NFCData?
)

data class NFCData(
    val cardType: String?,
    val applicationLabel: String?,
    val transactionAmount: String?,
    val currencyCode: String?,
    val transactionDate: String?,
    val transactionStatus: String?
)