package com.csmsscollege.csmsspoly.Parent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.R;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatWithTeacher extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatList;
    private DatabaseReference chatRef;
    private EditText etMessage;
    private ImageButton btnSend;
    private String parentEmail,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_teacher);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);

        // Get Parent Email from SharedPreferences


        Intent intent = getIntent();
        email = intent.getStringExtra("parentEmail"); // Correct method name

        if (email != null) {
            parentEmail = email;
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            parentEmail = sharedPreferences.getString("email", "default@example.com");

        }
// Get Parent Email from SharedPreferences (if not available in Intent)
        // Encode email for Firebase (Replace '.' with ',')
        String encodedEmail = parentEmail.replace(".", ",");


        // Firebase Reference
        chatRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly/Chats/" + encodedEmail);

        // Load previous chat messages
        loadChatMessages();

        // Send message
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadChatMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage message = ds.getValue(ChatMessage.class);
                    chatList.add(message);
                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatWithTeacher.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = chatRef.push().getKey();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String sender="Parent";
        if(email!=null){
            sender="Teacher";
        }
        ChatMessage chatMessage = new ChatMessage(messageText, timestamp, sender);

        if (messageId != null) {
            chatRef.child(messageId).setValue(chatMessage)
                    .addOnSuccessListener(unused -> etMessage.setText(""))
                    .addOnFailureListener(e -> Toast.makeText(ChatWithTeacher.this, "Failed to send", Toast.LENGTH_SHORT).show());
        }
    }
}
