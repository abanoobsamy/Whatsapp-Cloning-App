package com.example.whatsappcloning.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.whatsappcloning.R;
import com.example.whatsappcloning.adapter.UsersAdapter;
import com.example.whatsappcloning.databinding.FragmentUsersBinding;
import com.example.whatsappcloning.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dataReference;

    private UsersAdapter usersAdapter;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUsersBinding.inflate(getLayoutInflater(), container, false);

        ArrayList<Users> usersList = new ArrayList<>();

        usersAdapter = new UsersAdapter(getContext(), usersList, false);

        readUsers(usersList, usersAdapter);

        return binding.getRoot();
    }

    private void readUsers(ArrayList<Users> usersList, UsersAdapter usersAdapter) {

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        // here we don't set child() because we need other users
        // not current user which mean child.
        dataReference = FirebaseDatabase.getInstance().getReference("myUsers");

        dataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                usersList.clear();

                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {

                        Users users = dataSnapshot.getValue(Users.class);

                        assert users != null;

                        if (user.getUid() != null) {

                            if (!users.getId().equals(user.getUid())
                                    && users != null && users.getId() != null) {

                                usersList.add(users);
                            }
                        }

                        binding.rvUsers.setAdapter(usersAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}