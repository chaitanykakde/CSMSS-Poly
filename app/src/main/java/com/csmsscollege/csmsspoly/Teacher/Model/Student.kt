package com.csmsscollege.csmsspoly.Teacher.Model

data class Student(
    val enrollmentNumber: String,
    val name: String,
    val parentEmail: String,
    var isPresent: Boolean
)
