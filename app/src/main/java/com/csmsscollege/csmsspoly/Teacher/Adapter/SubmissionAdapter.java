package com.csmsscollege.csmsspoly.Teacher.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Model.Submission;
import java.util.List;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder> {

    private Context context;
    private List<Submission> submissionList;

    public SubmissionAdapter(Context context, List<Submission> submissionList) {
        this.context = context;
        this.submissionList = submissionList;
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_submission, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        Submission submission = submissionList.get(position);
        holder.tvStudentName.setText("Name: " + submission.getStudentEnrollment());
        holder.tvStudentEmail.setText("Email: " + submission.getStudentEmail());
        holder.tvSubmissionTime.setText("Submitted on: " + submission.getSubmissionTime());

        // Open PDF on button click
        holder.btnViewSubmission.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(submission.getAnswerUrl()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    public static class SubmissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentEmail, tvSubmissionTime;
        Button btnViewSubmission;

        public SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvSubmissionTime = itemView.findViewById(R.id.tvSubmissionTime);
            btnViewSubmission = itemView.findViewById(R.id.btnViewSubmission);
        }
    }
}
