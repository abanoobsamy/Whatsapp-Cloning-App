package com.example.whatsappcloning.ui.signUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappcloning.ui.MainActivity;
import com.example.whatsappcloning.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference dataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = binding.edNameReg.getText().toString();
                String email = binding.edEmailReg.getText().toString();
                String password = binding.edPasswordReg.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

                    Toast.makeText(RegisterActivity.this, "Fill All Fields!", Toast.LENGTH_SHORT).show();
                }
                else {

                    signUp(name, email, password);
                }
            }
        });


    }//Main

    private void signUp(String userName, String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();
                            String userId = user.getUid();

                            dataReference = FirebaseDatabase.getInstance().getReference("myUsers")
                                    .child(userId);

                            HashMap<String, String> hashMap = new HashMap<>();

                            hashMap.put("id", userId);
                            hashMap.put("userName", userName);
                            hashMap.put("imageUrl", "default");
                            hashMap.put("status", "offline");

                            dataReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent(RegisterActivity.this,
                                                MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                        Toast.makeText(RegisterActivity.this, "Sign Up Successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {

                            Toast.makeText(RegisterActivity.this,
                                    "Invalid Email Or Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}