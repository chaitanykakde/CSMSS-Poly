package com.csmsscollege.csmsspoly.Teacher.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.csmsscollege.csmsspoly.R
import com.csmsscollege.csmsspoly.Teacher.Model.StudentAttendance

class AttendanceReportAdapter(private val studentList: List<StudentAttendance>) :
    RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvEnrollment: TextView = itemView.findViewById(R.id.tvEnrollmentNumber)
        val tvPresent: TextView = itemView.findViewById(R.id.tvTotalPresent)
        val tvLectures: TextView = itemView.findViewById(R.id.tvTotalLectures)
        val tvPercentage: TextView = itemView.findViewById(R.id.tvAttendancePercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        holder.tvName.text = student.name
        holder.tvEnrollment.text = student.enrollmentNumber
        holder.tvPresent.text = student.presentCount.toString()
        holder.tvLectures.text = student.totalLectures.toString()
        holder.tvPercentage.text = "${student.getAttendancePercentage()}%"
    }

    override fun getItemCount(): Int = studentList.size
}
