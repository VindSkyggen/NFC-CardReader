package io.github.romantsisyk.nfccardreader

enum class EmvTag(val tag: String) {
    // Tags for TLV parsing
    CARDHOLDER_NAME("5F20"),
    APPLICATION_PAN("5A"),
    TRACK2_EQUIVALENT_DATA("57"),
    EXPIRATION_DATE("5F24"),
    APPLICATION_PREFERRED_NAME("9F12"),

    // Additional tags for interpreting NFC data
    CARD_TYPE("6F"),
    APPLICATION_LABEL("50"),
    TRANSACTION_AMOUNT("9F02"),
    CURRENCY_CODE("5F2A"),
    TRANSACTION_DATE("9A"),
    TRANSACTION_STATUS("90"),

    // Fallback for unknown tags
    UNKNOWN("FFFF");

    companion object {
        fun fromTag(tag: String): EmvTag {
            return entries.find { it.tag == tag } ?: UNKNOWN
        }
    }
}