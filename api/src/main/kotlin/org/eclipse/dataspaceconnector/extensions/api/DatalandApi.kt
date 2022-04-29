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

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.dataspaceconnector.extensions.controller.DatalandController
import org.eclipse.dataspaceconnector.spi.monitor.Monitor

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
class DatalandApi(
    val monitor: Monitor,
    val consumerApiController: ConsumerApiController,
    val datalandController: DatalandController
) {

    @GET
    @Path("health")
    fun checkHealth(): String {
        monitor.info("%s :: Received a health request")
        return "{\"response\":\"I'm alive!\"}"
    }

    @GET
    @Path("dataland/data/{dataId}/{contractDefinitionId}")
    fun selectDataById(
        @PathParam("dataId") dataId: String,
        @PathParam("contractDefinitionId") contractDefinitionId: String
    ): String {
        return datalandController.getAsset(assetId = dataId, contractDefinitionId = contractDefinitionId)
    }

    @POST
    @Path("dataland/data")
    fun insertData(data: String?): String {
        val providerRequest = datalandController.buildProviderRequest()
        val mapOfAssetIdAndContractDefinitionId = datalandController.registerAsset(providerRequest)
        return "{\"assetId\":\"${mapOfAssetIdAndContractDefinitionId["assetId"]}\"" + System.lineSeparator() +
                "\"contractDefinitionId\":\"${mapOfAssetIdAndContractDefinitionId["contractDefinitionId"]}\"}"
    }
}
