package com.fullChat.app.models

data class NotificationModel(
    val canonical_ids: Int,
    val failure: Int,
    val multicast_id: Long,
    val results: List<Result>,
    val success: Int
)

data class Result(
    val message_id: String
)