package com.jsbanez.currencyexchanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jsbanez.currencyexchanger.ui.theme.CurrencyExchangerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyExchangerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExchangeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ExchangeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val factory = ExchangeViewModelFactory(context)
    val vm: ExchangeViewModel = viewModel(factory = factory)

    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8EEF2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val quote = vm.computeQuote()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Currency Exchange",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )

                if (!state.isNetworkAvailable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ No internet connection",
                        color = Color(0xFFF39C12),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }

            // MY BALANCES Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MY BALANCES",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5D6D7E),
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.balances.keys.sorted().forEachIndexed { index, code ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(45.dp)
                                    .background(Color(0xFFE0E0E0))
                            )
                        }

                        Column(
                            modifier = Modifier.widthIn(min = 75.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5D6D7E),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.2f", state.balances[code] ?: 0.0),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C3E50),
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }

            // CURRENCY EXCHANGE Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CURRENCY EXCHANGE",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5D6D7E),
                    fontSize = 13.sp
                )

                ExchangeCard(
                    label = "SELL",
                    amount = state.inputAmount,
                    onAmountChanged = vm::setInputAmount,
                    currency = state.sellCurrency,
                    currencies = vm.availableCurrencies(),
                    onCurrencyChanged = vm::setSellCurrency,
                    isReadOnly = false,
                    iconColor = Color(0xFFE74C3C),
                    iconText = "↑"
                )

                // Divider between SELL and RECEIVE cards
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Divider(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }

                // RECEIVE Card
                ExchangeCard(
                    label = "RECEIVE",
                    amount = quote?.let { String.format("%.10f", it) } ?: "0.00",
                    onAmountChanged = {},
                    currency = state.buyCurrency,
                    currencies = vm.availableCurrencies(),
                    onCurrencyChanged = vm::setBuyCurrency,
                    isReadOnly = true,
                    iconColor = Color(0xFF2ECC71),
                    iconText = "↓"
                )

                // Live Rate Display !!!
                if (quote != null && state.inputAmount.toDoubleOrNull() != null) {
                    val rate = if (state.inputAmount.toDoubleOrNull()!! > 0) {
                        quote / state.inputAmount.toDoubleOrNull()!!
                    } else {
                        0.0
                    }

                    Text(
                        text = "Live rate: 1 ${state.sellCurrency} = %.2f ${state.buyCurrency}".format(
                            rate
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D6D7E),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // SUBMIT Button
            ElevatedButton(
                onClick = {
                    if (vm.performExchange()) {
                        val sold = state.inputAmount.toDoubleOrNull() ?: 0.0
                        val received = quote ?: 0.0
                        dialogMessage =
                            "You have converted %.2f %s to %.2f %s.".format(
                                sold,
                                state.sellCurrency,
                                received,
                                state.buyCurrency
                            )
                        showDialog = true
                    }
                },
                enabled = vm.canExchange(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFB0BEC5),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    "SUBMIT",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (state.isLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color(0xFF2196F3)
                    )
                }
            }

            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Success Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Currency converted") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Network connectivity dialog
    if (state.showNetworkDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissNetworkDialog() },
            title = { Text("No Internet Connection") },
            text = {
                Text("Please check your connection and try again.")
            },
            confirmButton = {
                TextButton(onClick = { vm.retryNetworkOperation() }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissNetworkDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeCard(
    label: String,
    amount: String,
    onAmountChanged: (String) -> Unit,
    currency: String,
    currencies: List<String>,
    onCurrencyChanged: (String) -> Unit,
    isReadOnly: Boolean,
    iconColor: Color,
    iconText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D6D7E),
            fontSize = 12.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            ) {
                Text(
                    text = iconText,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount Input
            if (isReadOnly) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50),
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            } else {
                TextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    placeholder = {
                        Text(
                            "0.00",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFBDC3C7),
                            fontSize = 22.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        fontSize = 22.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Currency Dropdown
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (expanded) "▲" else "▼",
                        fontSize = 10.sp,
                        color = Color(0xFF5D6D7E)
                    )
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 15.sp
                                )
                            },
                            onClick = {
                                onCurrencyChanged(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExchangePreview() {
    CurrencyExchangerTheme {
        ExchangeScreen()
    }
}