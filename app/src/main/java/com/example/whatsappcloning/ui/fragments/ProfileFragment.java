package com.example.whatsappcloning.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappcloning.R;
import com.example.whatsappcloning.databinding.FragmentProfileBinding;
import com.example.whatsappcloning.model.Users;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

//    private static final int REQ_IMAGE = 1;

    private FragmentProfileBinding binding;

    private FirebaseUser fUser;
    private DatabaseReference reference;

    private StorageReference storageReference;

    private StorageTask uploadTask;

    private Uri imageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        setProfile();

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        binding.ivProfileFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectImage();
            }
        });

        return binding.getRoot();
    }

    private void setProfile() {

        reference = FirebaseDatabase.getInstance().getReference("myUsers").child(fUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);

                binding.tvProfileFrag.setText(users.getUserName());

                if (users.getImageUrl().equals("default")) {

                    binding.ivProfileFrag.setImageResource(R.drawable.user);
                }
                else {

                    Glide.with(requireContext()).load(users.getImageUrl()).circleCrop()
                            .into(binding.ivProfileFrag);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);
    }

    private String getFileExtension(Uri uri) {

        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {

        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading...");
        dialog.show();

        if (imageUri != null) {

            StorageReference fileReference =
                    storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {

                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance()
                                .getReference("myUsers").child(fUser.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("imageUrl", mUri);
                        //for modified
                        reference.updateChildren(hashMap);

                        dialog.dismiss();
                    }
                    else {

                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
        else {

            Toast.makeText(getContext(), "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //has Deprecated
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK
//                && data != null && data.getData() != null) {
//
//            imageUri = data.getData();
//
//            if (uploadTask != null && uploadTask.isInProgress()) {
//
//                Toast.makeText(getContext(), "Upload in progress..", Toast.LENGTH_SHORT).show();
//            }
//            else {
//
//                uploadImage();
//            }
//        }
//    }

    private ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Intent data = result.getData();
                        // There are no request codes

                        if (result.getResultCode() == RESULT_OK && data != null
                                && data.getData() != null) {

                            imageUri = data.getData();

                            if (uploadTask != null && uploadTask.isInProgress()) {

                                Toast.makeText(getContext(), "Upload in progress..", Toast.LENGTH_SHORT).show();
                            }
                            else {

                                uploadImage();
                            }
                        }
                    }
                });
}