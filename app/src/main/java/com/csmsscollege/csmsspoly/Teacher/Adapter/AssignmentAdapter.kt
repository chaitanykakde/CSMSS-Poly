package com.csmsscollege.csmsspoly.Teacher.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.csmsscollege.csmsspoly.databinding.ItemAssignmentBinding

// Data Model for Assignment
data class Assignment(
    val year: String = "",
    val department: String = "",
    val description: String = "",
    val pdfUrl: String = ""
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "")
}



// Adapter Class
class AssignmentAdapter(
    private val context: Context,
    private val assignmentList: MutableList<Assignment>,
    private val onViewSubmissions: (Assignment) -> Unit // Click listener for View Submissions button
) : RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAssignmentBinding.inflate(inflater, parent, false)
        return AssignmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = assignmentList[position]
        holder.bind(assignment)
    }

    override fun getItemCount(): Int = assignmentList.size

    // ViewHolder Class
    inner class AssignmentViewHolder(private val binding: ItemAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(assignment: Assignment) {
            binding.tvYear.text = "Year: ${assignment.year}"
            binding.tvDepartment.text = "Department: ${assignment.department}"
            binding.tvDescription.text = assignment.description

            // View Submissions Button
            binding.btnViewSubmissions.setOnClickListener {
                onViewSubmissions(assignment)
            }
        }
    }
}
