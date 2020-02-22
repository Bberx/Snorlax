package com.snorlax.snorlax.model

import com.google.firebase.Timestamp

data class LateData(
    val late_time: Long = -1,
    val who_uid: String? = null,
    val last_edit: Timestamp? = null
)