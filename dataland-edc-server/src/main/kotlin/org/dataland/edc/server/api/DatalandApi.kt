/*
 *  Copyright (c) 2021 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */
package org.dataland.edc.server.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.dataland.edc.server.controller.DatalandController
import org.eclipse.dataspaceconnector.spi.monitor.Monitor

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
class DatalandApi(
    private val monitor: Monitor,
    private val datalandController: DatalandController
) {

    private val objectMapper = jacksonObjectMapper()

    @GET
    @Path("health")
    fun checkHealth(): String {
        monitor.info("Received a health request")
        return objectMapper.writeValueAsString(mapOf("response" to "I am alive!"))
    }

    @POST
    @Path("dataland/data")
    fun insertData(data: String): String {
        monitor.info("Received a POST request to register asset to EuroDaT")
        return datalandController.uploadAssetToEuroDaT(data)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("dataland/provideAsset/{assetId}")
    fun provideAsset(
        @PathParam("assetId") providerAssetId: String
    ): String {
        monitor.info("Providing asset with provider asset ID $providerAssetId")
        return datalandController.getProvidedAsset(providerAssetId)
    }

    @GET
    @Path("dataland/data/{dataId}")
    fun selectDataById(
        @PathParam("dataId") dataId: String
    ): String {
        monitor.info("Getting asset with data ID $dataId")
        return datalandController.getAssetFromEuroDaT(dataId)
    }

    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("dataland/receiveAsset/{id}")
    fun storeReceivedData(@PathParam("id") assetId: String, data: ByteArray): Response {
        monitor.info("ReceiveAsset endpoint has received a POST request by EuroDaT with an asset to store")
        val x: Map<String, String> = objectMapper.readValue(data.decodeToString())
        datalandController.storeReceivedAsset(assetId, x["content"]!!)
        return Response.ok("Dataland-connector received asset with asset ID $assetId").build()
    }
}
