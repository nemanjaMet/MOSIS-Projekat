package mosis.projekat;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServiceSendMyLoc extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String ipAddress = "http://192.168.137.225:8081";
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    String myUsername;
    String friendsUsernames;
    private LatLng myLoc;
    private UpdateLocTask mAuthTask = null;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private int radiusNearLocation = 100; // meters


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
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){

        myUsername = intent.getStringExtra("myUsername");
        friendsUsernames = intent.getStringExtra("friendsUsernames");

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
        //updateLocation();
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

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
