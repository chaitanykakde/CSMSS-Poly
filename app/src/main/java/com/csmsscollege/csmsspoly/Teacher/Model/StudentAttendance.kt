package com.csmsscollege.csmsspoly.Teacher.Model

data class StudentAttendance(
    val name: String = "",
    val enrollmentNumber: String = "",
    var presentCount: Int = 0,
    var totalLectures: Int = 0,
    var status:String=""
) {
    fun getAttendancePercentage(): Double {
        return if (totalLectures == 0) 0.0 else String.format("%.2f", (presentCount.toDouble() / totalLectures) * 100).toDouble()
    }

}
