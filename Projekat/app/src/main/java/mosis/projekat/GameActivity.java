package mosis.projekat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private String ipAddress = "http://192.168.137.225:8081";

    String questionPoints = "5";
    private RadioGroup radioGroup;
    private TextView tv_question;
    private TextView radioButton1;
    private TextView radioButton2;
    private TextView radioButton3;
    private TextView radioButton4;
    private Button button;
    private String questID = "";
    private String correctAnswer = "";
    boolean correctAnswered = false;
    //private boolean confirmAnswer = true;
    private View mGameFormView;
    private View mProgressView;
    private SendPointsTask mAuthTask = null;
    private String myUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        mGameFormView = findViewById(R.id.game_form);
        mProgressView = findViewById(R.id.game_progress);

        Intent intent = getIntent();

        tv_question = (TextView) findViewById(R.id.question_text);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
        radioButton4 = (RadioButton) findViewById(R.id.radioButton4);
        button = (Button) findViewById(R.id.button_confirm_answer);

        //Intent intent = getIntent();
        final String question = intent.getStringExtra("question");
        correctAnswer = intent.getStringExtra("correctAnswer");
        String wrongAnswer1 = intent.getStringExtra("wrongAnswer1");
        String wrongAnswer2 = intent.getStringExtra("wrongAnswer2");
        String wrongAnswer3 = intent.getStringExtra("wrongAnswer3");
        questID = intent.getStringExtra("questID");
        myUsername = intent.getStringExtra("myUsername");

        tv_question.setText(question);

        int i = 0;
        int[] numbers = new int[4];
        Random rand = new Random();
        while (i < 4) {
            final int number = rand.nextInt(4) + 1;
            if (!checkNumber(numbers, number)) {
                numbers[i] = number;
                i++;
                String answer = "";
                if (number == 1)
                    answer = correctAnswer;
                else if (number == 2)
                    answer = wrongAnswer1;
                else if (number == 3)
                    answer = wrongAnswer2;
                else if (number == 4)
                    answer = wrongAnswer3;

                if (i == 1)
                    radioButton1.setText(answer);
                else if (i == 2)
                    radioButton2.setText(answer);
                else if (i == 3)
                    radioButton3.setText(answer);
                else if (i == 4)
                    radioButton4.setText(answer);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!button.getText().equals("Finish")) {
                    AlertDialog builder = new AlertDialog.Builder(GameActivity.this)
                            .setMessage("Confirm this answer ?\n")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    checkRadioButtons();
                                    button.setText("Finish");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Perform Your Task Here--When No is pressed
                                    dialog.cancel();
                                }
                            }).show();
                } else {
                    // FINISH ACTIVITY
                    if (correctAnswered) {
                        showProgress(true);
                        mAuthTask = new SendPointsTask(questionPoints, myUsername);
                        mAuthTask.execute((Void) null);
                    } else {
                        Intent returnIntent = new Intent(GameActivity.this, MainActivity.class);
                        returnIntent.putExtra("status", "wrong");
                        setResult(4, returnIntent);
                        finish();
                    }
                }
            }
        });
    }


    public Boolean checkNumber(int [] array, int numberToCheck)
    {
        boolean contains = false;

        for (int i =0; i < array.length; i++)
        {
            if (array[i] == numberToCheck)
                contains = true;
        }

        return contains;
    }

    public void checkRadioButtons() {

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        int radioBtnID = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(radioBtnID);

        if (radioBtnID != -1) {

            if (selectedRadioButton.getText().equals(correctAnswer))
            {
                selectedRadioButton.setBackgroundColor(Color.GREEN);
                correctAnswered = true;
            }
            else
            {
                selectedRadioButton.setBackgroundColor(Color.RED);
                if (radioButton1.getText().equals(correctAnswer))
                {
                    radioButton1.setBackgroundColor(Color.GREEN);
                }
                else if (radioButton2.getText().equals(correctAnswer))
                {
                    radioButton2.setBackgroundColor(Color.GREEN);
                }
                else if (radioButton3.getText().equals(correctAnswer))
                {
                    radioButton3.setBackgroundColor(Color.GREEN);
                }
                else if (radioButton4.getText().equals(correctAnswer))
                {
                    radioButton4.setBackgroundColor(Color.GREEN);
                }
                button.setText("Finish");
                radioButton1.setEnabled(false);
                radioButton2.setEnabled(false);
                radioButton3.setEnabled(false);
                radioButton4.setEnabled(false);
            }
        }
        else
        {
            // Selektujte neki odgovor TOAST
            Toast.makeText(GameActivity.this, "Select some answer!", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mGameFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mGameFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mGameFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mGameFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class SendPointsTask extends AsyncTask<Void, Void, String> {

        private final String mPoints;
        private final String mUsername;

        SendPointsTask(String numberOfPoints, String username) {
            mPoints = numberOfPoints;
            mUsername = username;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("points", mPoints ));
            postParameters.add(new BasicNameValuePair("username", mUsername ));

            String res = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_checkuser
                // 192.168.137.162:8081
                res = CustomHttpClient.executeHttpPost(ipAddress + "/process_addPoints", postParameters);
                res=res.toString();
                res = res.trim();
                //res= res.replaceAll("\\s+","");
                //Thread.sleep(2000);
            } catch (InterruptedException e) {
                //return false;
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                //return false;
                return "Error";
            }

           /* for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUsername)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }*/

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
                /*Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);*/
                // slanje username-a glavnoj formi
                Intent returnIntent = new Intent(GameActivity.this, MainActivity.class);
                returnIntent.putExtra("status", "correct");
                setResult(4,returnIntent);
                finish();
            }
            else
            {
                Intent returnIntent = new Intent(GameActivity.this, MainActivity.class);
                returnIntent.putExtra("status", "wrong");
                setResult(4,returnIntent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
            AlertDialog builder = new AlertDialog.Builder(GameActivity.this)
                    .setMessage("If you exit, you can't return any more and get 0 points!\nAre you sure ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Exit from activity
                            Intent returnIntent = new Intent(GameActivity.this, MainActivity.class);
                            returnIntent.putExtra("status", "wrong");
                            setResult(4, returnIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform Your Task Here--When No is pressed
                            dialog.cancel();
                        }
                    }).show();
        }


}

