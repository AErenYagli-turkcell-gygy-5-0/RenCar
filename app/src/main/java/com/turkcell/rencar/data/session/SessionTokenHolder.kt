package com.turkcell.rencar.data.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTokenHolder @Inject constructor() {

    @Volatile
    var accessToken: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    fun update(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
