package com.turkcell.rencar.domain.reservation

import com.turkcell.rencar.domain.rental.RentalPlan

interface ReservationPlanStore {
    fun savePlan(vehicleId: String, plan: RentalPlan)
    fun getPlan(vehicleId: String): RentalPlan?
    fun clearPlan(vehicleId: String)
}
