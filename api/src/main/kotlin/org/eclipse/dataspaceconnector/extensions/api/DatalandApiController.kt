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
import org.eclipse.dataspaceconnector.policy.model.Action
import org.eclipse.dataspaceconnector.policy.model.Permission
import org.eclipse.dataspaceconnector.policy.model.Policy
import org.eclipse.dataspaceconnector.spi.monitor.Monitor
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset
import org.eurodat.broker.model.ProviderRequest
import java.net.URI

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eurodat.broker.common.service.DefaultOkHttpClientFactoryImpl
import org.eurodat.broker.common.service.HttpClient



@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
class DatalandApiController(val monitor: Monitor, val consumerApiController: ConsumerApiController) {

    private val jsonMapper = jacksonObjectMapper()
    //private val trusteeURL = "$trusteeHost:$trusteePort$trusteePrefix"
    private val trusteeURL = "http://20.31.200.61"
    //private val providerIdsURL = "$consumerHost:$consumerIdsPort$consumerPrefix"
    private val providerIdsURL = "localhost:9292/api/v1/ids"
    private val testCredentials = "password"
    private val trusteeClient = HttpClient(
        DefaultOkHttpClientFactoryImpl.create(false), trusteeURL, "APIKey", testCredentials)
    /*private val consumerClient = HttpClient(
        DefaultOkHttpClientFactoryImpl.create(false), consumerURL, "APIKey", testCredentials)*/


    @GET
    @Path("health")
    fun checkHealth(): String {
        monitor.info("%s :: Received a health request")
        return "{\"response\":\"I'm alive!\"}"
    }

    @GET
    @Path("dataland/data/{dataId}")
    fun selectDataById(@PathParam("dataId") dataId: String?): String {
        return "todo"
    }



    @POST
    @Path("dataland/data")
    fun insertData(data: String?): String {

        val action = Action.Builder.newInstance()
            .type("USE")
            .build()
        val permission = Permission.Builder.newInstance()
            .target("test-asset")
            .action(action)
            .build()
        val asset = Asset.Builder.newInstance()
            .id("test-asset")
            .property("endpoint", "https://filesamples.com/samples/code/json/sample2.json")
            .build()
        val policy = Policy.Builder.newInstance()
            .id("956e172f-2de1-4501-8881-057a57fd0e60")
            .permission(permission)
            .build()

        val providerRequest = ProviderRequest(
            "eurodat-connector-test",
            "$providerIdsURL/api/v1/ids/data",
            "owner-ID",
            "persistent",
            asset,
            policy,
            URI("urn:connector:provider"),
            URI("urn:connector:consumer")
        )
        val providerRequestString = jsonMapper.writeValueAsString(providerRequest)

        val assetResponse = trusteeClient.post("/api/asset/register", providerRequestString)
        val assetId = assetResponse["asset"]["properties"]["asset:prop:id"].asText()
        val contractDefinitionId = assetResponse["contractDefinition"]["id"].asText()

        return assetId
    }
}
