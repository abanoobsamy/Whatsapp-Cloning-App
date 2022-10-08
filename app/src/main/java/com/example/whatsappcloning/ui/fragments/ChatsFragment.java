package com.example.whatsappcloning.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsappcloning.R;
import com.example.whatsappcloning.adapter.UsersAdapter;
import com.example.whatsappcloning.databinding.FragmentChatsBinding;
import com.example.whatsappcloning.model.ChatList;
import com.example.whatsappcloning.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding binding;

    private UsersAdapter usersAdapter;
    private ArrayList<Users> usersList;
    private ArrayList<ChatList> chatLists;

    private FirebaseUser fUser;
    private DatabaseReference reference;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(getLayoutInflater(), container, false);

        usersList = new ArrayList<>();
        chatLists = new ArrayList<>();

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        readChats();

        return binding.getRoot();
    }

    private void readChats() {

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(fUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatLists.clear();

                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        ChatList chatList = dataSnapshot.getValue(ChatList.class);

                        chatLists.add(chatList);
                    }

                    chatListsUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chatListsUsers() {

        reference = FirebaseDatabase.getInstance().getReference("myUsers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                usersList.clear();

                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        Users users = dataSnapshot.getValue(Users.class);

                        for (ChatList chatList : chatLists) {

                            if (users.getId().equals(chatList.getId())) {

                                usersList.add(users);
                            }
                        }

                        usersAdapter = new UsersAdapter(getContext(), usersList, true);
                        binding.rvChats.setAdapter(usersAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}