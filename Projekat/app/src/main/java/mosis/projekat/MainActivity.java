package mosis.projekat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,  com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private String ipAddress = "http://192.168.137.225:8081";
    private  int categoryShowRadius = 250;
    private double categoryRadius = 250.0; // u metrima
    private double questionRadius = 50.0; // u metrima
    private double radiusSearchCategory = categoryRadius;
    private UpdateLocTask mAuthTask = null;
    String response = null;
    SupportMapFragment sMapFragment;
    private LatLng myLoc;
    private LatLng categoryLatLng;
    private LatLng questionLatLng;
    private GoogleMap mMap;
    private String myUsername;
    private String friendsUsernames = "";
    //FragmentManager sFm;
    private static final String TAG = "MainActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    ProjekatDBAdapter dbAdapter;
    //private ArrayList<User> friendsAvatar;
    private ArrayMap<String, Bitmap> friendsAvatar;
    private ArrayList<Marker> friendsMarkers;
    private ArrayList<Marker> categoryMarkers;
    private ArrayList<Marker> questionMarkers;
    private int numberOfQuestion = 5;
    private int questionNumber = 0;
    Questions questions;
    private View mCustomMarkerView;
    private ImageView mMarkerImageView;
    //private String ImageUrl = "";
    //private Circle mCircle;
    private int correctAnswered = 0;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ///////////////////
        mCustomMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        mMarkerImageView = (ImageView) mCustomMarkerView.findViewById(R.id.profile_image);
        //////////////////

        sMapFragment = SupportMapFragment.newInstance();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                if (questionNumber == 0)
                {
                    // Select category and number of question
                    mMap.clear();
                    if (friendsMarkers != null)
                        friendsMarkers.clear(); // TREBA PROVERITI OVO PUCA OVDE
                    categoryLatLng = myLoc;
                    Intent i = new Intent(MainActivity.this, QuestionActivity.class);
                    i.putExtra("questionNumber", Integer.toString(questionNumber));
                    startActivityForResult(i, 1);
                }
                else if ((questionNumber - 1 )< numberOfQuestion)
                {
                        // Next question
                    questionLatLng = myLoc;
                    float meters[] = new float[1];
                    Location.distanceBetween(categoryLatLng.latitude, categoryLatLng.longitude, questionLatLng.latitude, questionLatLng.longitude, meters);
                    if (meters[0] < categoryRadius)
                    {
                        // moze da se postavi pitanja
                        Intent i = new Intent(MainActivity.this, QuestionActivity.class);
                        i.putExtra("questionNumber", Integer.toString(questionNumber));
                        i.putExtra("numberOfQuestion", Integer.toString(numberOfQuestion));
                        i.putExtra("spinnerCategory", questions.getCategory());
                        startActivityForResult(i, 1);
                    }
                    else
                    {
                        // Toast to out of radius
                        Toast.makeText(MainActivity.this, "You can add question only inside radius!", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    // SEND DATA TO SERVER

                    AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Send questions to server ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // Perform Your Task Here--When Yes Is Pressed.
                                    //Toast.makeText(MainActivity.this, "Sending data....", Toast.LENGTH_SHORT).show();
                                    //dialog.cancel();
                                    Intent i = new Intent(MainActivity.this, QuestionActivity.class);
                                    i.putExtra("questionNumber", Integer.toString(questionNumber));
                                    i.putExtra("numberOfQuestion", Integer.toString(numberOfQuestion));
                                    i.putExtra("category", questions.getCategory());
                                    i.putExtra("questions", questions.getQuestions());
                                    i.putExtra("correctAnswers", questions.getCorrectAnswers());
                                    i.putExtra("wrongAnswers", questions.getWrongAnswers());
                                    i.putExtra("longitudeLatitude", questions.getLongitudeLatitude());
                                    //i.putExtra("categoryLongLat", questions.getCategoryLongLat());
                                    i.putExtra("categoryLong", questions.getLongitudeCategory());
                                    i.putExtra("categoryLat", questions.getLatitudeCategory());
                                    i.putExtra("createdUser", questions.getCreatedUser());
                                    i.putExtra("friendsUsernames", friendsUsernames);
                                    startActivityForResult(i, 1);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // Perform Your Task Here--When No is pressed
                                    dialog.cancel();
                                }
                            }).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // azuriranje username u header-u nav bara
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        View hView = navigationView.getHeaderView(0);
        TextView username_view = (TextView) hView.findViewById(R.id.username_text);
        username_view.setText(username);
        myUsername = username;
        //friendsUsernames = "";

        CheckFriends mFriendsTask;
        mFriendsTask = new CheckFriends(username);
        mFriendsTask.execute((Void) null);

        /*try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            Marker TP = googleMap.addMarker(new MarkerOptions().
                    position(TutorialsPoint).title("TutorialsPoint"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        sMapFragment.getMapAsync(this);
        FragmentManager sFm = getSupportFragmentManager();
        sFm.beginTransaction().add(R.id.map, sMapFragment).commit();
        // if (sMapFragment.isAdded())
        // {
        // sFm.beginTransaction().hide(sMapFragment).commit();

        // }
       /* if (drawer.isDrawerOpen(GravityCompat.START))
        {
            sFm.beginTransaction().hide(sMapFragment).commit();
        }
        else
        {
            sFm.beginTransaction().show(sMapFragment).commit();
        }*/


        /*// azuriranje username u header-u nav bara
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        View hView = navigationView.getHeaderView(0);
        TextView username_view = (TextView) hView.findViewById(R.id.username_text);
        username_view.setText(username);
        myUsername = username;
        //friendsUsernames = "";

        CheckFriends mFriendsTask;
        mFriendsTask = new CheckFriends(username);
        mFriendsTask.execute((Void) null);*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired.................");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected...................: " + mGoogleApiClient.isConnected());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "onResume: Location updates resumed............");
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            GetCategoryTask mCategoryTask;
            mCategoryTask = new GetCategoryTask(myUsername, friendsUsernames, "");
            //mCategoryTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            mCategoryTask.execute();
            return true;
        } else if (id == R.id.action_search_categories)
        {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivityForResult(i, 1);
        }
        else if (id == R.id.action_show_list)
        {
            Intent i = new Intent(MainActivity.this, ActivityList.class);
            startActivityForResult(i, 1);
            /*Intent i = new Intent(MainActivity.this, GameActivity.class);
            i.putExtra("showList", "true");
            startActivityForResult(i, 1);*/
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bluetooth) {
            Intent i = new Intent(MainActivity.this, ActivityList.class);
            i.putExtra("myUsername", myUsername);
            i.putExtra("myFriends", friendsUsernames);
            startActivityForResult(i, 1);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_viewprofile) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else { // Google Play Services are available

            // Enabling MyLocation Layer of Google Map
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

       /* // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else { // Google Play Services are available

            // Enabling MyLocation Layer of Google Map
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);

            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
        }*/
        // mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "Firing onLocationChanged....................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocation();

       /* // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        myLoc = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));

        // Zoom in the Google Map
        if (mAuthTask == null)
        {
            mAuthTask = new UpdateLocTask(myUsername, friendsUsernames, Double.toString(longitude),Double.toString(latitude));
            mAuthTask.execute((Void) null);
        }*/
    }

    private void updateLocation() {
        Log.d(TAG, "Update location started..................");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lon = String.valueOf(mCurrentLocation.getLongitude());
            //String accur = String.valueOf(mCurrentLocation.getAccuracy());
            //String provider = String.valueOf(mCurrentLocation.getProvider());

            // Creating a LatLng object for the current location
            myLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            // Showing the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));

            // Zoom in the Google Map
            if (mAuthTask == null) {
                mAuthTask = new UpdateLocTask(myUsername, friendsUsernames, lon, lat);
                mAuthTask.execute((Void) null);
            }

        } else {
            Log.d(TAG, "location is null.....................");
        }
    }

    /*@Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        if (friendsMarkers.contains(marker))
        {
            //Toast.makeText(MainActivity.this,marker.getTitle(),Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("friendUsername", marker.getTitle());
            intent.putExtra("longitude",marker.getPosition().longitude);
            intent.putExtra("latitude", marker.getPosition().latitude);
            startActivityForResult(intent, 1);
        }
        else if (categoryMarkers.contains(marker))
        {
            float meters[] = new float[1];
            Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude, myLoc.latitude, myLoc.longitude, meters);
            if (meters[0] < categoryRadius)
            {
                AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                        .setMessage(marker.getSnippet() + "\n\nDo you want to play game ?" )
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // User zeli da odgovara na kliknutu kategoriju
                                // Sakriti ostale markere (osim prijatelja) i prikazati samo pitanja
                                String categoryID = marker.getTitle();
                                GetQuestionsForCategoryTask mQuestionTask;
                                mQuestionTask = new GetQuestionsForCategoryTask(categoryID);
                                //mCategoryTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                mQuestionTask.execute();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Perform Your Task Here--When No is pressed
                                dialog.cancel();
                            }
                        }).show();
            }
            else
            {
                // Toast to far from category
                Toast.makeText(MainActivity.this, "You are two far from category!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (questionMarkers.contains(marker))
        {
            final String numbOfQuest = Integer.toString(numberOfQuestion);
            float meters[] = new float[1];
            Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude, myLoc.latitude, myLoc.longitude, meters);
            if (meters[0] < questionRadius)
            {
                AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Question: " + marker.getTitle() + "/" + numbOfQuest + "\n\nDo you want to answer ?" )
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // User zeli da odgovara na kliknutu kategoriju
                                // Sakriti ostale markere (osim prijatelja) i prikazati samo pitanja
                                String questID = marker.getTitle();
                                dbAdapter.open();
                                Questions questDB = dbAdapter.getQuestion(questID);
                                dbAdapter.close();
                                marker.remove(); // uklanjanje markera
                                String [] wrongAnswer123 = questDB.getWrongAnswers().split("\\|\\|");
                                Intent i = new Intent(MainActivity.this, GameActivity.class);
                                i.putExtra("question", questDB.getQuestions());
                                i.putExtra("correctAnswer", questDB.getCorrectAnswers());
                                i.putExtra("wrongAnswer1", wrongAnswer123[0]);
                                i.putExtra("wrongAnswer2", wrongAnswer123[1]);
                                i.putExtra("wrongAnswer3", wrongAnswer123[2]);
                                i.putExtra("questID", questID);
                                i.putExtra("myUsername", myUsername);
                                i.putExtra("correctAnswered", Integer.toString(correctAnswered));
                                startActivityForResult(i, 1);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Perform Your Task Here--When No is pressed
                                dialog.cancel();
                            }
                        }).show();
            }
            else
            {
                // Toast to far from category
                Toast.makeText(MainActivity.this, "You are two far from question!", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            // SOMETHING WRONG WITH MARKER
        }

        return false;
    }

    /*@Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this,marker.getTitle(),Toast.LENGTH_LONG).show();
        return true;
    }*/

    ////////////////UPDATE LOKACIJE ///////////////
    public class UpdateLocTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mFriends;
        private final String mLongitude;
        private final String mLatitude;

        UpdateLocTask(String username, String friends, String longitude, String latitude) {
            mUsername = username;
            mFriends = friends;
            mLongitude = longitude;
            mLatitude = latitude;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername ));
            postParameters.add(new BasicNameValuePair("friends", mFriends ));
            postParameters.add(new BasicNameValuePair("longitude", mLongitude ));
            postParameters.add(new BasicNameValuePair("latitude", mLatitude ));
            String res = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_updatelocation
                // 192.168.1.73:8081
                response = CustomHttpClient.executeHttpPost(ipAddress + "/process_updatelocation", postParameters);
                res=response.toString();
                res = res.trim();
                //Thread.sleep(2000);
            } catch (InterruptedException e) {
                //return false;
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                //return false;
                return "Error";
            }

            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;

            if (!result.equals("noFriends"))
            {
                // Update-lokacije prijatelja
                if (!friendsAvatar.isEmpty()) // Mozda pukne ako friendsAvatars nije inic.
                {
                    ArrayList<User> friendsLocation = new ArrayList<User>();
                    Gson gson = new Gson();
                    JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement str = jsonArray.get(i);
                        User user = gson.fromJson(str, User.class);
                        friendsLocation.add(user);
                    }

                    //String[] friendUsername = friendsUsernames.split(",");
                    for (int i=0; i<friendsLocation.size(); i++)
                    {
                        boolean markerPostoji = false;
                        for (int j=0; j<friendsMarkers.size(); j++)
                        {
                            if (friendsMarkers.get(j).getTitle().equals(friendsLocation.get(i).getUsername()))
                            {
                                markerPostoji = true;
                                Double lat = Double.parseDouble(friendsLocation.get(i).getLatitude());
                                Double lon = Double.parseDouble(friendsLocation.get(i).getLongitude());
                                LatLng friendPosition = new LatLng(lat, lon);
                                friendsMarkers.get(j).setPosition(friendPosition);
                                break;
                            }
                        }

                        if (!markerPostoji) {

                            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(((Bitmap) friendsAvatar.get(friendsLocation.get(i).getUsername())), 50, 50);
                            if (thumbnail != null) {
                                Double lat = Double.parseDouble(friendsLocation.get(i).getLatitude());
                                Double lon = Double.parseDouble(friendsLocation.get(i).getLongitude());
                                final LatLng friendPosition = new LatLng(lat, lon);
                                Marker friendMarker = mMap.addMarker(new MarkerOptions()
                                        .position(friendPosition)
                                        .title(friendsLocation.get(i).getUsername())
                                        //.snippet("Latitude: " + friendsLocation.get(i).getLatitude() + "\n" + "Longitude: " + friendsLocation.get(i).getLongitude())
                                        //.icon(BitmapDescriptorFactory.fromBitmap((Bitmap) friendsAvatar.get("user2"))));
                                        //.icon(BitmapDescriptorFactory.fromBitmap(thumbnail)));
                                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, thumbnail))));
                                friendsMarkers.add(friendMarker);
                                mMap.setOnMarkerClickListener(MainActivity.this);
                                /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker markerFriend) {
                                        //Toast.makeText(MainActivity.this,marker.getTitle(),Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                                        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("friendUsername", markerFriend.getTitle());
                                        intent.putExtra("longitude",markerFriend.getPosition().longitude);
                                        intent.putExtra("latitude", markerFriend.getPosition().latitude);
                                        startActivity(intent);
                                        return false;
                                    }
                                }); // PUCA !!!!!!!!!!*/
                               // friendsMarkers.add(friendMarker);


                            /*mMap.setOnMapClickListener(new GoogleMap.OnMarkerClickListener()
                            {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                   dbAdapter.open();
                                    if (marker.getTitle().equals("sad"))
                                    dbAdapter.close();


                                }
                            });*/
                            }
                        }

                    }
                }
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    public class GetQuestionsForCategoryTask extends AsyncTask<Void, Void, String> {

        private final String mCategoryID;

        GetQuestionsForCategoryTask(String categoryID) {
            mCategoryID = categoryID;
        }

        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("categoryID", mCategoryID ));
            String resQuestions = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_checkuser
                // 192.168.137.79:8081
                resQuestions = CustomHttpClient.executeHttpPost(ipAddress + "/process_getQuestion", postParameters);
                resQuestions = resQuestions.toString();
                resQuestions = resQuestions.trim();
            } catch (InterruptedException e) {
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }

            //return response;
            return resQuestions;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.contains("Category"))
            {
                questionMarkers = new ArrayList<Marker>();

                Gson gson = new Gson();
                JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                Questions questionsCategory = new Questions();

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_question_marker);
                Bitmap resizedIcon = Bitmap.createScaledBitmap(icon, 75, 75, false);

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement str = jsonArray.get(i);
                    questionsCategory = gson.fromJson(str, Questions.class);

                }

                correctAnswered = 0;
                questionNumber = 0;
                // ubacivanje pitanja i odgovora lokalnu u bazi
                mMap.clear();
                categoryMarkers.clear();
                addMarkersAfterClearMap();

                /*if (friendsMarkers != null)
                {
                    friendsMarkers.clear(); // TREBA PROVERITI OVO PUCA OVDE
                    friendsMarkers = new ArrayList<Marker>();
                }*/

                dbAdapter = new ProjekatDBAdapter(getApplicationContext());
                dbAdapter.open();
                //dbAdapter.getAllQuestions();
                dbAdapter.deleteAllQuestions();
                dbAdapter.insertQuestions(questionsCategory);
                numberOfQuestion = dbAdapter.getAllQuestions(); // Broj pitanja u bazi
                dbAdapter.close();

                String [] questLongLat = questionsCategory.getLongitudeLatitude().split("&&");
                for (int i =0; i < questLongLat.length; i++)
                {
                    String [] longLat = questLongLat[i].split(",");
                    Double lat = Double.parseDouble(longLat[1]);
                    Double lon = Double.parseDouble(longLat[0]);
                    LatLng questionLatLng = new LatLng(lat, lon);
                    final String questNumber = Integer.toString(i+1);
                    //final String numbOfQuest = Integer.toString(questLongLat.length);


                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(questionLatLng)
                            .title(questNumber)
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)));
                    questionMarkers.add(marker);
                    mMap.setOnMarkerClickListener(MainActivity.this);
                       /* mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(final Marker marker) {

                                float meters[] = new float[1];
                                Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude, myLoc.latitude, myLoc.longitude, meters);
                                if (meters[0] < questionRadius)
                                {
                                    AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("Question: " + marker.getTitle() + "/" + numbOfQuest + "\n\nDo you want to answer ?" )
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    // User zeli da odgovara na kliknutu kategoriju
                                                    // Sakriti ostale markere (osim prijatelja) i prikazati samo pitanja
                                                    String questID = marker.getTitle();
                                                    dbAdapter.open();
                                                    Questions questDB = dbAdapter.getQuestion(questID);
                                                    dbAdapter.close();
                                                    marker.remove(); // uklanjanje markera
                                                    String [] wrongAnswer123 = questDB.getWrongAnswers().split("\\|\\|");
                                                    Intent i = new Intent(MainActivity.this, GameActivity.class);
                                                    i.putExtra("question", questDB.getQuestions());
                                                    i.putExtra("correctAnswer", questDB.getCorrectAnswers());
                                                    i.putExtra("wrongAnswer1", wrongAnswer123[0]);
                                                    i.putExtra("wrongAnswer2", wrongAnswer123[1]);
                                                    i.putExtra("wrongAnswer3", wrongAnswer123[2]);
                                                    i.putExtra("questID", questID);
                                                    i.putExtra("myUsername", myUsername);
                                                    i.putExtra("correctAnswered", Integer.toString(correctAnswered));
                                                    startActivityForResult(i, 1);


                                                    //dialog.cancel();
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    // Perform Your Task Here--When No is pressed
                                                    dialog.cancel();
                                                }
                                            }).show();
                                }
                                else
                                {
                                    // Toast to far from category
                                    Toast.makeText(MainActivity.this, "You are two far from question!", Toast.LENGTH_LONG).show();
                                }
                                return false;
                            }
                        });*/
                }

            }

        }
    }

    public class CheckFriends extends AsyncTask<Void, Void, String> {

        private final String mUsername;;

        CheckFriends(String username) {
            mUsername = username;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername ));
            String resCheckFriends = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_getfriendship
                // 192.168.137.79:8081
                resCheckFriends = CustomHttpClient.executeHttpPost(ipAddress + "/process_getfriendship", postParameters);
                /*res=response.toString();
                res = res.trim();*/
                //Thread.sleep(2000);
            } catch (InterruptedException e) {
                //return false;
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                //return false;
                return "Error";
            }

            return resCheckFriends;
        }

        @Override
        protected void onPostExecute(String result) {
           // mAuthTask = null;

            result = result.trim();
            if (!result.equals("noFriends"))
            {
                if (result.contains("Username")) {
                    friendsMarkers = new ArrayList<Marker>();
                    // Lista prijatelja
                    // Podaci o prijateljima lokalno u bazu
                    ArrayList<User> usersBaza; // = new ArrayList<User>();
                    dbAdapter = new ProjekatDBAdapter(getApplicationContext());
                    dbAdapter.open();
                    usersBaza = dbAdapter.getAllEntriesUsernameCreated();
               /* if (usersBaza == null || usersBaza.isEmpty()) // PROVERITI OVO
                {
                    new ArrayList<User>();
                }*/
                    dbAdapter.close();

                    // Podaci o prijateljima sa servera
                    ArrayList<User> usersServer = new ArrayList<User>();
                    Gson gson = new Gson();
                    JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement str = jsonArray.get(i);
                        User user = gson.fromJson(str, User.class);
                        usersServer.add(user);
                    }

                    for (int i = 0; i < usersServer.size(); i++) {
                        // NE MOZE OVAKO //
                       /* if (usersBaza.contains(usersServer.get(i))) {
                            usersBaza.remove(usersServer.get(i));
                            usersServer.remove(i);
                            i--;
                        }*/
                        for (int j=0; j < usersBaza.size(); j++)
                        {
                            if ((usersServer.get(i).getUsername().equals(usersBaza.get(j).getUsername())) && (usersServer.get(i).getCreated().equals(usersBaza.get(j).getCreated())))
                            {
                                usersServer.remove(i);
                                usersBaza.remove(j);
                                i--;
                                break;
                            }
                        }
                    }

                    for (int i = 0; i < usersBaza.size(); i++) {
                        if (!usersBaza.get(i).getUsername().equals(myUsername)) {
                            dbAdapter.open();
                            dbAdapter.removeEntry(usersBaza.get(i).getUsername());
                            dbAdapter.close();
                        }
                    }

                    friendsAvatar = new ArrayMap<String, Bitmap>();
                    dbAdapter.open();
                    usersBaza = dbAdapter.getAllEntries();
                    dbAdapter.close();
                    for (int i = 0; i < usersBaza.size(); i++)
                    {

                        if (!usersBaza.get(i).getUsername().equals(myUsername))
                        {
                            friendsUsernames += usersBaza.get(i).getUsername() + ",";
                            Bitmap avatar = StringToBitMap(usersBaza.get(i).getImage());
                            friendsAvatar.put(usersBaza.get(i).getUsername(), avatar);
                        }

                    }
                    if (friendsUsernames.length() > 0)
                        friendsUsernames = friendsUsernames.substring(0, friendsUsernames.lastIndexOf(","));

                    for (int i = 0; i < usersServer.size(); i++) {
                        GetFriendProfile mFriendProfileTask;
                        mFriendProfileTask = new GetFriendProfile(usersServer.get(i).getUsername());
                        mFriendProfileTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                       /* try {
                            mFriendProfileTask.execute((Void) null).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        // PROVERITI OVO
                        /*try {
                            mFriendProfileTask.get(30000, TimeUnit.MILLISECONDS);
                            //Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }*/
                        //Thread.sleep(3000);
                    }

                    /*friendsAvatar = new ArrayMap<String, Bitmap>();
                    //dbAdapter = new ProjekatDBAdapter(getApplicationContext());
                    dbAdapter.open();
                    usersBaza = dbAdapter.getAllEntries();
                    dbAdapter.close();
                    for (int i = 0; i < usersBaza.size(); i++)
                    {

                        if (!usersBaza.get(i).getUsername().equals(myUsername))
                        {
                            friendsUsernames += usersBaza.get(i).getUsername() + ",";
                            Bitmap avatar = StringToBitMap(usersBaza.get(i).getImage());
                            friendsAvatar.put(usersBaza.get(i).getUsername(), avatar);
                        }

                    }
                    if (friendsUsernames.length() > 0)
                        friendsUsernames = friendsUsernames.substring(0, friendsUsernames.lastIndexOf(","));*/

                }
                else
                {
                    Toast.makeText(MainActivity.this, "Error with getting data abaout friends!", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(MainActivity.this, "You have no friends", Toast.LENGTH_SHORT).show();
            }

        }

        /*@Override
        protected void onCancelled() {
            mAuthTask = null;
        }*/
    }

    public class GetFriendProfile extends AsyncTask<Void, Void, String> {

        private final String mUsername;

        GetFriendProfile(String username) {
            mUsername = username;
        }

        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername ));
            String resFriendProfile = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_checkuser
                // 192.168.137.79:8081
                resFriendProfile = CustomHttpClient.executeHttpPost(ipAddress + "/process_getFriendProfile", postParameters);
                /*res=response.toString();
                res = res.trim();*/
                //Thread.sleep(100);
            } catch (InterruptedException e) {
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }

            //return response;
            return resFriendProfile;
        }

        @Override
        protected void onPostExecute(String result) {

            Gson gson = new Gson();
            JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
            User user = new User();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement str = jsonArray.get(i);
                user = gson.fromJson(str, User.class);
            }
            try {
                dbAdapter.open();
                dbAdapter.insertEntry(user);
                dbAdapter.close();

                if (!user.getUsername().equals(myUsername)) {
                    if (friendsUsernames.length() > 0)
                        friendsUsernames += "," + user.getUsername();
                    else
                        friendsUsernames = user.getUsername();
                    Bitmap avatar = StringToBitMap(user.getImage());
                    friendsAvatar.put(user.getUsername(), avatar);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e(e.getClass().getName(), e.getMessage(), e.getCause());
            }

        }
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

   private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    // za CIRCLE oko objekta PERIN kod !
    private void drawCircle(LatLng position, double radiusInMeters){
        //podesena vrednost za radius i za boje
        //double radiusInMeters = 500.0;
        int strokeColor = 0x0000FF; // obod radijusa
        int shadeColor = 0x44F0FFFF; //pozadina radijusa, 44 oznacava providnost!!

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor)
                .strokeColor(strokeColor).strokeWidth(2);
        Circle mCircle;
        mCircle = mMap.addCircle(circleOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_FIRST_USER)
            {
                questionNumber = Integer.parseInt(data.getStringExtra("questionNumber").trim());
                String spinnerSelection = data.getStringExtra("spinnerCategory").trim();
                numberOfQuestion = Integer.parseInt(data.getStringExtra("numberOfQuestion").trim());
                questions = new Questions();
                questions.setCategory(spinnerSelection);
                questions.setCreatedUser(myUsername);
                //questions.setCategoryLongLat(Double.toString(categoryLatLng.longitude) + "," + Double.toString(categoryLatLng.latitude));
                questions.setLongitudeCategory(Double.toString(categoryLatLng.longitude));
                questions.setLatitudeCategory(Double.toString(categoryLatLng.latitude));
                drawCircle(categoryLatLng, categoryRadius);
                Toast.makeText(MainActivity.this, "Post questions on desired locations, near this location... ", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == Activity.RESULT_OK){
                if (questions.Questions.isEmpty())
                {
                    questionNumber = Integer.parseInt(data.getStringExtra("questionNumber").trim());
                    questions.setQuestions(data.getStringExtra("question"));
                    questions.setCorrectAnswers(data.getStringExtra("correctAnswer"));
                    questions.setWrongAnswers(data.getStringExtra("wrongAnswer"));
                    questions.setLongitudeLatitude(Double.toString(questionLatLng.longitude) + "," + Double.toString(questionLatLng.latitude));
                }
                else
                {
                    String separate = "&&";
                    questionNumber = Integer.parseInt(data.getStringExtra("questionNumber").trim());
                    questions.setQuestions(questions.getQuestions() + separate + data.getStringExtra("question"));
                    questions.setCorrectAnswers(questions.getCorrectAnswers() + separate + data.getStringExtra("correctAnswer"));
                    questions.setWrongAnswers(questions.getWrongAnswers() + separate + data.getStringExtra("wrongAnswer"));
                    questions.setLongitudeLatitude(questions.getLongitudeLatitude() + separate + Double.toString(questionLatLng.longitude) + "," + Double.toString(questionLatLng.latitude));
                }

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_question_marker);
                Bitmap resizedIcon = Bitmap.createScaledBitmap(icon, 75, 75, false);

                Marker friendMarker = mMap.addMarker(new MarkerOptions()
                            .position(questionLatLng)
                            .title(Integer.toString(questionNumber-1))
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)));
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            Intent i = new Intent(MainActivity.this, QuestionActivity.class);
                            i.putExtra("question", questions.getQuestions());
                            i.putExtra("correctAnswer", questions.getCorrectAnswers());
                            i.putExtra("wrongAnswer", questions.getWrongAnswers());
                            i.putExtra("questionClicked", marker.getTitle());
                            i.putExtra("questionNumber", Integer.toString(questionNumber));
                            i.putExtra("numberOfQuestion", Integer.toString(numberOfQuestion));
                            i.putExtra("spinnerCategory", questions.getCategory());
                            startActivityForResult(i, 1);
                            return false;
                        }
                    });

                /*BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_question_marker);
                mMap.addMarker(new MarkerOptions().position(questionLatLng).title(Integer.toString(questionNumber-1)).icon(icon)).showInfoWindow();*/

                // primanje objekta User
               /* Bundle bundle = data.getExtras();
                if (bundle != null) {
                    User user = (User) bundle.getSerializable("result");
                   // User user = (User) getIntent().getSerializableExtra("result");
                    //Integer beta = 0;
                }*/
            }
            else if (resultCode == 3) {
                String status = data.getStringExtra("status");
                if (status.equals("correct"))
                {
                    // obrisi markere sa pitanjima
                    if (questionMarkers != null)
                        questionMarkers.clear();
                    mMap.clear(); // TREBA OPET POSTAVITI SVE MARKERE
                    addMarkersAfterClearMap();
                    /*if (friendsMarkers != null)
                        friendsMarkers.clear(); // TREBA PROVERITI OVO PUCA OVDE*/
                    questionNumber = 0;
                    numberOfQuestion = 5;
                    //mCircle = null;
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Error with sending questions on server.", Toast.LENGTH_SHORT).show();
                }
            }
            else if (resultCode == 2)
            {
                questions.setQuestions(data.getStringExtra("question"));
                questions.setCorrectAnswers(data.getStringExtra("correctAnswer"));
                questions.setWrongAnswers(data.getStringExtra("wrongAnswer"));
            }
            else if (resultCode == 4)
            {
                String status = data.getStringExtra("status");
                questionNumber++;
                if (status.equals("correct"))
                {
                    correctAnswered++;
                }

                if (questionNumber == numberOfQuestion)
                {
                    //Toast.makeText(MainActivity.this, "Vrati markere za Kategorije!", Toast.LENGTH_LONG).show();
                    mMap.clear();
                    if (questionMarkers != null)
                        questionMarkers.clear();
                    addMarkersAfterClearMap();
                    GetCategoryTask mCategoryTask;
                    mCategoryTask = new GetCategoryTask(myUsername, friendsUsernames, "");
                    //mCategoryTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    mCategoryTask.execute();
                }
            }

            else if (resultCode == Activity.RESULT_CANCELED)
            {

            }
            else if (resultCode == 5) // SEARCH ACTIVITY
            {
                String categorySearch = data.getStringExtra("category");
                String radiusSearch = data.getStringExtra("radius");
                String userCreatedSearch = data.getStringExtra("categoryCreated");

                if (radiusSearch.isEmpty())
                {
                    radiusSearch = Integer.toString(categoryShowRadius); // default da stavim
                }

                radiusSearchCategory = Double.parseDouble(radiusSearch);


                String [] categorySplit = categorySearch.split(",");
                String query = "SELECT ID,Category,LongitudeCategory,LatitudeCategory,CreatedUser from CategoryQuestions where ";

                for (int i = 0; i < categorySplit.length; i++)
                {
                    query += " Category='" + categorySplit[i] + "' OR";
                }
                query = query.substring(0,query.length() - 2);

                if (!userCreatedSearch.isEmpty())
                {
                    query += " AND CreatedUser='" + userCreatedSearch + "' ";
                }

                double [] boundingBox = getBoundingBox(myLoc.latitude, myLoc.longitude, Integer.parseInt(radiusSearch));

                query += " AND LatitudeCategory > '" + Double.toString(boundingBox[0]) + "' ";
                query += "AND LongitudeCategory > '" + Double.toString(boundingBox[1]) + "' ";
                query += "AND LatitudeCategory < '" + Double.toString(boundingBox[2]) + "' ";
                query += "AND LongitudeCategory < '" + Double.toString(boundingBox[3]) + "' ";

                GetCategoryTask mCategoryTask;
                mCategoryTask = new GetCategoryTask(myUsername, friendsUsernames, query);
                //mCategoryTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                mCategoryTask.execute();
            }
        }
    }

    public class GetCategoryTask extends AsyncTask<Void, Void, String> {

        //private final String myLong;
        //private final String myLat;
        private final String myUsername;
        private final String myFriendsUsernames;
        private final String readyQuery;

        GetCategoryTask(String username, String friendUsernames, String query) {
            myUsername = username;
            myFriendsUsernames = friendUsernames;
            readyQuery = query;
           /* myLong = longitude;
            myLat = latitude;*/
        }

        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("myUsername", myUsername ));
            postParameters.add(new BasicNameValuePair("friendsUsernames", myFriendsUsernames ));
            postParameters.add(new BasicNameValuePair("readyQuery", readyQuery ));
            /*postParameters.add(new BasicNameValuePair("myLong", myLong ));
            postParameters.add(new BasicNameValuePair("myLat", myLat ));*/
            String resCategory = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_checkuser
                // 192.168.137.79:8081
                resCategory = CustomHttpClient.executeHttpPost(ipAddress + "/process_getCategory", postParameters);
                resCategory=resCategory.toString();
                resCategory = resCategory.trim();
                //Thread.sleep(100);
            } catch (InterruptedException e) {
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }

            //return response;
            return resCategory;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.contains("Category"))
            {
                if (categoryMarkers != null)
                {
                    mMap.clear();
                    categoryMarkers.clear();
                    addMarkersAfterClearMap();
                }

                categoryMarkers = new ArrayList<Marker>();

                Gson gson = new Gson();
                JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                Questions questionsCategory = new Questions();

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_question_marker);
                Bitmap resizedIcon = Bitmap.createScaledBitmap(icon, 75, 75, false);

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement str = jsonArray.get(i);
                    questionsCategory = gson.fromJson(str, Questions.class);

                    Double lat = Double.parseDouble(questionsCategory.getLatitudeCategory());
                    Double lon = Double.parseDouble(questionsCategory.getLongitudeCategory());
                    LatLng categoryLatLng = new LatLng(lat, lon);

                    float meters[] = new float[1];
                    Location.distanceBetween(lat, lon, myLoc.latitude, myLoc.longitude, meters);
                    if (meters[0] <= radiusSearchCategory) {

                        final String category = questionsCategory.getCategory();
                        final String userCreated = questionsCategory.getCreatedUser();
                        Marker markerCategory = mMap.addMarker(new MarkerOptions()
                                .position(categoryLatLng)
                                .title(questionsCategory.getID())
                                .snippet("Category: " + category + "\n" + "Created by " + userCreated)
                                .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)));
                        categoryMarkers.add(markerCategory);

                        mMap.setOnMarkerClickListener(MainActivity.this);
                    /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(final Marker mQuest) {

                            float meters[] = new float[1];
                            Location.distanceBetween(mQuest.getPosition().latitude, mQuest.getPosition().longitude, myLoc.latitude, myLoc.longitude, meters);
                            if (meters[0] < categoryRadius)
                            {
                                AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("Category: " + category + "\n" + "Created by " + userCreated + "\n\nDo you want to play game ?" )
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                // User zeli da odgovara na kliknutu kategoriju
                                                // Sakriti ostale markere (osim prijatelja) i prikazati samo pitanja
                                                String categoryID = mQuest.getTitle();
                                                GetQuestionsForCategory mQuestionTask;
                                                mQuestionTask = new GetQuestionsForCategory(categoryID);
                                                //mCategoryTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                                mQuestionTask.execute();
                                                //dialog.cancel();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                // Perform Your Task Here--When No is pressed
                                                dialog.cancel();
                                            }
                                        }).show();
                            }
                            else
                            {
                                // Toast to far from category
                                Toast.makeText(MainActivity.this, "You are two far from category!", Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    });*/
                    }
                }
            }
        }
    }

    public static double[] getBoundingBox(final double pLatitude, final double pLongitude, final int pDistanceInMeters) {

        final double[] boundingBox = new double[4];

        final double latRadian = Math.toRadians(pLatitude);

        final double degLatKm = 110.574235;
        final double degLongKm = 110.572833 * Math.cos(latRadian);
        final double deltaLat = pDistanceInMeters / 1000.0 / degLatKm;
        final double deltaLong = pDistanceInMeters / 1000.0 /
                degLongKm;

        final double minLat = pLatitude - deltaLat;
        final double minLong = pLongitude - deltaLong;
        final double maxLat = pLatitude + deltaLat;
        final double maxLong = pLongitude + deltaLong;

        boundingBox[0] = minLat;
        boundingBox[1] = minLong;
        boundingBox[2] = maxLat;
        boundingBox[3] = maxLong;

        return boundingBox;
    }

    private void addMarkersAfterClearMap()
    {
        if (friendsMarkers != null)
        {
            for (int i = 0; i < friendsMarkers.size(); i++)
            {
                Marker marker = friendsMarkers.get(i);

                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(((Bitmap) friendsAvatar.get(marker.getTitle())), 50, 50);

                Marker friendMarker = mMap.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, thumbnail))));

                friendsMarkers.set(i, friendMarker);

                mMap.setOnMarkerClickListener(MainActivity.this);
            }
        }

        if (categoryMarkers != null)
        {
            for (int i = 0; i < categoryMarkers.size(); i++)
            {
                Marker marker = categoryMarkers.get(i);

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_question_marker);
                Bitmap resizedIcon = Bitmap.createScaledBitmap(icon, 75, 75, false);

                Marker markerCategory = mMap.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .snippet(marker.getSnippet())
                        .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)));

                categoryMarkers.set(i, markerCategory);

                mMap.setOnMarkerClickListener(MainActivity.this);
            }

        }

        /*if (questionMarkers != null)
        {
            for (int i = 0; i < questionMarkers.size(); i++)
            {
                Marker marker = questionMarkers.get(i);

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_question_marker);
                Bitmap resizedIcon = Bitmap.createScaledBitmap(icon, 75, 75, false);

                Marker questMarker = mMap.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)));


                questionMarkers.add(marker);


               // mMap.setOnMarkerClickListener(MainActivity.this);

            }
        }*/
    }
}
