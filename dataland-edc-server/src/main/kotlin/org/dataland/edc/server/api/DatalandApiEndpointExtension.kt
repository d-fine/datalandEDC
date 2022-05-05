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

import org.dataland.edc.server.controller.DatalandController
import org.dataland.edc.server.service.DataManager
import org.eclipse.dataspaceconnector.dataloading.AssetLoader
import org.eclipse.dataspaceconnector.spi.WebService
import org.eclipse.dataspaceconnector.spi.contract.negotiation.ConsumerContractNegotiationManager
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore
import org.eclipse.dataspaceconnector.spi.contract.offer.store.ContractDefinitionStore
import org.eclipse.dataspaceconnector.spi.system.Inject
import org.eclipse.dataspaceconnector.spi.system.Requires
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager

/**
 * Extends the EDC with the functionality required for Dataland
 */
@Requires(WebService::class)
class DatalandApiEndpointExtension : ServiceExtension {

    @Inject
    val assetLoader: AssetLoader? = null

    @Inject
    val contractDefinitionStore: ContractDefinitionStore? = null

    @Inject
    private val transferProcessManager: TransferProcessManager? = null

    @Inject
    private val contractNegotiationStore: ContractNegotiationStore? = null

    @Inject
    private val consumerContractNegotiationManager: ConsumerContractNegotiationManager? = null

    @Inject
    val webService: WebService? = null

    override fun name(): String {
        return "API Endpoint"
    }

    override fun initialize(context: ServiceExtensionContext) {
        val dataManager = DataManager(
            assetLoader!!,
            contractDefinitionStore!!,
            transferProcessManager!!,
            contractNegotiationStore!!,
            consumerContractNegotiationManager!!,
            context
        )
        webService!!.registerResource(DatalandController(dataManager, context))
    }
}
