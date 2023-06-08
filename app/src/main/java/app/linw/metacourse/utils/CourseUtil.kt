package app.linw.metacourse.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import app.linw.metacourse.data.TimeLabel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object CourseUtil {

    private fun parseTime(timeStr: String, pattern: String): LocalTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalTime.parse(timeStr, formatter)
    }
    private fun time2Str(time: LocalTime, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return time.format(formatter).toString()
    }

    /**
     * if a > b
     */
    fun compareStringTimes(a: String, b: String): Boolean {
        val time1 = parseTime(a, "HH:mm")
        val time2 = parseTime(b, "HH:mm")
        return time1 > time2
    }

    /**
     * 获取上一个时间区间后的时间区间
     */
    fun getTimeIntervalAfter(lastInterval: SnapshotStateMap<String, String>, interval: Int): SnapshotStateMap<String, String> {
        val timeSource = parseTime(lastInterval["end_time"].toString(),"HH:mm")
        val timeEnd = timeSource.plusMinutes(interval.toLong())
        return mutableStateMapOf("start_time" to time2Str(timeSource,"HH:mm") ,"end_time" to time2Str(timeEnd,"HH:mm"))
    }

    fun generateCourseLabel(
        timeData: SnapshotStateList<SnapshotStateMap<String, String>>,
        label: String
    ): MutableList<TimeLabel> {
        val timeLabels = mutableListOf<TimeLabel>()
        var i = 1
        timeData.forEach {
            timeLabels.add(TimeLabel(it["start_time"].toString(),it["end_time"].toString(),"$label-$i"))
            i++
        }
        return timeLabels
    }
}