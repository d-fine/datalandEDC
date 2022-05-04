package org.dataland.edc.server.models

import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object DALADefaultOkHttpClientFactoryImpl : DALAOkHttpClientFactory {
    override fun create(isSecure: Boolean): OkHttpClient = if (isSecure) {
        OkHttpClient.Builder()
    } else {
        println("An HttpClient was built in INSECURE mode")
        val trustManager: Array<X509TrustManager> = arrayOf(
            object : X509TrustManager {
                override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
                override fun checkClientTrusted(certificate: Array<X509Certificate?>?, str: String?) {}
                override fun checkServerTrusted(certificate: Array<X509Certificate?>?, str: String?) {}
            }
        )
        val context: SSLContext = SSLContext.getInstance("TLSv1.2")
        context.init(null, trustManager, SecureRandom())
        val allHostsValid = HostnameVerifier { _, _ -> true }

        OkHttpClient.Builder()
            .sslSocketFactory(context.socketFactory, trustManager[0])
            .hostnameVerifier(allHostsValid)
    }
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build()
}
