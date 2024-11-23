package io.github.romantsisyk.nfccardreader.usecase

import android.util.Log
import io.github.romantsisyk.nfccardreader.EmvTag
import io.github.romantsisyk.nfccardreader.utils.NfcDataMasker

class ParseTLVUseCase {

    private val TAG = "ParseTLVUseCase"

    fun execute(data: ByteArray): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var index = 0

        Log.d(TAG, "Starting TLV parsing. Data size: ${data.size}")

        while (index < data.size) {
            val tag = data[index++].toInt().and(0xFF).toString(16).padStart(2, '0').uppercase()
            Log.d(TAG, "Found tag: $tag at index $index")

            if (index >= data.size) {
                Log.w(TAG, "Index out of bounds after reading tag.")
                break
            }

            val length = data[index++].toInt().and(0xFF)
            Log.d(TAG, "Length of tag $tag: $length")

            if (index + length > data.size) {
                Log.w(TAG, "Not enough data remaining for tag $tag. Breaking.")
                break
            }

            val value = data.sliceArray(index until index + length)
            index += length
            Log.d(TAG, "Value for tag $tag: ${value.joinToString("") { "%02X".format(it) }}")

            // Map tags based on EMV tag definitions
            when (val emvTag = EmvTag.fromTag(tag)) {
                EmvTag.CARDHOLDER_NAME -> {
                    val name = value.toString(Charsets.UTF_8)
                    Log.d(TAG, "Parsed CARDHOLDER_NAME: $name")
                    result[emvTag.name] = name
                }

                EmvTag.APPLICATION_PAN -> {
                    val pan = NfcDataMasker.maskPan(value.joinToString("") { "%02X".format(it) })
                    Log.d(TAG, "Parsed APPLICATION_PAN: $pan")
                    result[emvTag.name] = pan
                }

                EmvTag.TRACK2_EQUIVALENT_DATA -> {
                    val track2Data = NfcDataMasker.maskTrack2Data(value.joinToString("") { "%02X".format(it) })
                    Log.d(TAG, "Parsed TRACK2_EQUIVALENT_DATA: $track2Data")
                    result[emvTag.name] = track2Data
                }

                EmvTag.EXPIRATION_DATE -> {
                    val expirationDate = value.joinToString("") { "%02X".format(it) }
                    Log.d(TAG, "Parsed EXPIRATION_DATE: $expirationDate")
                    result[emvTag.name] = expirationDate
                }

                EmvTag.APPLICATION_PREFERRED_NAME -> {
                    val preferredName = value.toString(Charsets.UTF_8)
                    Log.d(TAG, "Parsed APPLICATION_PREFERRED_NAME: $preferredName")
                    result[emvTag.name] = preferredName
                }

                EmvTag.UNKNOWN -> {
                    val unknownTagValue = value.joinToString("") { "%02X".format(it) }
                    Log.d(TAG, "Parsed UNKNOWN Tag $tag: $unknownTagValue")
                    result["Tag $tag"] = unknownTagValue
                }

                else -> {
                    Log.d(TAG, "Unrecognized tag $tag. Skipping.")
                }
            }
        }

        Log.d(TAG, "Completed TLV parsing. Parsed ${result.size} tags.")
        return result
    }
}
