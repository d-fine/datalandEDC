package org.eclipse.dataspaceconnector.extensions.models

import okhttp3.OkHttpClient

interface DALAOkHttpClientFactory {
    fun create(isSecure: Boolean = true): OkHttpClient
}