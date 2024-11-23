package io.github.romantsisyk.nfccardreader.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.romantsisyk.nfccardreader.R
import io.github.romantsisyk.nfccardreader.utils.orNA

@Composable
fun NFCReaderUI(viewModel: NFCReaderViewModel) {

    val nfcTagData by viewModel.nfcTagData.collectAsState()
    val rawResponse by viewModel.rawResponse.collectAsState()
    val additionalInfo by viewModel.additionalInfo.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Text(
                text = stringResource(R.string.nfc_reader_card_information),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )


            error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.Red)
                        .padding(8.dp)
                ) {
                    Text(
                        text = it,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.Green)
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.card_data_read_successfully),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text(stringResource(R.string.raw_nfc_response), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(stringResource(R.string.raw_response, rawResponse), fontSize = 16.sp)
                        }
                    }
                }


                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text(stringResource(R.string.parsed_tlv_data), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            if (nfcTagData.isNotEmpty()) {
                                nfcTagData.forEach { (key, value) ->
                                    Text("$key: $value", fontSize = 16.sp)
                                }
                            } else {
                                Text(stringResource(R.string.no_tlv_data_available), fontSize = 16.sp)
                            }
                        }
                    }
                }


                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Additional NFC Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            additionalInfo?.let {
                                Text("Card Type: ${it.cardType.orNA()}", fontSize = 16.sp)
                                Text("Application Label: ${it.applicationLabel.orNA()}", fontSize = 16.sp)
                                Text("Transaction Amount: ${it.transactionAmount.orNA()}", fontSize = 16.sp)
                                Text("Currency Code: ${it.currencyCode.orNA()}", fontSize = 16.sp)
                                Text("Transaction Date: ${it.transactionDate.orNA()}", fontSize = 16.sp)
                                Text("Transaction Status: ${it.transactionStatus.orNA()}", fontSize = 16.sp)
                            } ?: Text("No additional information available", fontSize = 16.sp)
                        }
                    }
                }


                item {
                    Button(
                        onClick = { viewModel.clearNfcData() },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 32.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear Data")
                    }
                }
            }
        }
    }
}
