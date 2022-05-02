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
package org.eclipse.dataspaceconnector.extensions.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.dataspaceconnector.extensions.controller.DatalandController
import org.eclipse.dataspaceconnector.spi.monitor.Monitor

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
class DatalandApi(
    val monitor: Monitor,
    val objectMapper: ObjectMapper,
    val consumerApiController: ConsumerApiController,
    val datalandController: DatalandController
) {

    @GET
    @Path("health")
    fun checkHealth(): String {
        monitor.info("%s :: Received a health request")
        val response = mapOf("response" to "I'm alive")
        return objectMapper.writeValueAsString(response)
    }

    @POST
    @Path("dataland/data")
    fun insertData(data: String): String {
        monitor.info("%s :: Received a POST request to register asset")
        val providerRequest = datalandController.buildProviderRequest(data)
        monitor.info("%s :: ProviderRequest was built")
        val mapOfAssetIdAndContractDefinitionId = datalandController.registerAsset(providerRequest)
        return mapOfAssetIdAndContractDefinitionId.values.joinToString(":")
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("dataland/upload/{assetId}")
    fun uploadData(
        @PathParam("assetId") assetId: String
    ): String {
        return datalandController.provideData(assetId)
    }

    @GET
    @Path("dataland/data/{dataId}")
    fun selectDataById(
        @PathParam("dataId") dataId: String,
    ): String {
        val splitDataId = dataId.split(":")
        if (splitDataId.size != 2) throw IllegalArgumentException("The data ID $dataId has an invalid format.")
        monitor.info("%s :: Getting asset with asset ID ${splitDataId[0]} and contract definition ID ${splitDataId[1]}")
        return datalandController.getAsset(
            assetId = splitDataId[0],
            contractDefinitionId = splitDataId[1]
        )
    }

    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("dataland/transferdestination/{id}")
    fun saveTransferedData(@PathParam("id") id: String, data: ByteArray): Response {
        val x: Map<String, String> = objectMapper.readValue(data.decodeToString())
        datalandController.storeAsset(id, x["content"]!!)
        return Response.ok("I received data with asset ID $id").build()
    }
}
