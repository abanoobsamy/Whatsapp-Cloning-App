package com.example.whatsappcloning.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.whatsappcloning.R;
import com.example.whatsappcloning.adapter.MessagesAdapter;
import com.example.whatsappcloning.databinding.ActivityMessageBinding;
import com.example.whatsappcloning.model.Chats;
import com.example.whatsappcloning.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;

    private MessagesAdapter messagesAdapter;
    private ArrayList<Chats> chatsList;

    private Intent intent;

    //come from intent
    private String userId;

    private ValueEventListener seenListener;

    private Users users;
    private Chats chats;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent = getIntent();

        setToolbarAction();
        setToolbarUser();

         linearLayoutManager = new LinearLayoutManager(this);
        //because to see last message
        linearLayoutManager.setStackFromEnd(true);
        binding.rvMessage.setLayoutManager(linearLayoutManager);

        seenMessage(userId);
    }

    private void setToolbarAction() {

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.toolbar.getNavigationIcon()
                .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
    }

    private void setToolbarUser() {

        auth = FirebaseAuth.getInstance();

        // and this is current user.
        user = auth.getCurrentUser();

        //we choose that because we need second user to message not current user.
        userId = intent.getStringExtra("userId");

        reference = FirebaseDatabase.getInstance().getReference("myUsers").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    users = snapshot.getValue(Users.class);

                    if (users.getImageUrl().equals("default")) {

                        binding.ivUserImageMessage.setImageResource(R.drawable.user);
                    }
                    else {

                        Glide.with(MessageActivity.this)
                                .load(users.getImageUrl()).circleCrop()
                                .into(binding.ivUserImageMessage);
                    }

                    binding.tvUserNameMessage.setText(users.getUserName());

                    readMessage(user.getUid(), userId, users.getImageUrl());
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.buttonChatSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = binding.editChatMessage.getText().toString();

                if (!msg.isEmpty()) {

                    sendMessage(user.getUid(), userId, msg);
                }

                binding.editChatMessage.setText("");
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {

        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("seen", false);

        reference.child("Chats").push().setValue(hashMap);

        /**
         *
         * to send to {@link com.example.whatsappcloning.ui.fragments.ChatsFragment}
         * */

        //to send to chat fragment
        DatabaseReference chatReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(user.getUid())
                .child(userId);

        //for one operating
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {

                    chatReference.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage(String myId, String userId, String imageUrl) {

        chatsList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatsList.clear();

                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        chats = dataSnapshot.getValue(Chats.class);

                        assert chats != null;

                        if (chats.getReceiver().equals(myId) && chats.getSender().equals(userId)
                                || chats.getReceiver().equals(userId) && chats.getSender().equals(myId)) {

                            chatsList.add(chats);
                        }

                        messagesAdapter = new MessagesAdapter(MessageActivity.this, chatsList, imageUrl);
                        binding.rvMessage.setAdapter(messagesAdapter);
                    }

                    if (chats.getSender().equals(users.getId())) {

                        if (linearLayoutManager.isSmoothScrollbarEnabled()) {

                            sendNotification();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(String userId) {

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        Chats chats = dataSnapshot.getValue(Chats.class);

                        if (user.getUid() != null) {

                            if (chats.getReceiver().equals(user.getUid())
                                    && chats.getSender().equals(userId)) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("seen", true);
                                dataSnapshot.getRef().updateChildren(hashMap);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkStatus(String status) {

        reference = FirebaseDatabase.getInstance().getReference("myUsers").child(user.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        reference.removeEventListener(seenListener);
        checkStatus("offline");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

//        PendingIntent resultPendingIntent =
//                PendingIntent.getActivity(this,0, new Intent(), 0);


        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.user)
                .setTicker("Hearty365")
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentInfo("Info");

        notificationBuilder.setContentTitle(users.getUserName());
        notificationBuilder.setContentText(chats.getMessage());
//        notificationBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }
}