package io.github.romantsisyk.nfccardreader.utils

object NfcDataDecoder {
    fun decodeAmount(bytes: List<String>): String {
        val hexValue = bytes.joinToString("")
        return "${hexValue.toLong(16) / 100.0} USD"
    }

    fun decodeCurrency(bytes: List<String>): String {
        val hexValue = bytes.joinToString("")
        return when (hexValue) {
            "0840" -> "USD"
            "0978" -> "EUR"
            else -> "Unknown Currency"
        }
    }

    fun decodeDate(bytes: List<String>): String {
        val year = "20${bytes[0]}"
        val month = bytes[1]
        val day = bytes[2]
        return "$year-$month-$day"
    }
}
