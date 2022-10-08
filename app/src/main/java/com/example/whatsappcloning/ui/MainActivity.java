package com.example.whatsappcloning.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.whatsappcloning.adapter.ViewPagerAdapter;
import com.example.whatsappcloning.databinding.ActivityMainBinding;

import com.example.whatsappcloning.R;
import com.example.whatsappcloning.model.Users;
import com.example.whatsappcloning.ui.fragments.ChatsFragment;
import com.example.whatsappcloning.ui.fragments.ProfileFragment;
import com.example.whatsappcloning.ui.fragments.UsersFragment;
import com.example.whatsappcloning.ui.signUser.LoginActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFragment();

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        dataReference = FirebaseDatabase.getInstance().getReference("myUsers").child(user.getUid());

        dataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setFragment() {

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);

        pagerAdapter.addFragment(new ChatsFragment(), "Chats");
        pagerAdapter.addFragment(new UsersFragment(), "Users");
        pagerAdapter.addFragment(new ProfileFragment(), "Profile");

        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                tab.setText(pagerAdapter.getTitles().get(position));
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:

                auth.signOut();

                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                return true;
        }

        return false;
    }

    private void checkStatus(String status) {

        dataReference = FirebaseDatabase.getInstance().getReference("myUsers").child(user.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        dataReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        checkStatus("offline");
    }
}