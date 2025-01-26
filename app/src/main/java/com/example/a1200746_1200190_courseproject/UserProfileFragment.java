package com.example.a1200746_1200190_courseproject;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import kotlin.Unit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private DataBaseHelper dbHelper;
    private Button saveChange;
    private TextView username;
    private ImageView profileImage;
    private ImageView changeProfileImage;
    private Boolean isPictureChanged = false ;
    private final int GALLERY_REQ_CODE = 1000;
    private String currentEmail;
    private String currentPhotoPath;
    private static final int CAMERA_REQ_CODE = 1002;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        email = getActivity().findViewById(R.id.email_edit_text);
        password = getActivity().findViewById(R.id.password_edit_text);
        confirmPassword = getActivity().findViewById(R.id.confirm_password_edit_text);
        saveChange = getActivity().findViewById(R.id.save_change_button);
        profileImage = getActivity().findViewById(R.id.imageView_profileImage);
        changeProfileImage = getActivity().findViewById(R.id.imageView_changeProfileImage);
        dbHelper = new DataBaseHelper(getActivity());
        email.setText(DataBaseHelper.currentUser.getEmail());
        currentEmail= email.getText().toString();
        username = getActivity().findViewById(R.id.userName);
        username.setText(dbHelper.currentUser.getFirstName() + " " + dbHelper.currentUser.getLastName());

        Uri picture = dbHelper.getProfilePictureByEmail(email.getText().toString());
        profileImage.setImageURI(picture);


        saveChange.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String emailInput = email.getText().toString();
                String passwordInput = password.getText().toString();
                String confirmPasswordInput = confirmPassword.getText().toString();

                if(TextUtils.isEmpty(emailInput) && TextUtils.isEmpty(passwordInput) && TextUtils.isEmpty(confirmPasswordInput)){
                    email.setText(currentEmail);
                    return;
                }

                if(!emailInput.equals(currentEmail)){
                    if (!TextUtils.isEmpty(emailInput) && !Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                        email.setError("Invalid Email Address");
                        email.setText(currentEmail);
                        return;
                    }
                    //check if both email and password are changed
                    if(!TextUtils.isEmpty(emailInput) && validateInputs(emailInput,passwordInput,confirmPasswordInput)){
                        // Create a confirmation dialog
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Confirm Update")
                                .setMessage("Are you sure you want to save changes ?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dbHelper.updateUserEmail(currentEmail, emailInput);
                                        dbHelper.updateUserPassword(emailInput,passwordInput);
                                        currentEmail = email.getText().toString();
                                        HomeActivity.navEmail.setText(emailInput);
                                        Toast.makeText(getActivity(), "Email and password updated successfully", Toast.LENGTH_SHORT).show();
                                        email.setText(emailInput);
                                        password.setText("");
                                        password.setHint("\uD83D\uDD12  New Password");
                                        confirmPassword.setText("");
                                        confirmPassword.setHint("\uD83D\uDD12 Confirm Password");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Dismiss the dialog without doing anything
                                        dialog.dismiss();
                                    }
                                })
                                .setCancelable(true) // Allow the user to cancel the dialog by tapping outside
                                .show();
                        return;
                    }

                    //if only email is updated
                    if(!TextUtils.isEmpty(emailInput) && TextUtils.isEmpty(passwordInput) && TextUtils.isEmpty(confirmPasswordInput)){
                        // Create a confirmation dialog
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Confirm Update")
                                .setMessage("Are you sure you want to save changes ?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dbHelper.updateUserEmail(currentEmail, emailInput);
                                        currentEmail = email.getText().toString();
                                        HomeActivity.navEmail.setText(emailInput);
                                        Toast.makeText(getActivity(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                                        email.setText(emailInput);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Dismiss the dialog without doing anything
                                        dialog.dismiss();
                                    }
                                })
                                .setCancelable(true) // Allow the user to cancel the dialog by tapping outside
                                .show();
                        return;
                    }

                }
                if (validateInputs(emailInput,passwordInput,confirmPasswordInput)){
                    // Create a confirmation dialog
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Confirm Update")
                            .setMessage("Are you sure you want to save changes ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper.updateUserPassword(currentEmail,passwordInput);
                                    Toast.makeText(getActivity(), "Password and password updated successfully", Toast.LENGTH_SHORT).show();
                                    email.setText(currentEmail);
                                    password.setText("");
                                    password.setHint("\uD83D\uDD12  New Password");
                                    confirmPassword.setText("");
                                    confirmPassword.setHint("\uD83D\uDD12 Confirm Password");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss the dialog without doing anything
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true) // Allow the user to cancel the dialog by tapping outside
                            .show();
                }

            }

        });

        profileImage.setOnClickListener(e -> {
            // Open gallery to select a new profile picture
            Intent iGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(iGallery, GALLERY_REQ_CODE);
        });

        changeProfileImage.setOnClickListener(e -> {
            // Open camera to capture a new profile picture
            Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure there's a camera activity to handle the intent
            if (iCamera.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create a temporary file to store the captured image
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(
                            getActivity(),
                            "com.example.android.a1200746_1200190_courseproject",
                            photoFile
                    );
                    iCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(iCamera, CAMERA_REQ_CODE);
                }
            }
        });



    }


    // Method to validate input fields
    private boolean validateInputs(String emailInput,  String passwordInput, String confirmPasswordInput) {
        if(!TextUtils.isEmpty(passwordInput)) {
            Log.i(TAG, "Paswword not empty ");
            // Validate password
            if ( password.length() < 6 || password.length() > 12 || !passwordInput.matches(".*\\d.*") || !passwordInput.matches(".*[a-z].*") || !passwordInput.matches(".*[A-Z].*")) {
                Log.i(TAG, "Paswword correct formaaat  ");
                password.setError("Password must be 6-12 characters long and contain at least one number, one lowercase letter, and one uppercase letter");
                return false;
            }
            // Validate password confirmation
            if (!TextUtils.equals(passwordInput, confirmPasswordInput)) {
                confirmPassword.setError("Passwords do not match");
                return false;
            }
            return true;
        }


        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE && data != null) {
                // Set image from gallery
                profileImage.setImageURI(data.getData());
                HomeActivity.profilePic.setImageURI(data.getData());

                // Update database
                boolean updated = dbHelper.updateUserProfilePicture(
                        email.getText().toString().trim(),
                        data.getData().toString()
                );
                if (updated) {
                    Toast.makeText(getActivity(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                    isPictureChanged = true;
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQ_CODE) {
                // Set image from camera
                Bitmap capturedImage = BitmapFactory.decodeFile(currentPhotoPath);
                profileImage.setImageBitmap(capturedImage);

                // Update database
                boolean updated = dbHelper.updateUserProfilePicture(
                        email.getText().toString().trim(),
                        currentPhotoPath
                );
                if (updated) {
                    Toast.makeText(getActivity(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                    HomeActivity.profilePic.setImageBitmap(capturedImage);
                    isPictureChanged = true;
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException ex) {
            Toast.makeText(getActivity(), "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}