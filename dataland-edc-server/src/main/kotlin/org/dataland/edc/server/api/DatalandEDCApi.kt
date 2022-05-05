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
interface DatalandEDCApi {
    @GET
    @Path("health")
    fun checkHealth(): String

    @POST
    @Path("dataland/data")
    fun insertData(data: String): String

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("dataland/provideAsset/{assetId}")
    fun provideAsset(@PathParam("assetId") providerAssetId: String): String

    @GET
    @Path("dataland/data/{dataId}")
    fun selectDataById(@PathParam("dataId") dataId: String): String

    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("dataland/receiveAsset/{id}")
    fun storeReceivedData(@PathParam("id") assetId: String, data: ByteArray): Response
}
