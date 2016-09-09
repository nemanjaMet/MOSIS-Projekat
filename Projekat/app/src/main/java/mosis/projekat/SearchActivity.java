package mosis.projekat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity {

    private CheckBox cb_culture;
    private CheckBox cb_cuisine;
    private CheckBox cb_geography;
    private CheckBox cb_history;
    private CheckBox cb_sport;
    private EditText et_radius;
    private EditText et_username;
    private Button btn_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        cb_culture = (CheckBox) findViewById(R.id.checkbox_culture);
        cb_cuisine = (CheckBox) findViewById(R.id.checkbox_cuisine);
        cb_geography = (CheckBox) findViewById(R.id.checkbox_geography);
        cb_history = (CheckBox) findViewById(R.id.checkbox_history);
        cb_sport = (CheckBox) findViewById(R.id.checkbox_sport) ;
        et_radius = (EditText) findViewById(R.id.radius);
        et_username = (EditText) findViewById(R.id.search_username);
        btn_search = (Button) findViewById(R.id.search_button);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String radius = et_radius.getText().toString().trim();
                if (!radius.isEmpty())
                {
                    int rad = Integer.parseInt(et_radius.getText().toString());
                    if (rad < 10 && rad > 1000)
                    {
                        Toast.makeText(SearchActivity.this, "Radius must be number greater then 10 and smaller then 1001", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                if (cb_culture.isChecked() || cb_cuisine.isChecked() || cb_geography.isChecked() || cb_history.isChecked() || cb_sport.isChecked())
                {
                    String categoryChecked = "";
                    if (cb_culture.isChecked())
                    {
                        categoryChecked = "Culture";
                    }
                    if (cb_cuisine.isChecked())
                    {
                        if (categoryChecked.isEmpty())
                        {
                            categoryChecked = "Cuisine";
                        }
                        else
                        {
                            categoryChecked += ",Cuisine";
                        }
                    }
                    if (cb_geography.isChecked())
                    {
                        if (categoryChecked.isEmpty())
                        {
                            categoryChecked = "Geography";
                        }
                        else
                        {
                            categoryChecked += ",Geography";
                        }
                    }
                    if (cb_history.isChecked())
                    {
                        if (categoryChecked.isEmpty())
                        {
                            categoryChecked = "History";
                        }
                        else
                        {
                            categoryChecked += ",History";
                        }
                    }
                    if (cb_sport.isChecked())
                    {
                        if (categoryChecked.isEmpty())
                        {
                            categoryChecked = "Sport";
                        }
                        else
                        {
                            categoryChecked += ",Sport";
                        }
                    }

                    String categoryCreated = et_username.getText().toString().trim();

                    Intent returnIntent = new Intent(SearchActivity.this, MainActivity.class);
                    returnIntent.putExtra("category", categoryChecked);
                    returnIntent.putExtra("radius", radius);
                    returnIntent.putExtra("categoryCreated", categoryCreated);
                    setResult(5,returnIntent);
                    finish();
                }
                else
                {
                    Toast.makeText(SearchActivity.this, "Check some category!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent returnIntent = new Intent(SearchActivity.this, MainActivity.class);
        setResult(RESULT_CANCELED,returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            Intent returnIntent = new Intent(SearchActivity.this, MainActivity.class);
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
