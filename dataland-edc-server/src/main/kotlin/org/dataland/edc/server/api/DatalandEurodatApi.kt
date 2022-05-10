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
    @Path("/asset/{trusteeAssetId}")
    /**
     * Endpoint to receive data delivered by the trustee
     * @param trusteeAssetId the identifier for the asset used on trustee side
     * @param data the data coming from the trustee in a byte array format
     */
    fun storeReceivedAsset(@PathParam("trusteeAssetId") trusteeAssetId: String, data: ByteArray): Response
}
