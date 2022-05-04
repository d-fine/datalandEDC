package org.dataland.edc.server.models

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class DALAHttpClient(private val client: OkHttpClient, private val baseURL: String, private val auth: String = "Basic", credentials: String? = null) {
    private val toJsonMapper = ObjectMapper()
    private val hasCredentials = !credentials.isNullOrEmpty()
    private val credentials = if (hasCredentials)
        Base64.getEncoder().encodeToString(credentials?.toByteArray()) else ""

    fun get(endpoint: String, params: Map<String, String>? = null) =
        sendRequest(Request.Builder().url(buildURL(endpoint, params)))

    fun post(endpoint: String, body: String? = null, params: Map<String, String>? = null) = sendRequest(
        Request.Builder().post(
            body?.toRequestBody("application/json".toMediaType()) ?: EMPTY_REQUEST
        )
            .url(buildURL(endpoint, params))
    )

    private fun canConnectToContainer(endpoint: String): Boolean {
        return try {
            get(endpoint, null)
            true
        } catch (e: SSLHandshakeException) {
            println("Container not ready for SSL Handshake")
            false
        }
    }

    fun connectionCheck(endpoint: String, maxAttempts: Int = 10, waitMillis: Long = 3000) {
        println("Waiting for client at URL $baseURL to start up")
        for (attempt in 1..maxAttempts) {
            println("Connection attempt $attempt")
            if (canConnectToContainer(endpoint)) {
                println("Client at URL $baseURL ready")
                return
            }
            Thread.sleep(waitMillis)
        }
        throw TimeoutException("Client at URL $baseURL could not start properly")
    }

    private fun buildURL(endpoint: String, params: Map<String, String>?): HttpUrl {
        val httpBuilder = (baseURL + endpoint).toHttpUrl().newBuilder()
        params?.forEach { httpBuilder.addQueryParameter(it.key, it.value) }
        return httpBuilder.build()
    }

    private fun sendRequest(request: Request.Builder): JsonNode {
        request.addAuthCredentials(auth, credentials)
        client.newCall(request.build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body!!.string()
            val jsonBody = try {
                toJsonMapper.readTree(responseBody)
            } catch (e: JsonParseException) {
                toJsonMapper.readTree("""{"content": "$responseBody"}""")
            }
            println(
                """Sent '${response.request.method}' request to URL : ${response.request.url}
                       Response Code : ${response.code}
                       Response Body : $jsonBody"""
            )
            return jsonBody
        }
    }

    private fun Request.Builder.addAuthCredentials(auth: String, credentials: String): Request.Builder {
        if (!hasCredentials) return this
        return when (auth) {
            "Basic" -> this.addHeader("Authorization", "Basic $credentials")
            "APIKey" -> this.addHeader("X-Api-Key", "password")
            else -> this
        }
    }

    // TODO: fun getJson<Class> and postJson<Class> are necessary to ensure getting/receiving objects is easy
}
