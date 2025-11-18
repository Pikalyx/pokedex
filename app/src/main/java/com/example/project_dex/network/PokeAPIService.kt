package com.example.project_dex.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This is all we need in this file now.
// The structure of our data must match the JSON response.
@Serializable
data class PagedResponse(
    @SerialName("results") val results: List<ApiResource>
)

@Serializable
data class ApiResource(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)
