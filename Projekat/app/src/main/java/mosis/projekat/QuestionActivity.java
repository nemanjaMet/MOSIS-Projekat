package mosis.projekat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;


public class QuestionActivity extends AppCompatActivity {

    private String ipAddress = "http://192.168.137.174:8081";

    private int numberOfQuestion = 5;
    private int questionNumber = 0;
    private EditText et_question;
    private EditText et_correctAnswer;
    private EditText et_wrongAnswer1;
    private EditText et_wrongAnswer2;
    private EditText et_wrongAnswer3;
    private Button btn_nextQuest;
    private EditText et_question_status;
    private Spinner spinner;
    //private TextView tv_question_status;
    String response = null;
    private SendQuestionsTask mAuthTask = null;
    private View mQuestionFormView;
    private View mProgressView;
    private boolean editData = false;
    private String [] questionArray;
    private String [] correctAnswerArray;
    private String [] wrongAnswerArray;
    private int questNumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        et_question = (EditText)findViewById(R.id.question);
        et_correctAnswer = (EditText)findViewById(R.id.correct_answer);
        et_wrongAnswer1 = (EditText)findViewById(R.id.wrong_answer1);
        et_wrongAnswer2 = (EditText)findViewById(R.id.wrong_answer2);
        et_wrongAnswer3 = (EditText)findViewById(R.id.wrong_answer3);
        et_question_status = (EditText)findViewById(R.id.question_status);
        spinner = (Spinner)findViewById(R.id.spinner_category);
        // tv_question_status = (TextView)findViewById(R.id.question_status);


       // String status = String.valueOf(questionNumber+1) + "/" + String.valueOf(numberOfQuestion) + " questions";
        //tv_question_status.setText(status);

