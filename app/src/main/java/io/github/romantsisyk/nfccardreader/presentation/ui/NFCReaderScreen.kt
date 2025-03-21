package io.github.romantsisyk.nfccardreader.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.romantsisyk.nfccardreader.R
import io.github.romantsisyk.nfccardreader.presentation.viewmodel.NFCReaderViewModel
import io.github.romantsisyk.nfccardreader.utils.orNA

@Composable
fun NFCReaderUI(viewModel: NFCReaderViewModel) {

    val nfcTagData by viewModel.nfcTagData.collectAsState()
    val rawResponse by viewModel.rawResponse.collectAsState()
    val additionalInfo by viewModel.additionalInfo.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Додаємо стан для контролю відкриття/закриття розділів
    var showRawResponse by remember { mutableStateOf(true) }
    var showParsedTlvData by remember { mutableStateOf(true) }
    var showBasicCardInfo by remember { mutableStateOf(true) }
    var showAdvancedCardInfo by remember { mutableStateOf(false) }
    var showTransactionInfo by remember { mutableStateOf(true) }
    var showSecurityInfo by remember { mutableStateOf(false) }
    
    // Додаємо mock-кнопку
    var showMockButton by remember { mutableStateOf(true) }

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

            // Статус-бар для відображення помилок або успіху
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.clearNfcData() },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Data")
                        }
                        
                        if (showMockButton) {
                            Button(
                                onClick = { viewModel.processMockNfcIntent() },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Mock")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Mock Data")
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRawResponse = !showRawResponse }
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text(stringResource(R.string.raw_nfc_response), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    if (showRawResponse) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                            
                            if (showRawResponse) {
                                Text(stringResource(R.string.raw_response, rawResponse), fontSize = 16.sp)
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showBasicCardInfo = !showBasicCardInfo }
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text("Basic Card Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    if (showBasicCardInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                            
                            if (showBasicCardInfo) {
                                additionalInfo?.let {
                                    Text("Card Type: ${it.cardType.orNA()}", fontSize = 16.sp)
                                    
                                    if (nfcTagData.containsKey("Cardholder Name")) {
                                        Text("Cardholder Name: ${nfcTagData["Cardholder Name"].orNA()}", fontSize = 16.sp)
                                    }
                                    
                                    if (nfcTagData.containsKey("Application PAN")) {
                                        Text("Card Number: ${nfcTagData["Application PAN"].orNA()}", fontSize = 16.sp)
                                    }
                                    
                                    if (nfcTagData.containsKey("Expiration Date")) {
                                        Text("Expiration Date: ${nfcTagData["Expiration Date"].orNA()}", fontSize = 16.sp)
                                    }
                                    
                                    Text("Application Label: ${it.applicationLabel.orNA()}", fontSize = 16.sp)
                                    
                                    if (nfcTagData.containsKey("Application Preferred Name")) {
                                        Text("Application Preferred Name: ${nfcTagData["Application Preferred Name"].orNA()}", fontSize = 16.sp)
                                    }
                                } ?: Text("No card information available", fontSize = 16.sp)
                            }
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTransactionInfo = !showTransactionInfo }
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text("Transaction Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    if (showTransactionInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                            
                            if (showTransactionInfo) {
                                additionalInfo?.let {
                                    Text("Transaction Amount: ${it.transactionAmount.orNA()}", fontSize = 16.sp)
                                    Text("Currency Code: ${it.currencyCode.orNA()}", fontSize = 16.sp)
                                    Text("Transaction Date: ${it.transactionDate.orNA()}", fontSize = 16.sp)
                                    Text("Transaction Status: ${it.transactionStatus.orNA()}", fontSize = 16.sp)
                                    it.transactionType?.let { type ->
                                        Text("Transaction Type: $type", fontSize = 16.sp)
                                    }
                                    it.transactionCategoryCode?.let { code ->
                                        Text("Transaction Category: $code", fontSize = 16.sp)
                                    }
                                } ?: Text("No transaction information available", fontSize = 16.sp)
                            }
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAdvancedCardInfo = !showAdvancedCardInfo }
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text("Advanced Card Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    if (showAdvancedCardInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                            
                            if (showAdvancedCardInfo) {
                                additionalInfo?.let {
                                    it.applicationIdentifier?.let { id ->
                                        Text("Application Identifier: $id", fontSize = 16.sp)
                                    }
                                    it.applicationTemplate?.let { template ->
                                        Text("Application Template: $template", fontSize = 16.sp)
                                    }
                                    it.dedicatedFileName?.let { name ->
                                        Text("Dedicated File Name: $name", fontSize = 16.sp)
                                    }
                                    it.issuerCountryCode?.let { code ->
                                        Text("Issuer Country Code: $code", fontSize = 16.sp)
                                    }
                                    it.transactionCurrencyExponent?.let { exp ->
                                        Text("Currency Exponent: $exp", fontSize = 16.sp)
                                    }
                                    it.serviceCode?.let { code ->
                                        Text("Service Code: $code", fontSize = 16.sp)
                                    }
                                    it.issuerUrl?.let { url ->
                                        Text("Issuer URL: $url", fontSize = 16.sp)
                                    }
                                    it.formFactorIndicator?.let { indicator ->
                                        Text("Form Factor: $indicator", fontSize = 16.sp)
                                    }
                                    it.terminalCountryCode?.let { code ->
                                        Text("Terminal Country: $code", fontSize = 16.sp)
                                    }
                                    it.applicationCurrencyCode?.let { code ->
                                        Text("App Currency Code: $code", fontSize = 16.sp)
                                    }
                                } ?: Text("No advanced card information available", fontSize = 16.sp)
                            }
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSecurityInfo = !showSecurityInfo }
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text("Security Information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    if (showSecurityInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                            
                            if (showSecurityInfo) {
                                additionalInfo?.let {
                                    it.applicationCryptogram?.let { crypto ->
                                        Text("Application Cryptogram: $crypto", fontSize = 16.sp)
                                    }
                                    it.applicationTransactionCounter?.let { counter ->
                                        Text("Transaction Counter: $counter", fontSize = 16.sp)
                                    }
                                    it.applicationInterchangeProfile?.let { profile ->
                                        Text("Interchange Profile: $profile", fontSize = 16.sp)
                                    }
                                    it.terminalVerificationResults?.let { results ->
                                        Text("Terminal Verification: $results", fontSize = 16.sp)
                                    }
                                    it.cardholderVerificationMethodResults?.let { method ->
                                        Text("CVM Method: $method", fontSize = 16.sp)
                                    }
                                    it.issuerScriptResults?.let { results ->
                                        Text("Issuer Script Results: $results", fontSize = 16.sp)
                                    }
                                    it.unpredictableNumber?.let { number ->
                                        Text("Unpredictable Number: $number", fontSize = 16.sp)
                                    }
                                } ?: Text("No security information available", fontSize = 16.sp)
                            }
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showParsedTlvData = !showParsedTlvData }
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text(stringResource(R.string.parsed_tlv_data), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    if (showParsedTlvData) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                            
                            if (showParsedTlvData) {
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
                }
            }
        }
    }
}
