package mosis.projekat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class RegisterActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private ImageButton selectImage;
    private EditText et_username;
    private EditText et_password;
    private EditText et_password2;
    private EditText et_name;
    private EditText et_lastname;
    private EditText et_phonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_username = (EditText) findViewById(R.id.username_register);
        et_password = (EditText) findViewById(R.id.password_register);
        et_password2 = (EditText) findViewById(R.id.password2_register);
        et_name = (EditText) findViewById(R.id.name_register);
        et_lastname = (EditText) findViewById(R.id.lastname_register);
        et_phonenumber = (EditText) findViewById(R.id.phoneNumber_register);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        selectImage = (ImageButton)findViewById(R.id.imageButton);
        selectImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });

        Button registerButton = (Button) findViewById(R.id.createProfile_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateProfile();
            }
        });

    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    //Log.i(TAG, "Image Path : " + path);
                    // Set the image in ImageView
                    selectImage.setImageURI(selectedImageUri);
                }
            }
        }
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void attemptCreateProfile() {

        // Store values at the time of the login attempt.
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();
        String password2 = et_password2.getText().toString();
        String name = et_name.getText().toString();
        String lastName = et_lastname.getText().toString();
        String phoneNumber = et_phonenumber.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username, if the user entered one.
        if (TextUtils.isEmpty(username)) {
            et_username.setError(getString(R.string.error_invalid_username));
            focusView = et_username;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            et_password.setError(getString(R.string.error_invalid_password));
            focusView = et_password;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            et_password.setError(getString(R.string.error_invalid_to_short_password));
            focusView = et_password;
            cancel = true;
        }

        // TREBA PROVERITI DA LI MOZE OVAKO
        // Check for a valid password2, if the user entered one.
        if (!password.equals(password2)) {
            et_password2.setError(getString(R.string.error_invalid_password2));
            focusView = et_password2;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}
