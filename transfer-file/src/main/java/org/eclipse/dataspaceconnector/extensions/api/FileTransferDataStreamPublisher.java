/*
 * This content is based on copyrighted work as referenced below.
 *
 * Changes made:
 * - simplified method
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

package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.asset.DataAddressResolver;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.transfer.inline.DataStreamPublisher;
import org.eclipse.dataspaceconnector.spi.transfer.inline.StreamContext;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;

public class FileTransferDataStreamPublisher implements DataStreamPublisher {
    private final Monitor monitor;
    private final DataAddressResolver dataAddressResolver;

    public FileTransferDataStreamPublisher(Monitor monitor, DataAddressResolver dataAddressResolver) {
        this.monitor = monitor;
        this.dataAddressResolver = dataAddressResolver;
    }

    @Override
    public void initialize(StreamContext context) {
    }

    @Override
    public boolean canHandle(DataRequest dataRequest) {
        return "file".equalsIgnoreCase(dataRequest.getDataDestination().getType());
    }

    @Override
    public Result<Void> notifyPublisher(DataRequest dataRequest) {
        var source = dataAddressResolver.resolveForAsset(dataRequest.getAssetId());
        var destination = dataRequest.getDataDestination();

        monitor.info("I should have copied a file");
        return Result.success();
    }
}
