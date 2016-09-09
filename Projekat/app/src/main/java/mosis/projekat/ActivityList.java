package mosis.projekat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.Set;


import org.json.JSONException;
import org.json.JSONObject;


public class ActivityList extends AppCompatActivity {

    //private String ipAddress = "http:/10.10.3.188:8081";
    private String ipAddress = MainActivity.publicIpAddress;

    private GetScoreListTask mGetScoreListTask = null;
    public static final String FIRST_COLUMN = "Position";
    public static final String SECOND_COLUMN = "Username";
    public static final String THIRD_COLUMN = "Points";
    private ArrayList<HashMap<String, String>> list;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final String DEVICE_NAME = "device_name";
    private int numberOfRows = 10;
    private int pageNumber = 0;
    private int lastGoodPageNumber = 0;
    ListView listView;
    private View mProgressView;
    private String getListUserPoints = "true";

    // Za Bluetooth
    // ----------------------------------------------------------- //
    private ProcessNewFriendshipTask mProcessNewFriendshipTask = null;
    private ProcessNewTeamNameTask mProcessNewTeamNameTask = null;
    String myUsername;
    String myFriends;
    boolean bluetoothActivity = false;
    ProgressDialog progressDialog;

    private Button mSendUserID;
    TextView BtNotifyText;
    String teamName = "";
    ProjekatDBAdapter dbAdapter;

    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    //*********************************//
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private int CURRENT_REQUEST_CONNECT_DEVICE_SECURE_OR_INSECURE = REQUEST_CONNECT_DEVICE_SECURE;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mBtService = null;

    // ----------------------------------------------------------- //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        myUsername = intent.getStringExtra("myUsername");


