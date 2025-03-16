package com.csmsscollege.csmsspoly.Teacher.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.Parent.ChatWithTeacher;
import com.csmsscollege.csmsspoly.R;

import java.util.List;

public class ParentEmailAdapter extends RecyclerView.Adapter<ParentEmailAdapter.ViewHolder> {
    private List<String> parentEmails;
    private Context context;

    public ParentEmailAdapter(Context context, List<String> parentEmails) {
        this.context = context;
        this.parentEmails = parentEmails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_email, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String email = parentEmails.get(position);
        holder.tvParentEmail.setText(email);

        holder.btnChat.setOnClickListener(v -> {
            // Open ChatWithTeacher Activity and pass parentEmail
            Intent intent = new Intent(context, ChatWithTeacher.class);
            intent.putExtra("parentEmail", email);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return parentEmails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvParentEmail;
        Button btnChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParentEmail = itemView.findViewById(R.id.tvParentEmail);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}
