package com.turkcell.rencar.data.session

import android.content.Context
import com.turkcell.rencar.domain.wallet.CardPaymentTransactionStore
import com.turkcell.rencar.domain.wallet.WalletTransaction
import com.turkcell.rencar.domain.wallet.WalletTransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesCardPaymentTransactionStore @Inject constructor(
    @ApplicationContext context: Context
) : CardPaymentTransactionStore {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getTransactions(): List<WalletTransaction> {
        val serializedTransactions = preferences.getString(TRANSACTIONS_KEY, null)
            ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(serializedTransactions)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    jsonArray.optJSONObject(index)?.toTransactionOrNull()?.let(::add)
                }
            }
        }.getOrDefault(emptyList())
    }

    override fun saveTransaction(transaction: WalletTransaction) {
        val transactions = (listOf(transaction) + getTransactions())
            .distinctBy { it.deduplicationKey() }
            .sortedByDescending(WalletTransaction::createdAt)
            .take(MAX_STORED_TRANSACTIONS)

        val jsonArray = JSONArray().apply {
            transactions.forEach { put(it.toJson()) }
        }
        preferences.edit().putString(TRANSACTIONS_KEY, jsonArray.toString()).apply()
    }

    private fun JSONObject.toTransactionOrNull(): WalletTransaction? {
        val id = optString(ID_FIELD).takeIf(String::isNotBlank) ?: return null
        val type = runCatching {
            WalletTransactionType.valueOf(optString(TYPE_FIELD))
        }.getOrNull() ?: return null
        val description = optString(DESCRIPTION_FIELD).takeIf(String::isNotBlank) ?: return null
        val createdAt = optString(CREATED_AT_FIELD).takeIf(String::isNotBlank) ?: return null
        if (!has(AMOUNT_FIELD)) return null

        return WalletTransaction(
            id = id,
            type = type,
            amount = optDouble(AMOUNT_FIELD),
            rentalId = if (isNull(RENTAL_ID_FIELD)) {
                null
            } else {
                optString(RENTAL_ID_FIELD).takeIf(String::isNotBlank)
            },
            description = description,
            createdAt = createdAt
        )
    }

    private fun WalletTransaction.toJson(): JSONObject = JSONObject().apply {
        put(ID_FIELD, id)
        put(TYPE_FIELD, type.name)
        put(AMOUNT_FIELD, amount)
        put(RENTAL_ID_FIELD, rentalId ?: JSONObject.NULL)
        put(DESCRIPTION_FIELD, description)
        put(CREATED_AT_FIELD, createdAt)
    }

    private fun WalletTransaction.deduplicationKey(): String =
        rentalId?.let { "rental:$it" } ?: "transaction:$id"

    private companion object {
        const val PREFERENCES_NAME = "card_payment_transaction_store"
        const val TRANSACTIONS_KEY = "transactions"
        const val MAX_STORED_TRANSACTIONS = 20
        const val ID_FIELD = "id"
        const val TYPE_FIELD = "type"
        const val AMOUNT_FIELD = "amount"
        const val RENTAL_ID_FIELD = "rentalId"
        const val DESCRIPTION_FIELD = "description"
        const val CREATED_AT_FIELD = "createdAt"
    }
}