        if (myUsername == null || myUsername == "") {
            // Prikaz liste sa poenima

            //mGameFormView.setVisibility(View.INVISIBLE);
            //mGameFormView.setVisibility(View.GONE);
            //View showListView = findViewById(R.id.score_list);
            //showListView.setVisibility(View.VISIBLE);

            myFriends = intent.getStringExtra("myFriends");

            mProgressView = findViewById(R.id.list_progress);
            listView = (ListView) findViewById(R.id.score_list);
            listView.setVisibility(View.VISIBLE);
            View mLayoutBluetooth = (View) findViewById(R.id.bluetooth_layout);
            mLayoutBluetooth.setVisibility(View.GONE);

            //list = new ArrayList<HashMap<String,String>>();


            showProgress(true);
            mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber), getListUserPoints);
            mGetScoreListTask.execute((Void) null);
        } else {
            // ********DEO ZA BLUETOOTH*********

            bluetoothActivity = true;
            mSendUserID = (Button) findViewById(R.id.send_userid);
            BtNotifyText = (TextView) findViewById(R.id.bt_notify_text);

            // Initialize array adapters. One for already paired devices and
            // one for newly discovered devices
            ArrayAdapter<String> pairedDevicesArrayAdapter =
                    new ArrayAdapter<String>(this, R.layout.device_name);
            mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

            // Find and set up the ListView for newly discovered devices
            ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
            newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
            newDevicesListView.setOnItemClickListener(mDeviceClickListener);
            listView = newDevicesListView;

            // Register for broadcasts when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(mReceiver, filter);

            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);

            // Get the local Bluetooth adapter
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();

            // If the adapter is null, then Bluetooth is not supported
            if (mBtAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                this.finish();
            }

            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // ********************************

            progressDialog = new ProgressDialog(ActivityList.this);

            dbAdapter = new ProjekatDBAdapter(getApplicationContext());
            dbAdapter.open();
            User user = dbAdapter.getTeamName(myUsername);
            dbAdapter.close();
            teamName = user.getTeamName();
        }

    }

    public class GetScoreListTask extends AsyncTask<Void, Void, String> {

        private final String fromPosition;
        private final String userPoints;

        GetScoreListTask(String FromPostion, String getUserPoints) {
            fromPosition = FromPostion;
            userPoints = getUserPoints;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("fromPosition", fromPosition));
            postParameters.add(new BasicNameValuePair("userPoints", userPoints));

            String res = null;
            try {
                res = CustomHttpClient.executeHttpPost(ipAddress + "/process_getList", postParameters);
                res = res.toString();
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
            showProgress(false);

            list = new ArrayList<HashMap<String, String>>();

            HashMap<String, String> firstTemp = new HashMap<String, String>();
            firstTemp.put(FIRST_COLUMN, FIRST_COLUMN);
            firstTemp.put(SECOND_COLUMN, SECOND_COLUMN);
            firstTemp.put(THIRD_COLUMN, THIRD_COLUMN);
            list.add(firstTemp);

            if (result.contains("Username")) {
                Gson gson = new Gson();
                JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement str = jsonArray.get(i);
                    User user = gson.fromJson(str, User.class);

                    HashMap<String, String> temp = new HashMap<String, String>();
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
            else if (result.contains("Team_name"))
            {
                Gson gson = new Gson();
                JsonArray jsonArray = new JsonParser().parse(result).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement str = jsonArray.get(i);
                    User user = gson.fromJson(str, User.class);

                    HashMap<String, String> temp = new HashMap<String, String>();
                    temp.put(FIRST_COLUMN, Integer.toString(i));
                    temp.put(SECOND_COLUMN, user.getTeamName());
                    temp.put(THIRD_COLUMN, Integer.toString(user.getPoints()));
                    list.add(temp);
                }

                lastGoodPageNumber = pageNumber;

                ListViewAdapter adapter = new ListViewAdapter(ActivityList.this, list);
                listView.setAdapter(adapter);
            }
            else if (result.contains("[]")) {
                if (pageNumber != lastGoodPageNumber) {
                    pageNumber = lastGoodPageNumber;
                    mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber), getListUserPoints);
                    mGetScoreListTask.execute((Void) null);
                } else {
                    Toast.makeText(ActivityList.this, "The list is empty!", Toast.LENGTH_LONG).show();
                }
            } else {
                // Ako ima greske da se vrati na MainAct i da izbaci Toast
                Toast.makeText(ActivityList.this, "Error with geting list!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mGetScoreListTask = null;
            showProgress(false);
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
        if (bluetoothActivity) {
            //
            getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_activity_list, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            Intent returnIntent = new Intent(ActivityList.this, MainActivity.class);
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }

        //noinspection SimplifiableIfStatement
        if (!bluetoothActivity) {
            if (isHaveInternetConnection()) {
                if (id == R.id.begining_list) {

                    if (mGetScoreListTask == null) {
                        showProgress(true);
                        pageNumber = 0;
                        mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber), getListUserPoints);
                        mGetScoreListTask.execute((Void) null);
                    }

                } else if (id == R.id.next_list) {
                    if (mGetScoreListTask == null) {
                        showProgress(true);
                        pageNumber++;
                        mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber * numberOfRows), getListUserPoints);
                        mGetScoreListTask.execute((Void) null);
                    }
                } else if (id == R.id.previus_list) {
                    if (mGetScoreListTask == null) {
                        if (pageNumber > 0) {
                            showProgress(true);
                            pageNumber--;
                            mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber * numberOfRows), getListUserPoints);
                            mGetScoreListTask.execute((Void) null);
                        }
                    }
                }
                else if (id == R.id.userOrTeam_list) {
                    pageNumber = 0;
                    lastGoodPageNumber = 0;

                    TextView textView = (TextView) findViewById(R.id.username_score_list);

                    if (getListUserPoints.equals("true")) {
                        getListUserPoints = "false";
                        textView.setText("Team name");
                        item.setTitle("Get user list");
                    } else {
                        getListUserPoints = "true";
                        textView.setText("Username");
                        item.setTitle("Get team list");
                    }

                    showProgress(true);
                    mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber), getListUserPoints);
                    mGetScoreListTask.execute((Void) null);

                }
            } else {
                Toast.makeText(getApplicationContext(), "You not have internet connection!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if (id == R.id.scan_device) {
                // SCAN BT DEVICE
                doDiscovery();
                return true;
            }
            else if (id == R.id.make_me_discoverable) {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            else if (id == R.id.secure_connection) {
                // Secure connection setup
                CURRENT_REQUEST_CONNECT_DEVICE_SECURE_OR_INSECURE = REQUEST_CONNECT_DEVICE_SECURE;
                return true;
            }
            else if (id == R.id.insecure_connection) {
                // Insecure connection setup
                CURRENT_REQUEST_CONNECT_DEVICE_SECURE_OR_INSECURE = REQUEST_CONNECT_DEVICE_INSECURE;
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (!bluetoothActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                listView.setVisibility(show ? View.GONE : View.VISIBLE);
                listView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listView.setVisibility(show ? View.GONE : View.VISIBLE);
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

            /*if (bluetoothActivity) {
                BtNotifyText.setVisibility(show ? View.GONE : View.VISIBLE);
                BtNotifyText.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        BtNotifyText.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

                mSendUserID.setVisibility(show ? View.GONE : View.VISIBLE);
                mSendUserID.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSendUserID.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            }*/
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                listView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
        else
        {
            if (show) {
                progressDialog.setTitle("Adding new friend");
                progressDialog.setMessage("Creating new friendship");
                progressDialog.show();
            }
            else
            {
                progressDialog.dismiss();
            }
        }
    }

    // Deo za Bluetooth
    // -------------------------------------------------------------------------------- //

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        mNewDevicesArrayAdapter.clear();

        // Indicate scanning in the title
        //setProgressBarIndeterminateVisibility(true);
        //setTitle(R.string.scanning);
        BtNotifyText.setText(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (bluetoothActivity) {
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBtAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else if (mBtService == null) {
                setupSendingUserID();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothActivity) {
            // Make sure we're not doing discovery anymore
            if (mBtAdapter != null) {
                mBtAdapter.cancelDiscovery();
            }

            // Unregister broadcast listeners
            this.unregisterReceiver(mReceiver);

            /***************************/

            if (mBtService != null) {
                mBtService.stop();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (bluetoothActivity) {
            // Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (mBtService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mBtService.getState() == BluetoothService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    mBtService.start();
                }
            }
        }
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            //setResult(Activity.RESULT_OK, intent);
            //finish();

            if (REQUEST_CONNECT_DEVICE_SECURE == CURRENT_REQUEST_CONNECT_DEVICE_SECURE_OR_INSECURE)
            {
                connectDevice(intent, true);
            }
            else if (REQUEST_CONNECT_DEVICE_INSECURE == CURRENT_REQUEST_CONNECT_DEVICE_SECURE_OR_INSECURE)
            {
                connectDevice(intent, false);
            }
            else
            {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                ActivityList.this.finish();
            }
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                //if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                if (mNewDevicesArrayAdapter.getPosition(device.getName() + "\n" + device.getAddress()) == -1)
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                //}
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
                //setTitle(R.string.select_device);
                BtNotifyText.setText(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*************************************************************************************************/
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    *
    *  BluetoothChatFragment
    *
    */

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Activity activity = getApplicationContext();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            BtNotifyText.setText(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            BtNotifyText.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            BtNotifyText.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(getApplicationContext(), "Handler: Message writted: " + writeMessage, Toast.LENGTH_SHORT).show();
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    createFriendship(msg);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link #EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address

        if (mBtService == null)
            setupSendingUserID();

        String address = data.getExtras()
                .getString(EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBtService.connect(device, secure);
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBtService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupSendingUserID() {
        Log.d(TAG, "setupSendingUserID()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        //mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendUserID.setVisibility(View.VISIBLE);
        mSendUserID.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                //sendMessage(userId);

                JSONObject json = new JSONObject();
                String messageForSending = "";
                try {
                    json.put("[ToServer:]", myUsername);//posaljem svoj
                    //json.put("[ToServer2:]", teamName);//posaljem svoj
                    messageForSending = json.toString();
                    sendMessage(messageForSending);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBtService = new BluetoothService(getApplicationContext(), mHandler);

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
    }

    public void createFriendship(Message msg)
    {
        byte[] readBuf = (byte[]) msg.obj;
        // construct a string from the valid bytes in the buffer
        String readMessage = new String(readBuf, 0, msg.arg1);
        Toast.makeText(ActivityList.this, "Handler: Message readed: " + readMessage, Toast.LENGTH_SHORT).show();
        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

        if (readMessage.contains("[ToServer:]")) {

            String serverUserId = "";
            try {
                JSONObject dataJson = new JSONObject(readMessage);
                serverUserId = dataJson.getString("[ToServer:]");
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ActivityList.this, "Error on parsing json from server (Bluetooth)!", Toast.LENGTH_SHORT).show();
            }

            AlertDialog myDialogBox = new AlertDialog.Builder(ActivityList.this)
                    .setTitle("Confirm friendship")
                    .setMessage(mConnectedDeviceName + " with username '" + serverUserId + "' wan't to become friend with you. Accept friendship ? ")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //MainActivity.this.sendMessage(userId);

                            JSONObject json = new JSONObject();
                            String messageForSending = "";
                            try {
                                json.put("[ToClient:]", myUsername);//posaljem svoj
                                if (teamName != null)
                                    json.put("[ToClient2:]", teamName);//posaljem svoj
                                else
                                    json.put("[ToClient2:]", "");//posaljem svoj
                                messageForSending = json.toString();
                                ActivityList.this.sendMessage(messageForSending);
                            } catch (Exception e) {
                                Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).create();
            myDialogBox.show();
        } else if (readMessage.contains("[ToClient:]"))
        {
            String serverUserId = "";
            String serverTeamName = "";
            try {
                JSONObject dataJson = new JSONObject(readMessage);
                serverUserId = dataJson.getString("[ToClient:]");
                myFriends = serverUserId;
                serverTeamName = dataJson.getString("[ToClient2:]");
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ActivityList.this, "Error on parsing json from server (Bluetooth)!", Toast.LENGTH_SHORT).show();
            }

            String finalTeamName = "";

            if (!TextUtils.isEmpty(teamName) && TextUtils.isEmpty(serverTeamName))
            {
                finalTeamName =  teamName;
            }
            else if (TextUtils.isEmpty(teamName) && !TextUtils.isEmpty(serverTeamName))
            {
                finalTeamName =  serverTeamName;
                teamName = serverTeamName;
            }
            else if (TextUtils.isEmpty(teamName) && TextUtils.isEmpty(serverTeamName))
            {
                LayoutInflater layoutInflater = LayoutInflater.from(ActivityList.this);
                View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityList.this);
                alertDialogBuilder.setView(promptView);

                final EditText editText_teamName = (EditText) promptView.findViewById(R.id.edittext_input_dialog);


                final String finalServerUserId = serverUserId;
                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String teamNameForSending = editText_teamName.getText().toString();
                                teamName = teamNameForSending;
                                if (!TextUtils.isEmpty(teamNameForSending) && teamNameForSending.length() > 3)
                                {
                                   /* showProgress(true);
                                    mProcessNewFriendshipTask = new ProcessNewFriendshipTask(myUsername, finalServerUserId, teamNameForSending);
                                    mProcessNewFriendshipTask.execute((Void) null);*/
                                    showProgress(true);
                                    mProcessNewTeamNameTask = new ProcessNewTeamNameTask(teamName);
                                    mProcessNewTeamNameTask.execute((Void) null);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Are you serious ? Try again!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
            else
            {
                finalTeamName = "";
                if (!TextUtils.isEmpty(teamName) && !TextUtils.isEmpty(serverTeamName))
                {
                    if (teamName.equals(serverTeamName))
                    { // same teams
                        JSONObject json = new JSONObject();
                        String messageForSending = "";
                        try {
                            json.put("[ToServer_createdFriendship:]", "false");//posaljem svoj
                            messageForSending = json.toString();
                            ActivityList.this.sendMessage(messageForSending);
                        } catch (Exception e) {
                            //Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                            Log.e("Bluetooth: ", "Same teams name sending msg...");
                        }

                        Toast.makeText(ActivityList.this, "You are already in the same teams!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    { // different teams
                        JSONObject json = new JSONObject();
                        String messageForSending = "";
                        try {
                            json.put("[ToServer_createdFriendship:]", "false");//posaljem svoj
                            messageForSending = json.toString();
                            ActivityList.this.sendMessage(messageForSending);
                        } catch (Exception e) {
                            //Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                            Log.e("Bluetooth: ", "Different teams sending msg...");
                        }

                        Toast.makeText(ActivityList.this, "You are in different teams! User can't be in two teams.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (!TextUtils.isEmpty(finalTeamName)) {
                final String teamNameForSending = finalTeamName;
                final String finalServerUserId = serverUserId;
                AlertDialog myDialogBox = new AlertDialog.Builder(ActivityList.this)
                        .setTitle("Confirm friendship")
                        .setMessage("Create friendship " + myUsername + "-" + serverUserId + " with team name " + finalTeamName + " ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //MainActivity.this.sendMessage(userId);

                                showProgress(true);
                                mProcessNewFriendshipTask = new ProcessNewFriendshipTask(myUsername, finalServerUserId, teamNameForSending);
                                mProcessNewFriendshipTask.execute((Void) null);

                            /*JSONObject json = new JSONObject();
                            String messageForSending = "";
                            try {
                                json.put("[ToServer_createdFriendship:]", "true");//posaljem svoj
                                messageForSending = json.toString();
                                ActivityList.this.sendMessage(messageForSending);
                            } catch (Exception e) {
                                Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                            }*/

                                //Toast.makeText(ActivityList.this, "Friendship created!", Toast.LENGTH_SHORT).show();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                JSONObject json = new JSONObject();
                                String messageForSending = "";
                                try {
                                    json.put("[ToServer_createdFriendship:]", "false");//posaljem svoj
                                    messageForSending = json.toString();
                                    ActivityList.this.sendMessage(messageForSending);
                                } catch (Exception e) {
                                    Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                                }

                                Toast.makeText(ActivityList.this, "Friendship not created!", Toast.LENGTH_SHORT).show();

                            }
                        }).create();
                myDialogBox.show();
            }
            /*else
            {
                Toast.makeText(getApplicationContext(), "Friendship not created! TeamName is empty.", Toast.LENGTH_SHORT).show();
            }*/
        }
        else if (readMessage.contains("[ToServer_createdFriendship:]"))
        {
            String serverFriendshipCreated = "";
            try {
                JSONObject dataJson = new JSONObject(readMessage);
                serverFriendshipCreated = dataJson.getString("[ToServer_createdFriendship:]");
                if (serverFriendshipCreated.contains("true"))
                {
                    teamName = dataJson.getString("[ToServer_createdFriendship:2]");
                    dbAdapter.open();
                    dbAdapter.updateTeamName(myUsername, teamName);
                    dbAdapter.close();

                    Toast.makeText(ActivityList.this, "Friendship is created!", Toast.LENGTH_SHORT).show();

                }
                else if (serverFriendshipCreated.contains("false"))
                {
                    Toast.makeText(ActivityList.this, "Friendship not created!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ActivityList.this, "Error on parsing json from server (Bluetooth)!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class ProcessNewFriendshipTask extends AsyncTask<Void, Void, String> {

        private final String mUser1;
        private final String mUser2;
        private final String mTeamName;

        ProcessNewFriendshipTask(String myUserName, String friendUsername, String teamName) {
            mUser1 = myUserName;
            mUser2 = friendUsername;
            mTeamName = teamName;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username1", mUser1));
            postParameters.add(new BasicNameValuePair("username2", mUser2));
            postParameters.add(new BasicNameValuePair("team_name", mTeamName));

            String res = null;
            try {
                res = CustomHttpClient.executeHttpPost(ipAddress + "/process_newfriendship", postParameters);
                res = res.toString();
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

            mProcessNewFriendshipTask = null;
            showProgress(false);

            if (result.contains("success"))
            {
                // ako je uspesno sklopljeno prijateljstvo

                dbAdapter.open();
                dbAdapter.updateTeamName(myUsername, teamName);
                dbAdapter.close();

                JSONObject json = new JSONObject();
                String messageForSending = "";
                try {
                    json.put("[ToServer_createdFriendship:]", "true");//posaljem svoj
                    json.put("[ToServer_createdFriendship:2]", teamName);//posaljem svoj
                    messageForSending = json.toString();
                    ActivityList.this.sendMessage(messageForSending);
                } catch (Exception e) {
                    Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(ActivityList.this, "Friendship created!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // ako nije uspesno

                JSONObject json = new JSONObject();
                String messageForSending = "";
                try {
                    json.put("[ToServer_createdFriendship:]", "false");//posaljem svoj
                    messageForSending = json.toString();
                    ActivityList.this.sendMessage(messageForSending);
                } catch (Exception e) {
                    Toast.makeText(ActivityList.this, "Error on sending friend request!", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(ActivityList.this, "Friendship not created!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            mProcessNewFriendshipTask = null;
            showProgress(false);
        }
    }


    public class ProcessNewTeamNameTask extends AsyncTask<Void, Void, String> {

        private final String mTeamName;

        ProcessNewTeamNameTask(String teamName) {
            mTeamName = teamName;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("team_name", mTeamName));

            String res = null;
            try {
                res = CustomHttpClient.executeHttpPost(ipAddress + "/process_newTeam", postParameters);
                res = res.toString();
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

            mProcessNewTeamNameTask = null;
            showProgress(false);

            if (result.contains("success"))
            {

                dbAdapter.open();
                dbAdapter.updateTeamName(myUsername, teamName);
                dbAdapter.close();

                if (myFriends != null && !TextUtils.isEmpty(myFriends)) {
                    showProgress(true);
                    mProcessNewFriendshipTask = new ProcessNewFriendshipTask(myUsername, myFriends, teamName);
                    mProcessNewFriendshipTask.execute((Void) null);
                }

            }
            else if (result.contains("otherName"))
            {
                LayoutInflater layoutInflater = LayoutInflater.from(ActivityList.this);
                View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityList.this);
                alertDialogBuilder.setView(promptView);

                final EditText editText_teamName = (EditText) promptView.findViewById(R.id.edittext_input_dialog);
                editText_teamName.setHint("Try other name");

                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String teamNameForSending = editText_teamName.getText().toString();
                                teamName = teamNameForSending;
                                if (!TextUtils.isEmpty(teamNameForSending) && teamNameForSending.length() > 3)
                                {
                                    showProgress(true);
                                    mProcessNewTeamNameTask = new ProcessNewTeamNameTask(teamName);
                                    mProcessNewTeamNameTask.execute((Void) null);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "Are you serious ? Try again!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
            else if (result.contains("teamFull"))
            {
                Toast.makeText(getApplicationContext(), "This team is full!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Error with creating team!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            mProcessNewTeamNameTask = null;
            showProgress(false);
        }
    }

}