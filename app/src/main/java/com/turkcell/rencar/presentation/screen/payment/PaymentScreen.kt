package com.turkcell.rencar.presentation.screen.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar.R
import com.turkcell.rencar.domain.cards.Card
import com.turkcell.rencar.domain.cards.CardBrand
import com.turkcell.rencar.domain.rental.PaymentMethod
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentRoute(
    rentalId: String,
    onNavigateHome: () -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Ödeme tamamlanmadan geri gidilirse kiralama ödemesiz kalır; sistem geri tuşu bilinçli olarak engellenir.
    BackHandler(enabled = true) {}

    LaunchedEffect(rentalId) {
        viewModel.onIntent(PaymentIntent.ScreenStarted(rentalId))
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PaymentEffect.NavigateHome -> onNavigateHome()
                PaymentEffect.NavigateToWallet -> onNavigateToWallet()
            }
        }
    }

    PaymentScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun PaymentScreen(
    state: PaymentState,
    onIntent: (PaymentIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .padding(horizontal = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.payment_completed_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 12.dp)
                )
                val vehicleDetails = listOfNotNull(
                    state.vehicleName.takeIf { it.isNotBlank() },
                    state.vehiclePlate.takeIf { it.isNotBlank() }
                ).joinToString(separator = " · ")
                if (vehicleDetails.isNotBlank()) {
                    Text(
                        text = vehicleDetails,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.payment_duration_label),
                    value = stringResource(R.string.payment_duration_minutes, state.durationMinutes.toWholeText()),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.payment_distance_label),
                    value = stringResource(R.string.payment_distance_km, state.distanceKm.toKmText()),
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PriceRow(
                        label = stringResource(R.string.payment_usage_fee_label),
                        value = state.usageFee.toTryPrice()
                    )
                    PriceRow(
                        label = stringResource(R.string.payment_start_fee_label),
                        value = state.startFee.toTryPrice()
                    )
                    PriceRow(
                        label = stringResource(R.string.payment_service_fee_label),
                        value = state.serviceFee.toTryPrice()
                    )
                    if (state.discountAmount > 0.0) {
                        PriceRow(
                            label = stringResource(R.string.payment_discount_label),
                            value = "-${state.discountAmount.toTryPrice()}",
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.payment_total_label),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = state.netAmount.toTryPrice(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }

            PaymentMethodSelector(
                selectedMethod = state.selectedMethod,
                onMethodSelected = { onIntent(PaymentIntent.MethodSelected(it)) },
                modifier = Modifier.padding(top = 16.dp)
            )

            when (state.selectedMethod) {
                PaymentMethod.WALLET -> WalletMethodRow(
                    balance = state.walletBalance,
                    sufficient = state.walletBalance >= state.netAmount,
                    modifier = Modifier.padding(top = 10.dp)
                )

                PaymentMethod.CARD -> CardMethodRow(
                    card = state.selectedCard,
                    onChangeCardClicked = { onIntent(PaymentIntent.ChangeCardClicked) },
                    onAddCardClicked = { onIntent(PaymentIntent.AddCardClicked) },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            val payButtonEnabled = !state.isPaying && !state.isPaid &&
                (state.selectedMethod == PaymentMethod.WALLET || state.selectedCardId != null)

            Button(
                onClick = { onIntent(PaymentIntent.PayClicked) },
                enabled = payButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (state.isPaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    val methodLabel = when (state.selectedMethod) {
                        PaymentMethod.WALLET -> stringResource(R.string.payment_method_wallet)
                        PaymentMethod.CARD -> stringResource(R.string.payment_method_card)
                    }
                    Text(
                        text = stringResource(R.string.payment_pay_action, methodLabel, state.netAmount.toTryPrice()),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (state.showInsufficientBalanceDialog) {
        InsufficientBalanceDialog(
            onConfirm = { onIntent(PaymentIntent.TopUpConfirmed) },
            onDismiss = { onIntent(PaymentIntent.TopUpDismissed) }
        )
    }

    if (state.showCardPicker) {
        CardPickerDialog(
            cards = state.cards,
            onCardSelected = { onIntent(PaymentIntent.CardSelected(it)) },
            onAddCardClicked = { onIntent(PaymentIntent.AddCardClicked) },
            onDismiss = { onIntent(PaymentIntent.CardPickerDismissed) }
        )
    }

    if (state.showAddCardDialog) {
        AddCardDialog(state = state, onIntent = onIntent)
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MethodOption(
            label = stringResource(R.string.payment_method_wallet),
            selected = selectedMethod == PaymentMethod.WALLET,
            onClick = { onMethodSelected(PaymentMethod.WALLET) },
            modifier = Modifier.weight(1f)
        )
        MethodOption(
            label = stringResource(R.string.payment_method_card),
            selected = selectedMethod == PaymentMethod.CARD,
            onClick = { onMethodSelected(PaymentMethod.CARD) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MethodOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun WalletMethodRow(balance: Double, sufficient: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.payment_wallet_balance_label, balance.toTryPrice()),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(
                        if (sufficient) R.string.payment_wallet_sufficient else R.string.payment_wallet_insufficient
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sufficient) {
                        RenCarSuccessColor
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
private fun CardMethodRow(
    card: Card?,
    onChangeCardClicked: () -> Unit,
    onAddCardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (card != null) {
                    Text(
                        text = "${card.brand.name} •••• ${card.last4}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.payment_no_card_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (card != null) {
                TextButton(onClick = onChangeCardClicked) {
                    Text(text = stringResource(R.string.payment_change_card_action))
                }
            }
            TextButton(onClick = onAddCardClicked) {
                Text(text = stringResource(R.string.payment_add_card_action))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (valueColor == Color.Unspecified) {
                MaterialTheme.colorScheme.onSurface
            } else {
                valueColor
            }
        )
    }
}

@Composable
private fun InsufficientBalanceDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.payment_insufficient_dialog_title), fontWeight = FontWeight.Bold) },
        text = { Text(text = stringResource(R.string.payment_insufficient_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.payment_insufficient_dialog_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.payment_insufficient_dialog_dismiss))
            }
        }
    )
}

@Composable
private fun CardPickerDialog(
    cards: List<Card>,
    onCardSelected: (String) -> Unit,
    onAddCardClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.payment_card_picker_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                cards.forEach { card ->
                    Text(
                        text = "${card.brand.name} •••• ${card.last4}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCardSelected(card.id) }
                            .padding(vertical = 12.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.payment_add_card_action),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddCardClicked)
                        .padding(vertical = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.payment_card_picker_dismiss))
            }
        }
    )
}

@Composable
private fun AddCardDialog(state: PaymentState, onIntent: (PaymentIntent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onIntent(PaymentIntent.AddCardDismissed) },
        title = { Text(text = stringResource(R.string.wallet_add_card_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardBrand.entries.forEach { brand ->
                        val selected = brand == state.addCardBrand
                        Surface(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable { onIntent(PaymentIntent.AddCardBrandChanged(brand)) }
                        ) {
                            Text(
                                text = brand.name,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.addCardLast4Input,
                    onValueChange = { onIntent(PaymentIntent.AddCardLast4Changed(it)) },
                    label = { Text(text = stringResource(R.string.wallet_add_card_last4_label)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    OutlinedTextField(
                        value = state.addCardExpMonthInput,
                        onValueChange = { onIntent(PaymentIntent.AddCardExpMonthChanged(it)) },
                        label = { Text(text = stringResource(R.string.wallet_add_card_exp_month_label)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.addCardExpYearInput,
                        onValueChange = { onIntent(PaymentIntent.AddCardExpYearChanged(it)) },
                        label = { Text(text = stringResource(R.string.wallet_add_card_exp_year_label)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                state.addCardErrorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntent(PaymentIntent.AddCardConfirmClicked) },
                enabled = !state.isAddCardSubmitting
            ) {
                if (state.isAddCardSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(R.string.wallet_add_card_confirm_action), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(PaymentIntent.AddCardDismissed) }) {
                Text(text = stringResource(R.string.wallet_add_card_cancel_action))
            }
        }
    )
}

private val RenCarSuccessColor = Color(0xFF1A9E63)

private fun Double.toTryPrice(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "₺${formatter.format(this)}"
}

private fun Double.toKmText(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    return formatter.format(this)
}

private fun Double.toWholeText(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }
    return formatter.format(this)
}