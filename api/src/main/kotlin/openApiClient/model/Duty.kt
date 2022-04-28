/**
 * EuroDat OpenAPI Spec
 *
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.0.1-SNAPSHOT
 * 
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package org.eurodat.brokerextension.openApiClient.model

import org.eurodat.brokerextension.openApiClient.model.Action
import org.eurodat.brokerextension.openApiClient.model.Permission

import com.squareup.moshi.Json

/**
 * 
 *
 * @param uid 
 * @param target 
 * @param action 
 * @param assignee 
 * @param assigner 
 * @param parentPermission 
 * @param consequence 
 */

data class Duty (

    @Json(name = "uid")
    val uid: kotlin.String? = null,

    @Json(name = "target")
    val target: kotlin.String? = null,

    @Json(name = "action")
    val action: Action? = null,

    @Json(name = "assignee")
    val assignee: kotlin.String? = null,

    @Json(name = "assigner")
    val assigner: kotlin.String? = null,

    @Json(name = "parentPermission")
    val parentPermission: Permission? = null,

    @Json(name = "consequence")
    val consequence: Duty? = null

)

