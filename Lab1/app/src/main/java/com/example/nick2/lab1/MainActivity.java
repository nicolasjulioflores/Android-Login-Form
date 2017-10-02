package com.example.nick2.lab1;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button saveButton;
    ImageButton profPic;
    EditText password;
    EditText username;
    EditText fullname;
    ImageView matchBox;
    Button clearButton;
    Dialog rePassBox;
    String currPass;
    boolean fClear; // Are these fields clear? for the fields:
    boolean uClear; // username, fullname and password
    boolean pClear;
    boolean pMatch = false;
    String thePassword;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_land);
        }
        //Toolbar for later
        //Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(myToolbar);

        // Respective fields of the form
        fullname = (EditText)findViewById(R.id.fullname);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);

        // Clear button
        clearButton = (Button)findViewById(R.id.clear);

        // Profile picture button
        profPic = (ImageButton)findViewById(R.id.profPic);

        //Save Button
        saveButton = (Button)findViewById(R.id.save);

        // Password matches box. Initialize to not visible
        // Make dependent on the pMatch variable
        matchBox = (ImageView) findViewById(R.id.matchBox);


        // Retrieving saved data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 1. Initialize these respective fields to their saved 'states'
        // 2. If any of these fields are nonempty, switch the button text to "Clear"
        // Must come after initialize clear button
        fullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s.toString().equals(""))) {
                    fClear = false;
                    switchClearButton(fClear, uClear, pClear, clearButton);
                } else {
                    fClear = true;
                    switchClearButton(fClear, uClear, pClear, clearButton);
                }
            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!(s.toString().equals(""))) {
                    uClear = false;
                    switchClearButton(fClear, uClear, pClear, clearButton);
                } else {
                    uClear = true;
                    switchClearButton(fClear, uClear, pClear, clearButton);
                }
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!(s.toString().equals(thePassword))) {
                    pMatch = false;
                    matchBox.setVisibility(View.INVISIBLE);
                }

                if (!(s.toString().equals(""))) {
                    pClear = false;
                    switchClearButton(fClear, uClear, pClear, clearButton);
                } else {
                    pClear = true;
                    switchClearButton(fClear, uClear, pClear, clearButton);
                }
            }
        });

        // Check if these fields are saved locally
        if (prefs.contains("fullname")) fullname.setText(prefs.getString("fullname", ""));
        if (prefs.contains("username")) username.setText(prefs.getString("username",""));
        if (prefs.contains("password")) password.setText(prefs.getString("password",""));
        if (prefs.contains("photo")) profPic.setImageBitmap(readImageFile(prefs.getString("photo","")));
        if (prefs.contains("pMatch")) pMatch = strToBool(prefs.getString("pMatch",""));


        // Initialize match box && set our current validated password to the one we had stored
        if (pMatch) {
            matchBox.setVisibility(View.VISIBLE);
            thePassword = password.getText().toString();
        }
        else matchBox.setVisibility(View.INVISIBLE);

        // Initialize and set functionality of clear button
        switchClearButton(fClear, uClear, pClear, clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Clear username, fullname and the image (if there is one)
                username.setText("");
                fullname.setText("");
                profPic.setImageBitmap(null);

                // Password field should be cleared, box goes away and user has to reconfirm
                password.setText("");
                matchBox.setVisibility(View.INVISIBLE);
                pMatch = false;
            }
        });



        // If a user unfocuses the password field, they are asked to re-enter their password in a
        // dialog box
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {


                if (!focus && (!((EditText)view).getText().toString().equals("")) && !pMatch) {
                    currPass = password.getText().toString();
                    startDialog(currPass);
                }


            }
        });

        // If users click on the profile picture button, the camera intent is sent
        // The image will be displayed on the button they clicked on.
        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTakePictureIntent(profPic);

            }
        });

        // If users hit the save button, saveForm() is called. Read saveForm() comments to learn when
        // saves are allowed.
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveForm();

            }
        });


    }

    // The "Already Have an Account" button switches to read "Clear" if any field is nonempty
    private void switchClearButton(boolean fClear, boolean uClear, boolean pClear, Button clearButton) {
        if (fClear && uClear && pClear) clearButton.setText("Already Have an Account");
        else clearButton.setText("Clear");
    }

    // Create a dialog box that disappears when the user correctly types in their password
    private void startDialog(final String currPass) {
        final LayoutInflater inflater = getLayoutInflater();
        final View passView = inflater.inflate(R.layout.passbox_dialog, null);

        // Access the passbox dialog xml file
        rePassBox = new Dialog(MainActivity.this);
        rePassBox.setContentView(passView);

        final EditText rePass = (EditText)passView.findViewById(R.id.repass);

        rePass.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int count, int after) {


            }

            public void afterTextChanged(Editable s) {
                if (s.toString().equals(currPass)) {
                    pMatch = true;
                    thePassword = s.toString();
                    matchBox.setVisibility(View.VISIBLE);
                    rePassBox.dismiss();
                }

            }

        });

        rePassBox.show();


    }


    // Calls the camera to 'Take a picture'; Request is received in onActivity Result
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private void sendTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Retrieves the image from the camera and sets it on the button
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profPic.setImageBitmap(imageBitmap);
        }

        /*
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            profPic.setImageURI(Crop.getOutput(data));
        } */
    }

    // Function to save all the data
    // Handles cases: (1) Not all fields are filled (2) Passwords do not match
    // Photo is optional, so do not require that field
    // By displaying a helpful alertDialog
    private void saveForm(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Handling to make sure that all the fields have entries
        if (!uClear && !fClear && !pClear) {

            // Make sure that the user has entered a password that matches
            if (pMatch) {

                prefs.edit().putString("username", username.getText().toString()).apply();

                prefs.edit().putString("fullname", fullname.getText().toString()).apply();

                prefs.edit().putString("password", password.getText().toString()).apply();

                prefs.edit().putString("pMatch", String.valueOf(pMatch)).apply();

                String imagePath = storeImageFile(((BitmapDrawable)profPic.getDrawable()).getBitmap());
                prefs.edit().putString("photo", imagePath).apply();


            } else {
                AlertDialog.Builder missingField = new AlertDialog.Builder(MainActivity.this);
                missingField.setTitle("Password not confirmed");
                missingField.setMessage("Please retype your password in the box that appears.");
                missingField.show();
            }

        } else {
            Log.d("tag", uClear + "-> user  " + pClear + "-> pass  " + fClear + "->full");
            AlertDialog.Builder missingField = new AlertDialog.Builder(MainActivity.this);
            missingField.setTitle("Missing Fields");
            missingField.setMessage("One of more fields in the form need to be entered.");
            missingField.show();
        }

    }

    // Make a new image file for the photo taken and then save it.
    // Find the path from the file and store that. Display that when we need
    // to restore the image
    private String storeImageFile(Bitmap bitmap) {
        File imageFile = null;

        // File name
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "Lab1" + "_" + time;

        // Make Filestream
        OutputStream out = null;

        String path = this.getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).getAbsolutePath();
        File imageDir = new File(path + "/Saved_images");

        if(!imageDir.exists()) imageDir.mkdirs();

        try {
            imageFile = File.createTempFile(imageFileName,".png", imageDir.getAbsoluteFile());
        } catch (IOException e) {
            Log.d("storeImageFile()", "IOException e");
        }

        try {
            out = new FileOutputStream(imageFile);
        } catch (FileNotFoundException e) {
            Log.d("storeImageFile()", "File not found");
        }

        // Write to the file
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

        try {
            out.flush();
            out.close();
        } catch (IOException f) {
            Log.d("storeImageFile()", "IOException f");
        }


        Log.d("path XXXXXXXXXXXXXXXXXX", imageFile.getAbsolutePath());
        return imageFile.getAbsolutePath();
    }

    // Retrieves the bitmap from the file
    private Bitmap readImageFile(String path) {

        return BitmapFactory.decodeFile(path);
    }

    // Convert a string to a boolean ("true" = true)
    // "anything else" = false
    private boolean strToBool(String b) {
        if (b.equals("true")) return true;
        else return false;
    }
}