        btn_nextQuest = (Button)findViewById(R.id.next_question);
        btn_nextQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick();
            }
        });

        String spinnerCategory = "";
        Intent intent = getIntent();
        questionNumber = Integer.parseInt(intent.getStringExtra("questionNumber"));
        String questionClicked = intent.getStringExtra("questionClicked");

            if (questionNumber > 0) {
                numberOfQuestion = Integer.parseInt(intent.getStringExtra("numberOfQuestion"));
                spinnerCategory = intent.getStringExtra("spinnerCategory");
            }

        if (questionClicked == null) {

            if (questionNumber == 0) {
                et_question.setVisibility(View.GONE);
                et_correctAnswer.setVisibility(View.GONE);
                et_wrongAnswer1.setVisibility(View.GONE);
                et_wrongAnswer2.setVisibility(View.GONE);
                et_wrongAnswer3.setVisibility(View.GONE);
                View viewBottom = (View) findViewById(R.id.line_buttom);
                viewBottom.setVisibility(View.INVISIBLE);
           /* View viewTop = (View)findViewById(R.id.line_top);
            viewTop.setVisibility(View.INVISIBLE);*/

                btn_nextQuest.setText("Start with question");
            } else if (questionNumber > 0 && (questionNumber - 1) < numberOfQuestion) {
                String status = String.valueOf(questionNumber) + "/" + String.valueOf(numberOfQuestion) + " questions";
                Boolean finded = false;
                int i = 0;
                while (!finded) {
                    spinner.setSelection(i);
                    if (spinner.getSelectedItem().toString().equals(spinnerCategory))
                        finded = true;
                    i++;
                }
                et_question_status.clearFocus();
                et_question_status.setHint("H");
                et_question_status.setFocusable(false);
                et_question_status.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)}); // povecavamo duzinu karaktera u editText-u
                et_question_status.setText(status);
                // velicina textBoxa da se poveca na veci od 1
                et_question_status.setEnabled(false);
                spinner.setEnabled(false);
            } else {
                // SLANJE PODATAKA SERVERU
                mQuestionFormView = findViewById(R.id.question_form);
                mProgressView = findViewById(R.id.question_progress);
                showProgress(true);
                Questions questions = new Questions();
                questions.setCategory(intent.getStringExtra("category"));
                questions.setQuestions(intent.getStringExtra("questions"));
                questions.setCorrectAnswers(intent.getStringExtra("correctAnswers"));
                questions.setWrongAnswers(intent.getStringExtra("wrongAnswers"));
                questions.setLongitudeLatitude(intent.getStringExtra("longitudeLatitude"));
                //questions.setCategoryLongLat(intent.getStringExtra("categoryLongLat"));
                questions.setLongitudeCategory(intent.getStringExtra("categoryLong"));
                questions.setLatitudeCategory(intent.getStringExtra("categoryLat"));
                questions.setCreatedUser(intent.getStringExtra("createdUser"));
                String friendsUsernames = intent.getStringExtra("friendsUsernames");
                mAuthTask = new SendQuestionsTask(questions, friendsUsernames);
                mAuthTask.execute((Void) null);
            }
        }
        else
        {
            questNumb = Integer.parseInt(questionClicked) - 1;

            String status = String.valueOf(questNumb) + "/" + String.valueOf(numberOfQuestion) + " questions";
            boolean finded = false;
            int i = 0;
            while (!finded) {
                spinner.setSelection(i);
                if (spinner.getSelectedItem().toString().equals(spinnerCategory))
                    finded = true;
                i++;
            }
            et_question_status.clearFocus();
            et_question_status.setHint("H");
            et_question_status.setFocusable(false);
            et_question_status.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)}); // povecavamo duzinu karaktera u editText-u
            et_question_status.setText(status);
            // velicina textBoxa da se poveca na veci od 1
            et_question_status.setEnabled(false);
            spinner.setEnabled(false);

            // editovanje podataka
            String questions = intent.getStringExtra("question");
            String correctAnswers = intent.getStringExtra("correctAnswer");
            String wrongAnswers = intent.getStringExtra("wrongAnswer");

            if (questNumb == 0 && questionNumber-1 == 0)
            {
                questionArray = new String[1];
                correctAnswerArray = new String[1];
                wrongAnswerArray = new String[1];
                questionArray[0] = questions;
                correctAnswerArray[0] = correctAnswers;
                wrongAnswerArray[0] = wrongAnswers;
            }
            else
            {
                questionArray = questions.split("&&");
                correctAnswerArray = correctAnswers.split("&&");
                wrongAnswerArray = wrongAnswers.split("&&");
            }


            String [] wrongAnswer123 = wrongAnswerArray[questNumb].split("\\|\\|");

            et_question.setText(questionArray[questNumb]);
            et_correctAnswer.setText(correctAnswerArray[questNumb]);
            et_wrongAnswer1.setText(wrongAnswer123[0]);
            et_wrongAnswer2.setText(wrongAnswer123[1]);
            et_wrongAnswer3.setText(wrongAnswer123[2]);

            editData = true;

        }
    }

    private void btnClick() {
        if (!editData) {
            if (questionNumber == 0) {
                String spinnerSelection = spinner.getSelectedItem().toString();
                if (et_question_status.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Insert number of question (1-9)", Toast.LENGTH_LONG).show();
                    return;
                }
                numberOfQuestion = Integer.parseInt(et_question_status.getText().toString().trim());

                if (numberOfQuestion < 1 && numberOfQuestion > 9) {
                    Toast.makeText(this, "Insert number of question (1-9)", Toast.LENGTH_LONG).show();
                    return;
                }
                if (spinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Select some category!", Toast.LENGTH_LONG).show();
                    return;
                }

                // TREBA PROVERITI DA LI JE KORISNIK VEC POSTAVIO PITANJA ZA OVU KATEGORIJU, a mozda i ne ?????
                questionNumber++;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("numberOfQuestion", Integer.toString(numberOfQuestion));
                returnIntent.putExtra("spinnerCategory", spinnerSelection);
                returnIntent.putExtra("questionNumber", Integer.toString(questionNumber));
                setResult(Activity.RESULT_FIRST_USER, returnIntent);
                finish();
                // mozda mora pre finish-a da ide
           /* Snackbar.make(btn_nextQuest, "Post questions on desired positions, near this location... ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
            } else {
                String question = (et_question.getText().toString()).trim();
                String correctAnswer = (et_correctAnswer.getText().toString()).trim();
                String wrongAnswer1 = (et_wrongAnswer1.getText().toString()).trim();
                String wrongAnswer2 = (et_wrongAnswer2.getText().toString()).trim();
                String wrongAnswer3 = (et_wrongAnswer3.getText().toString()).trim();

                if (question.isEmpty() || correctAnswer.isEmpty() || wrongAnswer1.isEmpty() || wrongAnswer2.isEmpty() || wrongAnswer3.isEmpty()) {
                    Toast.makeText(QuestionActivity.this, "Popunite sva polja!", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //questionNumber++;
                    if (questionNumber - 1 < numberOfQuestion) {
                        questionNumber++;
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("questionNumber", Integer.toString(questionNumber));
                        returnIntent.putExtra("question", question);
                        returnIntent.putExtra("correctAnswer", correctAnswer);
                        returnIntent.putExtra("wrongAnswer", wrongAnswer1 + "||" + wrongAnswer2 + "||" + wrongAnswer3);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();

                    /*if (questionNumber == numberOfQuestion-1)
                    {
                        btn_nextQuest.setText("Post questions");
                    }*/

                        //String status = String.valueOf(questionNumber+1) + "/" + String.valueOf(numberOfQuestion) + " questions";
                        // tv_question_status.setText(status);

                   /* et_question.setText("");
                    et_correctAnswer.setText("");
                    et_wrongAnswer1.setText("");
                    et_wrongAnswer2.setText("");
                    et_wrongAnswer3.setText("");*/
                    }
                }
            }

        }
        else
        {
            //String [] wrongAnswer123 = wrongAnswer[questNumb].split("||");
            String question = (et_question.getText().toString()).trim();
            String correctAnswer = (et_correctAnswer.getText().toString()).trim();
            String wrongAnswer1 = (et_wrongAnswer1.getText().toString()).trim();
            String wrongAnswer2 = (et_wrongAnswer2.getText().toString()).trim();
            String wrongAnswer3 = (et_wrongAnswer3.getText().toString()).trim();

            if (question.isEmpty() || correctAnswer.isEmpty() || wrongAnswer1.isEmpty() || wrongAnswer2.isEmpty() || wrongAnswer3.isEmpty()) {
                Toast.makeText(QuestionActivity.this, "Popunite sva polja!", Toast.LENGTH_LONG).show();
                return;
            }

            questionArray[questNumb] = question;
            correctAnswerArray[questNumb] = correctAnswer;
            wrongAnswerArray[questNumb] = wrongAnswer1 + "||" + wrongAnswer2 + "||" + wrongAnswer3;

            question = "";
            for (int i = 0; i < questionArray.length; i++)
            {
                question += questionArray[i] + "&&";
            }
            question = question.substring(0,question.length()-2);

            correctAnswer = "";
            for (int i = 0; i < correctAnswerArray.length; i++)
            {
                correctAnswer += correctAnswerArray[i] + "&&";
            }
            correctAnswer = correctAnswer.substring(0,correctAnswer.length()-2);

            wrongAnswer1 = "";
            for (int i = 0; i < wrongAnswerArray.length; i++)
            {
                wrongAnswer1 += wrongAnswerArray[i] + "&&";
            }
            wrongAnswer1 = wrongAnswer1.substring(0,wrongAnswer1.length()-2);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("question", question);
            returnIntent.putExtra("correctAnswer", correctAnswer);
            returnIntent.putExtra("wrongAnswer", wrongAnswer1);
            setResult(2, returnIntent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent(QuestionActivity.this, MainActivity.class);
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_question, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mQuestionFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mQuestionFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mQuestionFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mQuestionFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class SendQuestionsTask extends AsyncTask<Void, Void, String> {

        private final Questions questions;
        private final String mFriendsUsernames;

        SendQuestionsTask(Questions question, String friendsUsernames) {
            questions = question;
            mFriendsUsernames = friendsUsernames;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("category", questions.getCategory() ));
            postParameters.add(new BasicNameValuePair("questions", questions.getQuestions() ));
            postParameters.add(new BasicNameValuePair("correctAnswers", questions.getCorrectAnswers() ));
            postParameters.add(new BasicNameValuePair("wrongAnswers", questions.getWrongAnswers() ));
            postParameters.add(new BasicNameValuePair("longitudeLatitude", questions.getLongitudeLatitude() ));
            //postParameters.add(new BasicNameValuePair("categoryLongLat", questions.getCategoryLongLat() ));
            postParameters.add(new BasicNameValuePair("categoryLong", questions.getLongitudeCategory() ));
            postParameters.add(new BasicNameValuePair("categoryLat", questions.getLatitudeCategory() ));
            postParameters.add(new BasicNameValuePair("createdUser", questions.getCreatedUser() ));
            postParameters.add(new BasicNameValuePair("friendsUsernames", mFriendsUsernames ));
            String res = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_checkuser
                // 192.168.137.162:8081
                response = CustomHttpClient.executeHttpPost(ipAddress + "/process_newquestion", postParameters);
                res=response.toString();
                res = res.trim();
                //res= res.replaceAll("\\s+","");
                Thread.sleep(2000);
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
                Intent returnIntent = new Intent(QuestionActivity.this, MainActivity.class);
                returnIntent.putExtra("status", "correct");
                setResult(3,returnIntent);
                finish();
            }
            else
            {
                Intent returnIntent = new Intent(QuestionActivity.this, MainActivity.class);
                returnIntent.putExtra("status", "error");
                setResult(3,returnIntent);
                finish();
            }

            /*if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }*/
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
