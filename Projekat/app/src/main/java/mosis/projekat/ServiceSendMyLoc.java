package mosis.projekat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiceSendMyLoc extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static Boolean serviceRunning = false;
    //private String ipAddress = "http:/10.10.3.188:8081";
    private String ipAddress = MainActivity.publicIpAddress;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    String myUsername;
    String friendsUsernames;
    private LatLng myLoc;
    private UpdateLocTask mAuthTask = null;
    private static final long INTERVAL = 1000 * 30;
    private static final long FASTEST_INTERVAL = 1000 * 20;
    private int radiusNearLocation = 100; // meters
    private LatLng myLastLocNotification;
    private ArrayList<String> myLastFriendsLoc;
    boolean firstTime = true;


    public ServiceSendMyLoc() {
    }

    @Override
    public void onCreate() {
        if (isGooglePlayServicesAvailable()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            createLocationRequest();
            mGoogleApiClient.connect(); // OVO JE OVDE BILO NA DRUGOM SAJTU
        }

        myLastFriendsLoc =  new ArrayList<String>();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){

        myUsername = intent.getStringExtra("myUsername");
        friendsUsernames = intent.getStringExtra("friendsUsernames");

        serviceRunning = true;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return TODO;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this); // OVO JE OVDE BILO NA DRUGOM SAJTU*/
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            return false;
        }
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
        Log.d("Service: ", "Location update started ..............: ");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Service: ", "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Service: ", "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        if (isHaveInternetConnection())
            updateLocation();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Service: ", "Connection failed: " + connectionResult.toString());
    }

    private void updateLocation() {
        Log.d("Service: ", "Update location started..................");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lon = String.valueOf(mCurrentLocation.getLongitude());

            // Creating a LatLng object for the current location
            myLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            String query = "SELECT ID,Category,LongitudeCategory,LatitudeCategory,CreatedUser from CategoryQuestions where ";

            double [] boundingBox = MainActivity.getBoundingBox(myLoc.latitude, myLoc.longitude, radiusNearLocation);

            query += "LatitudeCategory > '" + Double.toString(boundingBox[0]) + "' ";
            query += "AND LongitudeCategory > '" + Double.toString(boundingBox[1]) + "' ";
            query += "AND LatitudeCategory < '" + Double.toString(boundingBox[2]) + "' ";
            query += "AND LongitudeCategory < '" + Double.toString(boundingBox[3]) + "' ";

            // Zoom in the Google Map
            if (mAuthTask == null) {
                mAuthTask = new UpdateLocTask(myUsername, friendsUsernames, lon, lat, query);
                mAuthTask.execute((Void) null);
            }

        } else {
            Log.d("Service: ", "location is null.....................");
        }
    }

    ////////////////UPDATE LOKACIJE ///////////////
    public class UpdateLocTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mFriends;
        private final String mLongitude;
        private final String mLatitude;
        private final String mQueryNearCategory;

        UpdateLocTask(String username, String friends, String longitude, String latitude, String query) {
            mUsername = username;
            mFriends = friends;
            mLongitude = longitude;
            mLatitude = latitude;
            mQueryNearCategory = query;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername ));
            postParameters.add(new BasicNameValuePair("friends", mFriends ));
            postParameters.add(new BasicNameValuePair("longitude", mLongitude ));
            postParameters.add(new BasicNameValuePair("latitude", mLatitude ));
            postParameters.add(new BasicNameValuePair("queryNearCategory", mQueryNearCategory ));
            String res = null;
            try {
                res = CustomHttpClient.executeHttpPost(ipAddress + "/process_updatelocation", postParameters);
                res = res.toString();
                res = res.trim();
            } catch (InterruptedException e) {
                return "Error";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;

            // 1. Ako si blizu prijatelja izbaci notifikaciju
            // 2. Aki si blizu kategorije izbaci notifikaciju
            /*if (!result.equals("noFriends"))
            {

            }*/
            if (result.contains("|&&|"))
            {
                String [] friendsCategories = result.split("\\|&&\\|");

                if (friendsCategories[0].contains("Username"))
                {

                    Gson gson = new Gson();
                    JsonArray jsonArray = new JsonParser().parse(friendsCategories[0]).getAsJsonArray();
                    User user = new User();

                    if (myLastFriendsLoc != null && myLastFriendsLoc.size() > 0)
                    {
                        for (int i=0; i < myLastFriendsLoc.size() && i > -1; i++)
                        {
                            if (!friendsCategories[0].contains('"' + myLastFriendsLoc.get(i) + '"'))
                            {
                                myLastFriendsLoc.remove(i);
                                i--;
                            }
                        }
                    }

                    boolean showNotification = false;

                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement str = jsonArray.get(i);
                        user = gson.fromJson(str, User.class);

                        Double lat = Double.parseDouble(user.getLatitude());
                        Double lon = Double.parseDouble(user.getLongitude());

                        float meters[] = new float[1];
                        Location.distanceBetween(lat, lon, myLoc.latitude, myLoc.longitude, meters);
                        if (meters[0] <= radiusNearLocation) {
                            // Near Friend
                            //break;

                            if (myLastFriendsLoc != null && myLastFriendsLoc.size() > 0)
                            {
                                if (!myLastFriendsLoc.contains(user.getUsername()))
                                {
                                    showNotification = true;
                                    myLastFriendsLoc.add(user.getUsername());
                                }
                            }
                            else
                            {
                                showNotification = true;
                                myLastFriendsLoc.add(user.getUsername());

                            }


                        }
                    }

                    if (showNotification)
                    {
                        // Prikazi notifikaciju
                        showNotification("Near your friend!", "Click to see what friend", R.mipmap.ic_launcher_friends);
                    }

                }
                else
                {
                    if (myLastFriendsLoc != null && myLastFriendsLoc.size() > 0)
                    {
                        for (int i=0; i < myLastFriendsLoc.size() && i > -1; i++)
                        {
                            if (!friendsCategories[0].contains('"' + myLastFriendsLoc.get(i) + '"'))
                            {
                                myLastFriendsLoc.remove(i);
                                i--;
                            }
                        }
                    }
                }

                if (friendsCategories[1].contains("Category"))
                {
                    Gson gson = new Gson();
                    JsonArray jsonArray = new JsonParser().parse(friendsCategories[1]).getAsJsonArray();
                    Questions questionsCategory = new Questions();

                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement str = jsonArray.get(i);
                        questionsCategory = gson.fromJson(str, Questions.class);

                        Double lat = Double.parseDouble(questionsCategory.getLatitudeCategory());
                        Double lon = Double.parseDouble(questionsCategory.getLongitudeCategory());
                        //LatLng categoryLatLng = new LatLng(lat, lon);

                        float meters[] = new float[1];
                        Location.distanceBetween(lat, lon, myLoc.latitude, myLoc.longitude, meters);
                        if (meters[0] <= radiusNearLocation) {
                            // Near categories
                            if (myLastLocNotification == null)
                            {
                                // prikazi notifikaciju
                                myLastLocNotification = myLoc;
                                showNotification("Near category!", "Click to see what category", R.drawable.ic_question_marker);
                                break;
                            }
                            else
                            {
                                Location.distanceBetween(myLastLocNotification.latitude, myLastLocNotification.longitude, myLoc.latitude, myLoc.longitude, meters);
                                if (2 * meters[0] > radiusNearLocation) // krug na krug
                                {
                                    // prikazi notifikaciju
                                    myLastLocNotification = myLoc;
                                    showNotification("Near category!", "Click to see what category", R.drawable.ic_question_marker);
                                    break;
                                }
                            }


                        }
                    }
                }
            }
            firstTime = false;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    @Override
    public void onDestroy()
    {
        serviceRunning = false;
        mGoogleApiClient.disconnect();
    }

    public void showNotification(String title, String text, int icon)
    {
        if (!firstTime) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(icon) // notification icon
                    .setContentTitle(title) // title for notification
                    .setContentText(text) // message for notification
                    .setAutoCancel(true); // clear notification after click

            mBuilder.setDefaults(Notification.DEFAULT_ALL);


            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("username", myUsername);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }


    public boolean isHaveInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean NisConnected = activeNetwork != null && activeNetwork.isConnected();
        if (NisConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
            else
                return false;
        }
        return false;
    }

}
