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
import com.csmsscollege.csmsspoly.Teacher.Model.ParentChatModel;

import java.util.List;

public class ParentChatAdapter extends RecyclerView.Adapter<ParentChatAdapter.ViewHolder> {

    private Context context;
    private List<ParentChatModel> parentList;

    public ParentChatAdapter(Context context, List<ParentChatModel> parentList) {
        this.context = context;
        this.parentList = parentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParentChatModel parentChat = parentList.get(position);
        holder.tvParentEmail.setText("Message from: " + parentChat.getParentEmail());

        holder.btnReplyNow.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatWithTeacher.class);
            intent.putExtra("parentEmail", parentChat.getParentEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return parentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvParentEmail;
        Button btnReplyNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParentEmail = itemView.findViewById(R.id.tv_parent_email);
            btnReplyNow = itemView.findViewById(R.id.btn_reply_now);
        }
    }
}

