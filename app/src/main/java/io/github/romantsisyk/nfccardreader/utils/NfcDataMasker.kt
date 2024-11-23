package io.github.romantsisyk.nfccardreader.utils

object NfcDataMasker {
    fun maskPan(pan: String): String {
        return pan.replace(Regex("\\d(?=\\d{4})"), "X")
    }

    fun maskTrack2Data(track2: String): String {
        return track2.replace(Regex("\\d(?=\\d{4})"), "X").replace("=", "X")
    }
}