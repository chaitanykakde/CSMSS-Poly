package com.csmsscollege.csmsspoly.Teacher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.Parent.ChatWithTeacher;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.ParentChatAdapter;
import com.csmsscollege.csmsspoly.Teacher.Adapter.ParentEmailAdapter;
import com.csmsscollege.csmsspoly.Teacher.Model.ParentChatModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ParentChats extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParentChatAdapter adapter;
    private List<ParentChatModel> parentChatList;
    private DatabaseReference chatsRef;

    private Button chatWithOtherParents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_chats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        parentChatList = new ArrayList<>();
        adapter = new ParentChatAdapter(this, parentChatList);
        recyclerView.setAdapter(adapter);

        chatWithOtherParents = findViewById(R.id.btnChatWithOtherParents);
        chatWithOtherParents.setOnClickListener(v ->
        {
            Intent intent = new Intent(ParentChats.this, ListOfParents.class);
            startActivity(intent);
        });


        // Firebase Database Reference
        chatsRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly").child("Chats");

        fetchParentChats();
    }




    // Function to extract parent emails from CSV






    private void fetchParentChats() {
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentChatList.clear();
                for (DataSnapshot parentSnapshot : snapshot.getChildren()) {
                    String encodedEmail = parentSnapshot.getKey();
                    if (encodedEmail != null) {
                        String parentEmail = encodedEmail.replace(",", ".");
                        parentChatList.add(new ParentChatModel(parentEmail));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching parent chats: " + error.getMessage());
            }
        });
    }
}
