package com.csmsscollege.csmsspoly.Student.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Student.Model.Assignment;
import com.csmsscollege.csmsspoly.Student.SubmitAssignments;
import com.google.firebase.database.*;

import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {
    private Context context;
    private List<Assignment> assignmentList;
    private String studentEmail;

    public AssignmentAdapter(Context context, List<Assignment> assignmentList, String studentEmail) {
        this.context = context;
        this.assignmentList = assignmentList;
        this.studentEmail = studentEmail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignmentList.get(position);
        holder.tvDescription.setText(assignment.getDescription());

        holder.btnViewPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(assignment.getPdfUrl()));
            context.startActivity(intent);
        });

        // **Check if student has already submitted this assignment**
        DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly/Answers");
        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean alreadySubmitted = false;

                for (DataSnapshot answerSnapshot : snapshot.getChildren()) {
                    String submittedEmail = answerSnapshot.child("studentEmail").getValue(String.class);
                    String submittedDescription = answerSnapshot.child("assignmentDescription").getValue(String.class);

                    if (submittedEmail != null && submittedDescription != null &&
                            submittedEmail.equals(studentEmail) && submittedDescription.equals(assignment.getDescription())) {
                        alreadySubmitted = true;
                        break;
                    }
                }

                if (alreadySubmitted) {
                    holder.btnSubmitAnswer.setText("Already Submitted");
                    holder.btnSubmitAnswer.setEnabled(false);
                    holder.btnSubmitAnswer.setBackgroundColor(Color.GRAY);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.btnSubmitAnswer.setOnClickListener(v -> ((SubmitAssignments) context).showSubmitDialog(assignment.getId(),assignment.getDescription()));
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvDescription;
        Button btnViewPdf, btnSubmitAnswer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnViewPdf = itemView.findViewById(R.id.btnViewPDF);
            btnSubmitAnswer = itemView.findViewById(R.id.btnSubmitAnswer);
        }
    }
}
