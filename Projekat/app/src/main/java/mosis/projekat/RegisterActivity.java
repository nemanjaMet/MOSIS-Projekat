package mosis.projekat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private String ipAddress = "http://192.168.0.103:8081";

    private static final int SELECT_PICTURE = 100;
    private ImageButton selectImage;
    private Bitmap bitmap = null;
    private EditText et_username;
    private EditText et_password;
    private EditText et_password2;
    private EditText et_name;
    private EditText et_lastname;
    private EditText et_phonenumber;
    private UserLoginTask mAuthTask = null;
    String response = null;
    private View mRegisterFormView;
    private View mProgressView;
    ProjekatDBAdapter dbAdapter;
    private boolean isMainActivity = false;


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

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        selectImage = (ImageButton)findViewById(R.id.imageButton);


        Button registerButton = (Button) findViewById(R.id.createProfile_button);


        dbAdapter = new ProjekatDBAdapter(getApplicationContext());
        Bundle p = getIntent().getExtras();
        if (p != null)
        {
            String friendUsername =p.getString("friendUsername");
            dbAdapter.open();
            User friend = dbAdapter.getEntry(friendUsername);
            dbAdapter.close();
            friend.setLongitude(String.valueOf(p.getDouble("longitude")));
            friend.setLatitude(String.valueOf(p.getDouble("latitude")));
            isMainActivity = true;

            TextView selectAvatar = (TextView)findViewById(R.id.imageLabel);
            selectAvatar.setVisibility(View.INVISIBLE);
            et_username.setEnabled(false);
            et_username.setFocusable(false);
            et_password.setHint(R.string.hint_longitude);
            et_password.setEnabled(false);
            et_password.setFocusable(false);
            et_password.setInputType(InputType.TYPE_CLASS_TEXT);
            et_password2.setEnabled(false);
            et_password2.setFocusable(false);
            et_password2.setInputType(InputType.TYPE_CLASS_TEXT);
            et_password2.setHint("Latitude");
            et_name.setEnabled(false);
            et_name.setFocusable(false);
            et_lastname.setEnabled(false);
            et_lastname.setFocusable(false);
            et_phonenumber.setEnabled(false);
            et_phonenumber.setFocusable(false);
            registerButton.setVisibility(View.INVISIBLE);

            et_username.setText(friendUsername);
            et_password.setText(friend.getLongitude());
            et_password2.setText(friend.getLatitude());
            et_name.setText(friend.getName());
            et_lastname.setText(friend.getLastname());
            et_phonenumber.setText(friend.getPhoneNumber());

            bitmap = StringToBitMap(friend.getImage());
            //Setting the Bitmap to ImageView
            selectImage.setImageBitmap(bitmap);
        }
        else
        {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptCreateProfile();
                }
            });
            selectImage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    openImageChooser();
                }
            });
        }

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
                    // Set the image in ImageView
                   // selectImage.setImageURI(selectedImageUri); RADI I OVAKO
                    try {
                        //Getting the Bitmap from Gallery
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        //Setting the Bitmap to ImageView
                        selectImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        if (bitmap == null)
        {
           // Toast.makeText(RegisterActivity.this, "Select your avatar!", Toast.LENGTH_SHORT).show();
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
            else
                Toast.makeText(RegisterActivity.this, "Select your avatar!", Toast.LENGTH_SHORT).show();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            User user = new User(username);
            user.setPassword(password);
            user.setName(name);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            //user.setImage(bitmap);
            String image = getStringImage(bitmap);
            user.setImage(image);
            user.setCreated(mydate);
            //Intent returnIntent = new Intent();
            //returnIntent.putExtra("result", user);
            /*Bundle mBundle = new Bundle();
            mBundle.putSerializable("result",user);
            returnIntent.putExtras(mBundle);*/
           // setResult(Activity.RESULT_OK,returnIntent);
            //finish();
            showProgress(true);
            mAuthTask = new UserLoginTask(user);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final User user;

        UserLoginTask(User mUser) {
            user = mUser;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            //String stringImage = getStringImage(user.getImage());
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", user.getUsername() ));
            postParameters.add(new BasicNameValuePair("password", user.getPassword() ));
            postParameters.add(new BasicNameValuePair("name", user.getName() ));
            postParameters.add(new BasicNameValuePair("lastname", user.getLastname() ));
            postParameters.add(new BasicNameValuePair("phonenumber", user.getPhoneNumber() ));
            //postParameters.add(new BasicNameValuePair("image", stringImage ));
            postParameters.add(new BasicNameValuePair("image", user.getImage()));
            postParameters.add(new BasicNameValuePair("created", user.getCreated() ));
            String res = null;
            try {
                // Simulate network access. //http://192.168.0.103:8081/process_newuser
                // 192.168.137.79:8081
                response = CustomHttpClient.executeHttpPost(ipAddress + "/process_newuser", postParameters);
                res=response.toString();
                res = res.trim();
                //Thread.sleep(2000);
            } catch (InterruptedException e) {
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }

            // TODO: register the new account here.
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);

            if (result.equals("success"))
            {
                // otvaranje glavne forme
                /*Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(i);*/
                // slanje samo username-a
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", et_username.getText().toString());
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
                // slanje objekta
               /* Intent returnIntent = new Intent();
                returnIntent.putExtra("result", user);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("result",user);
                returnIntent.putExtras(mBundle);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();*/
            }
            else if (result.equals("userErr"))
            {
                et_username.setError(getString(R.string.error_username_already_taken));
                et_username.requestFocus();
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
            else
            {
                Toast.makeText(RegisterActivity.this, "Error on sending data! Check internet connection.", Toast.LENGTH_SHORT).show();
                //mUsernameView.setError(getString(R.string.error_invalid_username));
                //mUsernameView.requestFocus();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /*public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }*/

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (isMainActivity) {
            Intent returnIntent = new Intent(RegisterActivity.this, MainActivity.class);
            setResult(RESULT_CANCELED,returnIntent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
