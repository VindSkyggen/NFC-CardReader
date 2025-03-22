package io.github.romantsisyk.nfccardreader.utils

object NfcDataMasker {
    fun maskPan(pan: String): String {
        if (pan.isEmpty()) return ""
        return pan.replace(Regex("\\d(?=\\d{4})"), "X")
    }

    fun maskTrack2Data(track2: String): String {
        if (track2.isEmpty()) return ""
        
        // First mask digits in the PAN part
        val maskedDigits = track2.replace(Regex("\\d(?=\\d{4})"), "X")
        
        // Then safely replace equals sign if it exists
        return if (maskedDigits.contains("=")) {
            maskedDigits.replace("=", "X")
        } else {
            maskedDigits
        }
    }
}