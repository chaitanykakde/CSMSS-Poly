package com.csmsscollege.csmsspoly.Teacher.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.csmsscollege.csmsspoly.R
import com.csmsscollege.csmsspoly.Teacher.Model.Student

class StudentAdapter(private val studentList: List<Student>) :
    RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val txtEnrollmentNumber: TextView = itemView.findViewById(R.id.tvEnrollmentNumber)
        val chkPresent: CheckBox = itemView.findViewById(R.id.cbPresent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        holder.txtStudentName.text = student.name
        holder.txtEnrollmentNumber.text = student.enrollmentNumber
        holder.chkPresent.setOnCheckedChangeListener { _, isChecked ->
            student.isPresent = isChecked
        }
    }

    override fun getItemCount() = studentList.size
}
