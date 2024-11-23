package io.github.romantsisyk.nfccardreader

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.romantsisyk.nfccardreader.ui.NFCReaderUI
import io.github.romantsisyk.nfccardreader.ui.NFCReaderViewModel
import kotlinx.coroutines.launch

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
            if (it.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
                lifecycleScope.launch {
                    viewModel.processNfcIntent(it)
                }
            }
        }
    }
}
