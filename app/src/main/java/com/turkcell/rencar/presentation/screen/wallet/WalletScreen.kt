package com.turkcell.rencar.presentation.screen.wallet

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.domain.wallet.WalletTransactionType
import com.turkcell.rencar.presentation.component.card.CardBrandBadge
import com.turkcell.rencar.presentation.component.navigation.BottomNavBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WalletRoute(
    onNavigateToMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(WalletIntent.ScreenStarted)
        viewModel.effect.collect { effect ->
            when (effect) {
                WalletEffect.NavigateToMap -> onNavigateToMap()
                WalletEffect.NavigateToHistory -> onNavigateToHistory()
                WalletEffect.NavigateToProfile -> onNavigateToProfile()
            }
        }
    }

    WalletScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun WalletScreen(
    state: WalletState,
    onIntent: (WalletIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.wallet_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.wallet_balance_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        text = state.balance.toTryPrice(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Button(
                        onClick = { onIntent(WalletIntent.TopUpClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.wallet_topup_action),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.wallet_cards_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onIntent(WalletIntent.AddCardClicked) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.wallet_add_card_action),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (state.cards.isEmpty()) {
                Text(
                    text = stringResource(R.string.wallet_no_cards_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.cards.forEach { card ->
                CardRow(
                    card = card,
                    onSetDefaultClicked = { onIntent(WalletIntent.SetDefaultCardClicked(card.id)) },
                    onDeleteClicked = { onIntent(WalletIntent.DeleteCardClicked(card.id)) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Text(
                text = stringResource(R.string.wallet_transactions_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp, bottom = 11.dp)
            )

            if (state.transactions.isEmpty()) {
                Text(
                    text = stringResource(R.string.wallet_no_transactions_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(horizontal = 14.dp)) {
                        items(state.transactions, key = { it.id }) { transaction ->
                            TransactionRow(transaction = transaction)
                        }
                    }
                }
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

        BottomNavBar(
            selectedItem = state.selectedNavItem,
            onItemSelected = { onIntent(WalletIntent.NavItemSelected(it)) }
        )
    }

    if (state.showTopUpDialog) {
        TopUpDialog(state = state, onIntent = onIntent)
    }

    if (state.showAddCardDialog) {
        AddCardDialog(state = state, onIntent = onIntent)
    }

    if (state.pendingDeleteCardId != null) {
        DeleteCardConfirmDialog(
            errorMessage = state.deleteCardErrorMessage,
            isDeleting = state.isDeletingCard,
            onConfirm = { onIntent(WalletIntent.DeleteCardConfirmed) },
            onDismiss = { onIntent(WalletIntent.DeleteCardDismissed) }
        )
    }
}

@Composable
private fun CardRow(
    card: Card,
    onSetDefaultClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardBrandBadge(brand = card.brand, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "•••• ${card.last4}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(
                        R.string.wallet_card_expiry,
                        "%02d".format(card.expMonth),
                        (card.expYear % 100).toString().padStart(2, '0')
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (card.isDefault) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.wallet_card_default_badge),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.wallet_set_default_card_action),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onSetDefaultClicked)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.wallet_delete_card_action),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(20.dp)
                        .clickable(onClick = onDeleteClicked)
                )
            }
        }
    }
}

@Composable
private fun DeleteCardConfirmDialog(
    errorMessage: String?,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.wallet_delete_card_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = stringResource(R.string.wallet_delete_card_dialog_message))
                errorMessage?.let { error ->
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
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = stringResource(R.string.wallet_delete_card_confirm_action),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.wallet_delete_card_cancel_action))
            }
        }
    )
}

@Composable
private fun TransactionRow(transaction: WalletTransaction) {
    val isCredit = transaction.type != WalletTransactionType.RENTAL_PAYMENT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = if (isCredit) {
                        RenCarSuccessColor.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (isCredit) R.drawable.ic_add else R.drawable.ic_rencar_car),
                contentDescription = null,
                tint = if (isCredit) RenCarSuccessColor else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Text(
            text = "${if (isCredit) "+" else ""}${transaction.amount.toTryPrice()}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = if (isCredit) RenCarSuccessColor else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun TopUpDialog(state: WalletState, onIntent: (WalletIntent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onIntent(WalletIntent.TopUpDismissed) },
        title = { Text(text = stringResource(R.string.wallet_topup_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.topUpAmountInput,
                    onValueChange = { onIntent(WalletIntent.TopUpAmountChanged(it)) },
                    label = { Text(text = stringResource(R.string.wallet_topup_amount_label)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                state.topUpErrorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onIntent(WalletIntent.TopUpConfirmClicked) },
                enabled = !state.isTopUpSubmitting
            ) {
                if (state.isTopUpSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(R.string.wallet_topup_confirm_action), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(WalletIntent.TopUpDismissed) }) {
                Text(text = stringResource(R.string.wallet_topup_cancel_action))
            }
        }
    )
}

@Composable
private fun AddCardDialog(state: WalletState, onIntent: (WalletIntent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onIntent(WalletIntent.AddCardDismissed) },
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
                            modifier = Modifier.clickable { onIntent(WalletIntent.AddCardBrandChanged(brand)) }
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
                    onValueChange = { onIntent(WalletIntent.AddCardLast4Changed(it)) },
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
                        onValueChange = { onIntent(WalletIntent.AddCardExpMonthChanged(it)) },
                        label = { Text(text = stringResource(R.string.wallet_add_card_exp_month_label)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.addCardExpYearInput,
                        onValueChange = { onIntent(WalletIntent.AddCardExpYearChanged(it)) },
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
                onClick = { onIntent(WalletIntent.AddCardConfirmClicked) },
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
            TextButton(onClick = { onIntent(WalletIntent.AddCardDismissed) }) {
                Text(text = stringResource(R.string.wallet_add_card_cancel_action))
            }
        }
    )
}

private val RenCarSuccessColor = androidx.compose.ui.graphics.Color(0xFF1A9E63)

private fun Double.toTryPrice(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return "₺${formatter.format(this)}"
}