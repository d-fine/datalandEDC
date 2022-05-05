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
import java.io.IOException
import java.util.concurrent.TimeUnit

class DALAHttpClient(
    private val baseURL: String,
    credentials: String? = null
) {
    private val httpTimeOut: Long = 30
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .writeTimeout(httpTimeOut, TimeUnit.SECONDS)
            .readTimeout(httpTimeOut, TimeUnit.SECONDS)
            .connectTimeout(httpTimeOut, TimeUnit.SECONDS)
            .callTimeout(httpTimeOut, TimeUnit.SECONDS)
            .build()

    private val toJsonMapper = ObjectMapper()

    fun post(endpoint: String, body: String? = null): JsonNode {
        val request = Request.Builder().post(
            body?.toRequestBody("application/json".toMediaType()) ?: EMPTY_REQUEST
        )
            .url((baseURL + endpoint).toHttpUrl().newBuilder().build())
        //TODO: Sollte statt password hier "credentials" benutzt werden?
        request.addHeader("X-Api-Key", "password")
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

}
