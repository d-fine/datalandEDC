package org.dataland.edc.server.models

import okhttp3.OkHttpClient

interface DALAOkHttpClientFactory {
    fun create(isSecure: Boolean = true): OkHttpClient
}
