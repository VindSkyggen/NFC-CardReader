package io.github.romantsisyk.nfccardreader.app

import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.romantsisyk.nfccardreader.presentation.ui.NFCReaderUI
import io.github.romantsisyk.nfccardreader.presentation.viewmodel.NFCReaderViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: NFCReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NFCReaderUI(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        intent?.let {
            Log.d("MainActivity", "Intent action: ${it.action}")
            if (it.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
                lifecycleScope.launch {
                    try {
                        viewModel.processNfcIntent(it)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error processing NFC intent", e)
                    }
                }
            }
        }
    }

}
