package mosis.projekat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ActivityList extends AppCompatActivity implements AdapterView.OnItemClickListener {

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

    // Za Bluetooth
    // ----------------------------------------------------------- //
    ArrayAdapter<String> listAdapter; //adapter koji je u pozadini listViewa!!!!
    Button connectNew;
    BluetoothAdapter btAdapter; //bluetooth adapter na lokalnom uredjaju. Preko njega se zovu funkcije za bluetooth!!!
    Set<BluetoothDevice> devicesArray;//niz uredjaja sa kojima je fon uparen!!!
    ArrayList<String> pairedDevices;//niz imena uredjaja
    ArrayList<BluetoothDevice> devices;
    boolean clicked = false;
    IntentFilter filter; //filter za broadcast Reciever
    BroadcastReceiver reciver;// i reciver za istu funkciju kao filter!!!!
    //private ArrayList addressList;
    String tag = "debugging";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    String myUsername;
    String myFriends;
    boolean bluetoothActivity = false;
    // ----------------------------------------------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        myUsername = intent.getStringExtra("myUsername");

        if (myUsername == null || myUsername == "") {
            // Prikaz liste sa poenima

            //mGameFormView.setVisibility(View.INVISIBLE);
            //mGameFormView.setVisibility(View.GONE);
            //View showListView = findViewById(R.id.score_list);
            //showListView.setVisibility(View.VISIBLE);

            myFriends = intent.getStringExtra("myFriends");

            listView = (ListView) findViewById(R.id.score_list);
            listView.setVisibility(View.VISIBLE);
            View mLayoutBluetooth = (View) findViewById(R.id.bluetooth_layout);
            mLayoutBluetooth.setVisibility(View.GONE);

            //list = new ArrayList<HashMap<String,String>>();


            //showProgress(true);
            mGetScoreListTask = new GetScoreListTask(Integer.toString(pageNumber));
            mGetScoreListTask.execute((Void) null);
        }
        else
        {
            // Deo za Bluetooth
            bluetoothActivity = true;
            invalidateOptionsMenu();
            init();
            if (btAdapter == null) {//provera da li bluetooth uposte postoji na uredjaju!!!!
                Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_LONG).show();//ako ne postoji obavestava se korisnik
                finish();
            } else {
                if (!btAdapter.isEnabled()) {//Provera da li je BT ukljucen!!!!
                    //turnOnBT();
                    discoverableBT();// da li je uredjaj vidljiv treba da se testira!!!!
                }

                getPairedDevices(); //funkcija za proveru uparenih uredjaja!!!
                connectNew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        devices.clear();
                        listAdapter.clear();
                        startDiscovery();
                        clicked = true;
                    }
                });

            }
        }

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
        if (bluetoothActivity)
        {
            //
        }
        else
        {
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

    // Deo za Bluetooth
    // -------------------------------------------------------------------------------- //

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    //Do something
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";// ovo saljemo preko handlera!!
                    connectedThread.write(s.getBytes());
                    Log.i(tag, "connected");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }
    // pita korisnika da li da postavi uredjaj da bude vidljiv??? treba da se testira!!!
    private void discoverableBT() {
        Intent discoverableIntent = new Intent(btAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(btAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);
    }

    //private void turnOnBT() {//Funkcija kojom se ukljucuje BT!!!
    //Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    //startActivityForResult(intent, 1);
    //}
    //proverava se da li vec postoje uredjaji u memoriji telefona sa kojima je taj fon uparen!!!!!
    private void getPairedDevices() {
        devicesArray = btAdapter.getBondedDevices();
        //addressList = new ArrayList();
        if (devicesArray.size() > 0) {
            for (BluetoothDevice device : devicesArray) {
                pairedDevices.add(device.getName());
                //addressList.add(device.getAddress());
            }
        }
    }

    private void init() {
        connectNew = (Button) findViewById(R.id.bConnectNew);
        listView = (ListView) findViewById(R.id.listView_bluetooth);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();//inicijalizacija bluetooth adaptera!!
        pairedDevices = new ArrayList<String>();
        //addressList = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        reciver = new BroadcastReceiver() {//Skenira okolinu fona i trazi druge uredjaje sa ukljucenim bluetoothom!!!!
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //ako naso neki uredjaj sa upaljenim bluetoothom proverava da li je u paired devices onda pise paired!!!
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {// proverava se koja se akcija desila tako sto se uporedjuje sa stringom action!!!!
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    //if naredba da vec jednom nadjeni uredjaji ne budu dodati opet
                    //pairedDevices.add(device.getName());
                    //addressList.add(device.getAddress());
                    String s = "";
                    for (int n = 0; n < pairedDevices.size(); n++) {
                        if (device.getName().equals(pairedDevices.get(n))) {
                            //append

                            s = "(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());

                    //primer funkcije1!!!
                    //public void clearAdapter()
                    //{
                    //  deviceNames.clear();
                    // selected.clear();
                    //notifyDataSetChanged();
                    //}

                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Toast.makeText(getApplicationContext(), "Searching for devices!!!", Toast.LENGTH_LONG).show();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Toast.makeText(getApplicationContext(), "Search finished", Toast.LENGTH_LONG).show();


                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if (btAdapter.getState() == btAdapter.STATE_OFF) {
                        //turnOnBT();
                        discoverableBT();
                    }
                }
            }
        };

        registerReceiver(reciver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(reciver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(reciver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(reciver, filter);
    }

    //kad te pita app dal da ukljucis bluetooth tad je pauzirana!!!
    @Override
    protected void onPause() {
        if (bluetoothActivity) {
            if (clicked) {
                btAdapter.cancelDiscovery();
                Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
                super.onPause();
                // unregisterReceiver(reciver);
            } else {
                Toast.makeText(this, "Paused else", Toast.LENGTH_SHORT).show();
                super.onPause();
                unregisterReceiver(reciver);// da ne bi crashovala app mora da se reciever unregistruje
            }
        }
        else
        {
            super.onPause(); // OVO NIJE BILO PROVERITI
        }

    }

    @Override
    protected void onStop() {
        if (bluetoothActivity) {
            //if naredba da se predstavi uslov i kad je kliknuto i kad nije na dugme za pretragu!!!!!!
            if (clicked) {
                btAdapter.cancelDiscovery();
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
                super.onStop();
                unregisterReceiver(reciver);
            } else {
                Toast.makeText(this, "Stopped else", Toast.LENGTH_SHORT).show();
                super.onStop();
                unregisterReceiver(reciver);
            }
        }
        else
        {
            super.onStop();
        }

    }

    @Override
    protected void onRestart()
    {
        if (bluetoothActivity) {
            super.onRestart();
            Toast.makeText(this, "Restarted", Toast.LENGTH_SHORT).show();
            init();
            //mozda treba kod da se reciver registruje nekako!!!
        }
        else
        {
            super.onRestart();
        }
    }
    @Override
    protected void onDestroy()
    {
        if (bluetoothActivity) {
            if (clicked) {
                btAdapter.cancelDiscovery();
                Toast.makeText(this, "Destroyed", Toast.LENGTH_SHORT).show();
                super.onDestroy();
                unregisterReceiver(reciver);
            } else {
                super.onDestroy();
                unregisterReceiver(reciver);
            }
        }
        else
        {
            super.onDestroy();
        }
    }
    //kada se korisniku ponudi da ukljuci BT ako korisnik klikne cancel, izlazi se iz app i korisnik se obavestava da mora da ukljuci BT!!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        //POPRAVI ME PLSTY SSTY!!!!!
        if(listAdapter.getItem(position).contains("Paired")){//ovde ide kod za razmenu usernama!!!!

            BluetoothDevice selectedDevice = devices.get(position);// uredjaj na listi na koji je kliknuto!!!!
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
            //naredna funkcija treba zasebno valjda!!!!!
            //AcceptThread connects = new AcceptThread();
            //connects.run();
        }
        else {// umesto ovoga kad se klikene treba da se upare uredjaji!!!
            Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
            //Object[] o = devicesArray.toArray();
            // BluetoothDevice selectedDevice = (BluetoothDevice)o[position];
            // ConnectThread connect = new ConnectThread(selectedDevice);
            // connect.start();

        }
    }

    //KLIJENT!!!!
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i(tag, "connect - succeeded");
            } catch (IOException connectException) {
                Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT,mmSocket).sendToTarget();

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    //SERVER!!!!
    private class AcceptThread extends Thread {
        String name = btAdapter.getName();
        private final BluetoothServerSocket mmServerSocket;



        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(name, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void manageConnectedSocket(BluetoothSocket mySocket2){

        }
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    ///////////////////KONEKCIJA!!!!!//////////////////
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
