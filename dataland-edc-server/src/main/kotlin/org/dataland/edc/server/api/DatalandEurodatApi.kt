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

@Path("/dataland/eurodat")
/**
 * Provides the REST Endpoints of the Dataland EDC service towards EuroDaT
 */
interface DatalandEurodatApi {
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/asset/{datalandAssetId}")
    /**
     * Endpoint returning the data associated to the provided ID, data are deleted on Dataland EDC after pick-up
     * @param datalandAssetId ID used on Dataland EDC side to identify the data
     */
    fun provideAsset(@PathParam("datalandAssetId") datalandAssetId: String): String

    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/asset/{eurodatAssetId}")
    /**
     * Endpoint to receive data delivered by the trustee
     * @param eurodatAssetId the identifier for the asset used on trustee side
     * @param data the data coming from the trustee in a byte array format
     */
    fun storeReceivedAsset(@PathParam("eurodatAssetId") eurodatAssetId: String, data: ByteArray): Response
}
