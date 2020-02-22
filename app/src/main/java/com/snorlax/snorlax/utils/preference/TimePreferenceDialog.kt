package com.snorlax.snorlax.utils.preference

import android.app.TimePickerDialog
import android.content.Context
import com.snorlax.snorlax.model.LateData
import com.snorlax.snorlax.utils.TimeUtils.secondsToHour
import com.snorlax.snorlax.utils.TimeUtils.secondsToMinute

class TimePreferenceDialog(
    context: Context,
    listener: OnTimeSetListener?,
    lateData: LateData
) : TimePickerDialog(
    context,
    listener,
    secondsToHour(lateData.late_time),
    secondsToMinute(lateData.late_time),
    false
)