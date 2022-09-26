package org.dataland.edc.server.api

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.dataland.edc.server.models.CheckHealthResponse
import org.dataland.edc.server.models.InsertDataResponse

@OpenAPIDefinition(info = Info(title = "Dataland EDC OpenAPI Spec", version = "un-versioned"))
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/dataland")
/**
 * Provides the REST Endpoints of the Dataland EDC service towards the internal Dataland Backend
 */
interface DatalandInternalEdcApi {
    @GET
    @Path("health")
    /**
     * Endpoint to probe if the service is running.
     */
    fun checkHealth(): CheckHealthResponse

    @POST
    @Path("/data")
    /**
     * Endpoint to trigger the upload of the delivered data to the trustee
     * @param data in a string format
     */
    fun insertData(data: String, @QueryParam("correlationId") correlationId: String): InsertDataResponse

    @GET
    @Path("/data/{dataId}")
    /**
     * Endpoint for the Dataland backend to retrieve data from the trustee
     * @param dataId identifier containing the required information to retrieve data from the trustee
     */
    fun selectDataById(@PathParam("dataId") dataId: String, @QueryParam("correlationId") correlationId: String): String
}
