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

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
/**
 * Provides the REST Endpoints of the Dataland EDC service
 */
interface DatalandEDCApi {
    @GET
    @Path("health")
    /**
     * Endpoint to probe if the service is running.
     */
    fun checkHealth(): String

    @POST
    @Path("dataland/data")
    /**
     * Endpoint to trigger the upload of the delivered data to the trustee
     * @param data in a string format
     */
    fun insertData(data: String): String

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("dataland/provideAsset/{providerAssetId}")
    /**
     * Endpoint returning the data associated to the provided ID, data are deleted on Dataland EDC after pick-up
     * @param providerAssetId ID used on Dataland EDC side to identify the data
     */
    fun provideAsset(@PathParam("providerAssetId") providerAssetId: String): String

    @GET
    @Path("dataland/data/{dataId}")
    /**
     * Endpoint for the Dataland backend to retrieve data from the trustee
     * @param dataId identifier containing the required information to retrieve data from the trustee
     */
    fun selectDataById(@PathParam("dataId") dataId: String): String

    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("dataland/receiveAsset/{assetId}")
    /**
     * Endpoint to receive data delivered by the trustee
     * @param assetId the identifier for the asset used on trustee side
     * @param data the data coming from the trustee in a byte array format
     */
    fun storeReceivedData(@PathParam("assetId") assetId: String, data: ByteArray): Response
}
