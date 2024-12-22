package com.onthecrow.sharegram.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstagramPost(
    val status: String,
    val data: InstagramPostData,
)

@Serializable
data class InstagramPostData(
    @SerialName("xdt_shortcode_media") val xdtShortcodeMedia: ShortcodeMedia,
)

@Serializable
data class ShortcodeMedia(
    val id: String,
    @SerialName("__typename") val typeName: String, // XDTGraphVideo or XDTGraphImage
    @SerialName("shortcode") val shortCode: String,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("display_url") val displayUrl: String? = null, // for XDTGraphImage type
)