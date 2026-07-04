package com.turkcell.rencar.data.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTokenHolder @Inject constructor() {

    @Volatile
    var accessToken: String? = null
        private set

    fun update(accessToken: String) {
        this.accessToken = accessToken
    }

    fun clear() {
        accessToken = null
    }
}
