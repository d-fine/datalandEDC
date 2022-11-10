package org.dataland.edc.server.utils

/**
 * An object containing frequently used constants
 */
object Constants {
    const val TIMEOUT_MS = 120L * 1000L
    const val POLL_INTERVAL_MS = 50L
    const val DUMMY_STRING = "abc-123"

    const val ASSET_ID_ASSET_FOR_ASSET_MANAGEMENT = "asset-for-asset-management"
    const val ACTION_TYPE_USE = "USE"

    const val CONNECTOR_ID_PROVIDER = "provider"
    const val CONNECTOR_ID_CONSUMER = "consumer"
    const val PROTOCOL_IDS_MULTIPART = "ids-multipart"
    const val TYPE_HTTP_ASSET_REGISTRATION = "HttpAssetRegistration"
    const val PROVIDER_ID_DATALAND = "dataland-provider-id"
    const val OWNER_ID_DATALAND = "dataland-owner-id"
    const val CONTENT_TYPE_PERSISTENT = "persistent"
    const val POLICY_TEMPLATE_ID = "policy-template"
    const val TYPE_HTTP_FV = "HttpFV"
    const val APP_EXTRACT_ASSET = "EXTRACT_ASSET"
    const val INPUT_LIST_EMPTY = "[]"

    const val URN_KEY_PROVIDER = "urn:connector:provider"
    const val URN_KEY_CONSUMER = "urn:connector:consumer"

    const val DATA_TYPE_REGISTRATION = "DataTypeForRegisteringAsset"
    const val DATA_TYPE_RECEIVING = "DataTypeForReceivingAsset"
}
