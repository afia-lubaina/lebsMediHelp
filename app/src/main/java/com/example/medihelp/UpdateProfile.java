package com.example.medihelp;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medihelp.R;
import com.example.medihelp.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UpdateProfile extends AppCompatActivity {

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;

    // UI components
    private EditText editName, editEmail, editPassword, Age, Weight, Blood, Gender;
    private Button saveButton, backButton;
    ImageView ivSelectImage;
    private Spinner bloodGroupSpinner, genderSpinner;
    private ProgressBar progressBar;
    private static final String TAG = "YourClassName";

    private String prevPass, prevEmail;
    private static final int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary));


        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI components
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        Age = findViewById(R.id.Age);
        Weight = findViewById(R.id.Weight);
        saveButton = findViewById(R.id.SaveButton);
        backButton = findViewById(R.id.backButton);
        ivSelectImage = findViewById(R.id.ivSelectImage);
//        progressBar = findViewById(R.id.progressBar);


        bloodGroupSpinner = findViewById(R.id.Blood);
        ArrayAdapter<CharSequence> bloodGroupAdapter = ArrayAdapter.createFromResource(this, R.array.blood_group_options, android.R.layout.simple_spinner_item);
        bloodGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodGroupAdapter);

        genderSpinner = findViewById(R.id.Gender);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        editName.setText(MainActivity.currentUserData.getName());
        editEmail.setText(MainActivity.currentUserData.getEmail());
        editPassword.setText(MainActivity.currentUserData.getPassword());
        Age.setText(MainActivity.currentUserData.getAge());
        Weight.setText(MainActivity.currentUserData.getWeight());


        String[] bloodGroups = getResources().getStringArray(R.array.blood_group_options);
        int position = -1;

        for (int i = 0; i < bloodGroups.length; i++) {
            if (MainActivity.currentUserData.getBloodGroup().equals(bloodGroups[i])) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            bloodGroupSpinner.setSelection(position);
        }

        String[] genders = getResources().getStringArray(R.array.gender_options);
        int position2 = -1;

        for (int i = 0; i < genders.length; i++) {
            if (MainActivity.currentUserData.getGender().equals(genders[i])) {
                position2 = i;
                break;
            }
        }

        if (position2 != -1) {
            genderSpinner.setSelection(position2);
        }

        String imageUrl = MainActivity.currentUserData.getPicture(); // Replace with the actual method or key to access the image URL
        if(imageUrl!=null) {
            Picasso.get().load(imageUrl).into(ivSelectImage);
        }



        ivSelectImage.setOnClickListener(view -> {
            openImagePicker();
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileData();
                Intent intent = new Intent(getApplicationContext(), UserDetails.class);
                startActivity(intent);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle back button click or navigation to previous screen
                Intent intent = new Intent(getApplicationContext(), UserDetails.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void openImagePicker() {
        // Use an Intent to open the image picker
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // The user selected an image
            Uri selectedImageUri = data.getData();
            // Now, you can proceed with the image upload process to Firebase Storage.
            uploadImageToFirebase(selectedImageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Create a reference to the Firebase Storage location where you want to store the image.
        // Replace "your-app-name" with your actual Firebase Storage bucket name.
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Create a reference to the specific file you want to upload.
            // You can use the imageUri to create a reference.
            // Make sure to define a unique file name, for example, based on the user's ID.
            StorageReference imageRef = storageRef.child(uid); // Replace "user_id123" with the actual user's ID

            // Upload the image to the specified Storage reference.
            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded successfully
                    // Get the download URL for the uploaded image

                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            String imageUrl = downloadUri.toString();
                            Picasso.get().load(imageUrl).into(ivSelectImage);

                            // Now you have the download URL for the uploaded image
                            // Store this URL in the Firebase Realtime Database under the user's profile.
                            // Update the user's profile with the new image URL

                            userDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User existingUser = dataSnapshot.getValue(User.class);

                                        if (!imageUrl.isEmpty()) {
                                            existingUser.setPicture(imageUrl);
//                                        ivSelectImage.setImageBitmap();
                                        }
                                        // Save the updated user data to Firebase
                                        userDatabase.child(uid).setValue(existingUser)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(UpdateProfile.this, "Saved Changes", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            Toast.makeText(UpdateProfile.this, "Something went wrong...", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle the error

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            // Handle the error if the image upload fails
                        }
                    });
                }
            });
        }
    }



    private void saveProfileData() {
        // Retrieve user input
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String age = Age.getText().toString().trim();
        // String gender = Gender.getText().toString().trim();
        String weight = Weight.getText().toString().trim();
        //  String bloodGroup = Blood.getText().toString().trim();

        String selectedGender = genderSpinner.getSelectedItem().toString();
        String selectedBloodGroup = bloodGroupSpinner.getSelectedItem().toString();


        // Check if any of the fields have non-empty values
        if (!name.isEmpty() || !email.isEmpty() || !password.isEmpty() || !age.isEmpty() || !weight.isEmpty() || genderSpinner.getSelectedItemPosition() != 0 ||  bloodGroupSpinner.getSelectedItemPosition() != 0) {
            // At least one field has a non-empty value, so we can proceed to update the profile

            // Validate user input (add your own validation logic)

            // Update the user's profile data in Firebase
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();


                // Fetch the existing user data from Firebase
                userDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User existingUser = dataSnapshot.getValue(User.class);

                            // Update the fields with non-empty values and change text color to black
                            if (!name.isEmpty() && !name.equals(MainActivity.currentUserData.getName())) {
                                existingUser.setName(name);
                                editName.setTextColor(getResources().getColor(R.color.black)); // Change text color to black
                            }
                            if (!email.isEmpty() && !email.equals(MainActivity.currentUserData.getEmail())) {
                                prevEmail = existingUser.getEmail();
                                prevPass = existingUser.getPassword();
                                UpdateEmail(prevPass,  email);
                                existingUser.setEmail(email);
                                editEmail.setTextColor(getResources().getColor(R.color.black)); // Change text color to black

                            }
                            if (!password.isEmpty() && !password.equals(MainActivity.currentUserData.getPassword())) {
                                prevPass = existingUser.getPassword();
                                existingUser.setPassword(password);
                                editPassword.setTextColor(getResources().getColor(R.color.black)); // Change text color to black
                                UpdatePassword(prevPass, password);
                            }
                            if (!age.isEmpty() && !age.equals(MainActivity.currentUserData.getAge())) {
                                existingUser.setAge(age);
                                Age.setTextColor(getResources().getColor(R.color.black)); // Change text color to black
                            }
                            if (genderSpinner.getSelectedItemPosition() != 0) {
                                existingUser.setGender(selectedGender); // Update gender value with the selected option

                            }
                            if (bloodGroupSpinner.getSelectedItemPosition() != 0) {
                                existingUser.setBloodGroup(selectedBloodGroup); // Update blood group value with the selected option

                            }
                            if (!weight.isEmpty() && !weight.equals(MainActivity.currentUserData.getWeight())) {
                                existingUser.setWeight(weight);
                                Weight.setTextColor(getResources().getColor(R.color.black)); // Change text color to black
                            }
//                            if (!bloodGroup.isEmpty()) {
//                                existingUser.setBloodGroup(bloodGroup);
//                                Blood.setTextColor(getResources().getColor(R.color.black)); // Change text color to black
//                            }

                            // Save the updated user data to Firebase
                            userDatabase.child(uid).setValue(existingUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(UpdateProfile.this, "Saved Changes", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(UpdateProfile.this, "Something went wrong...", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle the error

                    }


                });
            }
        }
    }

    private void UpdatePassword(String oldPassword, String newPassword) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
        currentUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        currentUser.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //password updated
                                        Toast.makeText(UpdateProfile.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //password failed

                                        Toast.makeText(UpdateProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void UpdateEmail(String currentPassword, String newEmail) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(authCredential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> reauthTask) {
                        if (reauthTask.isSuccessful()) {
                            // Re-authentication successful, update email
                            currentUser.updateEmail(newEmail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> emailUpdateTask) {
                                            if (emailUpdateTask.isSuccessful()) {
                                                // Email updated successfully and verification email sent
                                                Log.d(TAG, "Email updated successfully. Verification email sent.");
                                                Toast.makeText(UpdateProfile.this, "Email updated successfully. Please check your inbox for a verification email.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e(TAG, "Failed to update email: " + emailUpdateTask.getException().getMessage());
                                                Toast.makeText(UpdateProfile.this, "Failed to update email: " + emailUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Re-authentication failed: " + reauthTask.getException().getMessage());
                            Toast.makeText(UpdateProfile.this, "Re-authentication failed: " + reauthTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendEmailVerification() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Verification email sent successfully
                            Log.d(TAG, "Email verification sent.");
                            Toast.makeText(UpdateProfile.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Log the error message
                            Log.e(TAG, "Failed to send email verification: " + task.getException().getMessage());
                            Toast.makeText(UpdateProfile.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }





}






