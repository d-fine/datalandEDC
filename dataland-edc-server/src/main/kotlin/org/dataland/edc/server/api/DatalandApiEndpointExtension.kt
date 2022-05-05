/*
 * This content is based on copyrighted work as referenced below.
 *
 * Changes made:
 * - removed unneeded parts
 */
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

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.dataland.edc.server.controller.DatalandController
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.extensions.api.ConsumerApiController
import org.eclipse.dataspaceconnector.spi.WebService
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.system.Requires
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext

@OpenAPIDefinition(info = Info(title = "Dataland EDC OpenAPI Spec", version = "1.0.0-SNAPSHOT"))
/**
 * Extends the EDC with the functionality required for Dataland
 */
@Requires(WebService::class, ConsumerApiController::class)
class DatalandApiEndpointExtension : ServiceExtension {
    override fun name(): String {
        return "API Endpoint"
    }

    override fun initialize(context: ServiceExtensionContext) {
        val webService: WebService = context.getService(WebService::class.java)
        val assetLoader = context.getService(AssetLoader::class.java)
        val contractDefinitionStore = context.getService(ContractDefinitionStore::class.java)
        val dataManager = DataManager(assetLoader, contractDefinitionStore, context)
        webService.registerResource(DatalandController(dataManager, context))
    }
}
