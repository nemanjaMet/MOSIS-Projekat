package mosis.projekat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityList extends AppCompatActivity {

    private String ipAddress = "http://192.168.137.225:8081";

    private GetScoreListTask mGetScoreListTask = null;
    public static final String FIRST_COLUMN="Position";
    public static final String SECOND_COLUMN="Username";
    public static final String THIRD_COLUMN="Points";
    private ArrayList<HashMap<String, String>> list;
    private int numberOfRows = 10;
    private int pageNumber = 0;
    private int lastGoodPageNumber = 0;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //mGameFormView.setVisibility(View.INVISIBLE);
        //mGameFormView.setVisibility(View.GONE);
        //View showListView = findViewById(R.id.score_list);
        //showListView.setVisibility(View.VISIBLE);

        listView=(ListView)findViewById(R.id.score_list);
        // listView.setVisibility(View.VISIBLE);

        //list = new ArrayList<HashMap<String,String>>();


        //showProgress(true);
        mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber));
        mGetScoreListTask.execute((Void) null);

    }

    public class GetScoreListTask extends AsyncTask<Void, Void, String> {

        private final String fromPosition;

        GetScoreListTask(String FromPostion) {
            fromPosition = FromPostion;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("fromPosition", fromPosition ));

            String res = null;
            try {
                res = CustomHttpClient.executeHttpPost(ipAddress + "/process_getList", postParameters);
                res=res.toString();
                res = res.trim();
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
            mGetScoreListTask = null;
            //showProgress(false);

            list = new ArrayList<HashMap<String,String>>();

            HashMap<String,String> firstTemp = new HashMap<String, String>();
            firstTemp.put(FIRST_COLUMN, FIRST_COLUMN);
            firstTemp.put(SECOND_COLUMN, SECOND_COLUMN);
            firstTemp.put(THIRD_COLUMN, THIRD_COLUMN);
            list.add(firstTemp);

            if (result.contains("Username"))
            {
                Gson gson = new Gson();
                JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement str = jsonArray.get(i);
                    User user = gson.fromJson(str, User.class);

                    HashMap<String,String> temp = new HashMap<String, String>();
                    temp.put(FIRST_COLUMN, Integer.toString(i));
                    temp.put(SECOND_COLUMN, user.getUsername());
                    temp.put(THIRD_COLUMN, Integer.toString(user.getPoints()));
                    list.add(temp);
                }

                lastGoodPageNumber = pageNumber;

                ListViewAdapter adapter = new ListViewAdapter(ActivityList.this, list);
                listView.setAdapter(adapter);


                /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
                    {
                        int pos=position+1;
                        Toast.makeText(GameActivity.this, Integer.toString(pos)+" Clicked", Toast.LENGTH_SHORT).show();
                    }

                });*/
            }
            else if (result.contains("[]"))
            {
                if (pageNumber != lastGoodPageNumber) {
                    pageNumber = lastGoodPageNumber;
                    mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber));
                    mGetScoreListTask.execute((Void) null);
                }
                else
                {
                    Toast.makeText(ActivityList.this, "The list is empty!", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                // Ako ima greske da se vrati na MainAct i da izbaci Toast
                Toast.makeText(ActivityList.this, "Error with geting list!", Toast.LENGTH_LONG).show();
    }
}

        @Override
        protected void onCancelled() {
            mGetScoreListTask = null;
            //showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
            Intent returnIntent = new Intent(ActivityList.this, MainActivity.class);
            setResult(RESULT_CANCELED, returnIntent);
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.begining_list) {
            pageNumber = 0;
            mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber));
            mGetScoreListTask.execute((Void) null);
        } else if (id == R.id.next_list)
        {
            pageNumber++;
            mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber * numberOfRows));
            mGetScoreListTask.execute((Void) null);
        }
        else if (id == R.id.previus_list)
        {
          if (pageNumber > 0)
          {
              pageNumber--;
              mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber * numberOfRows));
              mGetScoreListTask.execute((Void) null);
          }
        }

        return super.onOptionsItemSelected(item);
    }

}
