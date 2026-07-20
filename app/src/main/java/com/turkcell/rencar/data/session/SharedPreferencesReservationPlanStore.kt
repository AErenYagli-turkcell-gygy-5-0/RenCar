package com.turkcell.rencar.data.session

import android.content.Context
import androidx.core.content.edit
import com.turkcell.rencar.domain.rental.RentalPlan
import com.turkcell.rencar.domain.reservation.ReservationPlanStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesReservationPlanStore @Inject constructor(
    @ApplicationContext context: Context
) : ReservationPlanStore {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun savePlan(vehicleId: String, plan: RentalPlan) {
        if (vehicleId.isBlank()) return
        preferences.edit { putString(vehicleId.toPlanKey(), plan.name) }
    }

    override fun getPlan(vehicleId: String): RentalPlan? {
        if (vehicleId.isBlank()) return null
        return preferences.getString(vehicleId.toPlanKey(), null)
            ?.let { value -> runCatching { RentalPlan.valueOf(value) }.getOrNull() }
    }

    override fun clearPlan(vehicleId: String) {
        if (vehicleId.isBlank()) return
        preferences.edit { remove(vehicleId.toPlanKey()) }
    }

    private fun String.toPlanKey(): String = "$PLAN_KEY_PREFIX$this"

    private companion object {
        const val PREFERENCES_NAME = "reservation_plan_store"
        const val PLAN_KEY_PREFIX = "plan_"
    }
}
