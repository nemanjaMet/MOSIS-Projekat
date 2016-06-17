package mosis.projekat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,  com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private UpdateLocTask mAuthTask = null;
    String response = null;
    SupportMapFragment sMapFragment;
    private LatLng myLoc;
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

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sMapFragment = SupportMapFragment.newInstance();

        setContentView(R.layout.activity_main);
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
        //nemco probaj ovde da napravis da hajduje mapicku!!
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
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
                // 192.168.137.79:8081
                response = CustomHttpClient.executeHttpPost("http://192.168.0.103:8081/process_updatelocation", postParameters);
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

            if (!result.equals("nofriends"))
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
                                LatLng friendPosition = new LatLng(lat, lon);
                                Marker friendMarker = mMap.addMarker(new MarkerOptions()
                                        .position(friendPosition)
                                        .title(friendsLocation.get(i).getUsername())
                                        //.snippet("Latitude: " + friendsLocation.get(i).getLatitude() + "\n" + "Longitude: " + friendsLocation.get(i).getLongitude())
                                        //.icon(BitmapDescriptorFactory.fromBitmap((Bitmap) friendsAvatar.get("user2"))));
                                        .icon(BitmapDescriptorFactory.fromBitmap(thumbnail)));
                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        //Toast.makeText(MainActivity.this,marker.getTitle(),Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                                        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        i.putExtra("friendUsername", marker.getTitle());
                                        i.putExtra("longitude",marker.getPosition().longitude);
                                        i.putExtra("latitude", marker.getPosition().latitude);
                                        startActivity(i);
                                        return false;
                                    }
                                }); // PUCA !!!!!!!!!!
                                friendsMarkers.add(friendMarker);
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
            String res = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_getfriendship
                // 192.168.137.79:8081
                res = CustomHttpClient.executeHttpPost("http://192.168.0.103:8081/process_getfriendship", postParameters);
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

            return res;
        }

        @Override
        protected void onPostExecute(String result) {
           // mAuthTask = null;

            result = result.trim();
            if (!result.equals("nofriends"))
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
            String res = null;
            try {
                // Simulate network access. // http://192.168.0.103:8081/process_checkuser
                // 192.168.137.79:8081
                res = CustomHttpClient.executeHttpPost("http://192.168.0.103:8081/process_getFriendProfile", postParameters);
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
            return res;
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

}
