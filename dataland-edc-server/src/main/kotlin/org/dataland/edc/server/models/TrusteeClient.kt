package org.dataland.edc.server.models

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.eurodat.broker.model.ProviderRequest
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val REGISTER_ASSET_PAHT = "/asset/register"
private const val HTTP_TIMEOUT: Long = 30

/**
 * An HTTP client to communicate with the EuroDaT Broker
 */
class TrusteeClient(
    private val baseURL: String,
    private val credentials: String
) {
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .writeTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
            .callTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
            .build()

    private val toJsonMapper = ObjectMapper()

    private fun post(endpoint: String, body: String? = null): JsonNode {
        val request = Request.Builder().post(
            body?.toRequestBody("application/json".toMediaType()) ?: EMPTY_REQUEST
        )
            .url((baseURL + endpoint).toHttpUrl().newBuilder().build())
        request.addHeader("X-Api-Key", credentials)
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

    /**
     * registers an asset by sending an REST request to the EuroDaT broker
     * returns the response of the Broker as JsonNode
     */
    fun registerAsset(providerRequest: ProviderRequest): JsonNode {
        val providerRequestString = toJsonMapper.writeValueAsString(providerRequest)
        return post(REGISTER_ASSET_PAHT, providerRequestString)
    }
}
