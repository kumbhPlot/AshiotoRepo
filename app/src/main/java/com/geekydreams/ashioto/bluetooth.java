package com.geekydreams.ashioto;

import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class bluetooth extends AppCompatActivity {

    @Bind(R.id.saveLocal) Button saveLocalButton;


    float appFin;
    float area;
    String rpi = "nilayscience.no-ip.org";
    int prt = 8343;
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    private boolean mIsUserInitiatedDisconnect = false;

    public Integer finUUID;
    // All controls here
    private TextView areaHint;
    private TextView mTxtReceive;
    private TextView mTxtOut;
    private TextView mTxtdensity;
    Button syncBtn;
    //  private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    Integer inInt;
    String outFin;
    Integer outInt;
    RelativeLayout denRelative;
    RelativeLayout inRelative;
    RelativeLayout outRelative;
    public String areaPref = "areaPref";
    String inFin;
    String denstityStr;
    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;
    String strInput;

    //Amazon variables
    DynamoDBMapper mapper;

    //Strings
    String year;
    String month;
    String date;
    String hour;
    String minute;
    String second;

    //Ints
    int ud;
    int gateId;
    int gateCode;
    String lat,longi;

    public String u = "uuidP";

    //Shared Prefs
    SharedPreferences uuidPrefs;
    SharedPreferences.Editor uuidPrefsEditor;

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(bluetooth.this, "Device Disconnected", Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }


    };

    DB localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);
        try {
            localDb = DBFactory.open(getApplication(), "ashiotoDB");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        try {
            if (Start.localDB.exists("gateID")) {
                try {
                    gateCode = Start.localDB.getInt("gateID");
                } catch (SnappydbException e) {
                    e.printStackTrace();
                }
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        SharedPreferences getGateID = getSharedPreferences("settings", 0);
        gateId = getGateID.getInt("gateID", 1);
        lat = getGateID.getString("lat", "0.000");
        longi = getGateID.getString("long", "0.000");

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                bluetooth.this, // Context
                "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        final AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);


        Toolbar toolbar = (Toolbar) findViewById(R.id.tooltooth);
        setSupportActionBar(toolbar);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(Start.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(Start.DEVICE_UUID));
        mMaxChars = b.getInt(Start.BUFFER_SIZE);
        syncBtn = (Button) findViewById(R.id.syncBtn);

        OnClickListener buttonConnectOnClickListener =
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
/*
                        QueryRequest request = new QueryRequest()
                                .withTableName("kumbha5")
                                .withScanIndexForward(true)
                                .withAttributesToGet("uuid")
                                .withLimit(1);

                        QueryResult result = ddbClient.query(request);

                        List g = result.getItems();

                        String lastUUID = g.get(0).toString();
                        Integer h = Integer.parseInt(lastUUID);
                        Toast.makeText(getApplicationContext(), lastUUID, Toast.LENGTH_LONG).show();
*/

                        String uid = String.valueOf(ud);
                        SyncTask syncTask = new SyncTask(uid, ud);
                        syncTask.execute();
                    }
                };
        syncBtn.setOnClickListener(buttonConnectOnClickListener);
        uuidPrefs = getSharedPreferences(u, 0);
        uuidPrefsEditor = uuidPrefs.edit();
        saveLocalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = uuidPrefs.getInt("uuid", 0);
                int nxt = cur + 1;
                uuidPrefsEditor.putInt("uuid", nxt).apply();
                localSaveTask saveTask = new localSaveTask(nxt);
                saveTask.execute();
            }
        });

        //GEt Area
        //SharedPreferences

        //mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        //mBtnSend = (Button) findViewById(R.id.btnSend);
        //mBtnClear = (Button) findViewById(R.id.btnClear);
        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        mTxtOut = (TextView) findViewById(R.id.txtOut);
        mTxtdensity = (TextView) findViewById(R.id.densityView);

        //mEditSend = (EditText) findViewById(R.id.editSend);
//        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
//        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
//        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);




        /*mBtnDisconnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsUserInitiatedDisconnect = true;
                new DisConnectBT().execute();
            }
        });*/

        /*mBtnSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    mBTSocket.getOutputStream().write(mEditSend.getText().toString().getBytes());
                    mTxtReceive.setText("");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });*/
/*
        mBtnClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mEditSend.setText("");
            }
        });

        mBtnClearInput.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mTxtReceive.setText("");
            }
        });*/
    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {    //due to this programme run infinitely
                    final byte[] buffer = new byte[2048];//This is the buffer size, i.e. the amount we read in one run
                    if (inputStream.available() > 0) {
                        int stringStream = inputStream.read(buffer);
                        int i;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
						 */
                        for (i = 0; i <= buffer.length && buffer[i] != 0; i++) {
                            strInput = new String(buffer, 0, i);
                        }

						/*
						 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
						 */

                        if (strInput.startsWith("#")) {

                            mTxtReceive.post(new Runnable() {
                                @Override
                                public void run() {
                                    //replace #with $


                                    //OUT
                                    int endOut = strInput.lastIndexOf("%");
                                    int secOut = strInput.lastIndexOf("^");
                                    outFin = strInput.substring(endOut + 1, secOut);
                                    //IN
                                    int endIn = strInput.lastIndexOf("#");
                                    int secIn = strInput.lastIndexOf("$");
                                    final String inFin = strInput.substring(endIn + 1, secIn);
                                    //DENSITY
                                    Float inInt = Float.parseFloat(inFin);
                                    Float outInt = Float.parseFloat(outFin);
                                    float pplInInt = inInt - outInt;
                                    SharedPreferences getArea = getSharedPreferences(areaPref, 0);
                                    area = getArea.getFloat(areaPref, 0);
                                    appFin = area / pplInInt;
                                    denstityStr = String.valueOf(appFin);
                                    try {
                                        mTxtdensity.setText(denstityStr);
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
//                                        mTxtdensity.setText("Data Not Available");
                                    }


                                    //String inString = String.valueOf(inInt);
                                    //String outString = String.valueOf(outInt);
                                /*String strTemp = strTest.replace(strTest.charAt(endlastoccurrence),'$');

                                final String strEmpty="";
                                int iLastindex=strTest.lastIndexOf('$');
                                final String strTest2=strTest.substring(iLastindex, strTest.length());
                                strInput=strInput.replace("#", "$");*/
                                    try {
                                        //mTxtOut.setText(inString);
                                        mTxtReceive.setText(outFin);
                                    } catch (NullPointerException e) {
                                        //mTxtOut.setText("Data Not Available");
                                        mTxtReceive.setText("Data Not Available");
//                                  mTxtdensity.setText("Data Not Available");
                                        e.printStackTrace();
                                    }

                                /*if (densityFin != null) {
                                    mTxtdensity.append(densityFin);
                                } else {
                                    mTxtdensity.setText("Data Not Available");
                                }*/

                                    //Uncomment below for testing
                                    //mTxtReceive.append("\n");
                                    //mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() +"\n");
/*
                                    int outlength = mTxtOut.getEditableText().length();
                                    if (outlength >= outFin.length()){
                                        mTxtOut.getEditableText().delete(0, outlength - outFin.length());
                                    }

                                    int txtLength = mTxtReceive.getEditableText().length();
                                    if(txtLength >= inFin.length()){
                                        mTxtReceive.getEditableText().delete(0, txtLength - inFin.length() );
                                    }
*/

                                  /*  if (chkScroll.isChecked()) { // Scroll only if this is checked
                                        scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
                                            @Override
                                            public void run() {
                                                scrollView.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                    }*/
                                }
                            });
                            mTxtOut.post(new Runnable() {
                                @Override
                                public void run() {
                                    //IN
                                    int endIn = strInput.lastIndexOf("#");
                                    int secIn = strInput.lastIndexOf("$");
                                    inFin = strInput.substring(endIn + 1, secIn);

                                    try {
                                        mTxtOut.setText(inFin);
                                    } catch (NullPointerException e) {
                                        mTxtOut.setText("Data not available");
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    } else if (mBTSocket == null) {
                        Toast.makeText(bluetooth.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                    Thread.sleep(100);
                }
            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(bluetooth.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    //mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    Method m = mDevice.getClass().getMethod("createRfcommSocket", int.class);
                    mBTSocket = (BluetoothSocket) m.invoke(mDevice, 1);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
                // Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
                mHandler.sendEmptyMessage(0);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response;

        MyClientTask(String addr, int port) {
            dstAddress =  addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                Socket socket = new Socket(dstAddress, dstPort);
                InputStream inputStream = socket.getInputStream();
                String s = "#" + outFin + "$" + inFin + "*" + appFin + "%";
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                pw.println(s);
                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                socket.close();
                response = byteArrayOutputStream.toString("UTF-8");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(bluetooth.this, response, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }

    }
    /*@DynamoDBTable(tableName = "test_gate1")
    public class Ashioto{
        private String year;
        private String month;
        private String date;
        private String hour;
        private String minute;
        private String second;
        private int uuid;
        private int n;
        private int gateID;
        private int inCount;
        private int outCount;
        private int vlotted;
        private float app;
        private Boolean synced;
        private Boolean plotted;

        //Initialization Attributes
        @DynamoDBHashKey(attributeName = "uuid")
        public int getUuid(){
            return uuid;
        }
        public void setUuid(int uuid){
            this.uuid = uuid;
        }
        @DynamoDBIndexHashKey(attributeName = "Plotted", globalSecondaryIndexName = "Plotted-n-index")
        public int getVlotted(){
            return vlotted;
        }
        public void setVlotted(int vlotted){
            this.vlotted = vlotted;
        }
        @DynamoDBAttribute(attributeName = "GateID")
        public int getGateID(){
            return gateID;
        }
        public void setGateID(int gateID){
            this.gateID = gateID;
        }
        @DynamoDBIndexRangeKey(attributeName = "n", globalSecondaryIndexName = "Plotted-n-index")
        public int getN(){
            return n;
        }
        public void setN(int n){
            this.n = n;
        }
        //End of initialization values
        //Resource Attributes
        @DynamoDBAttribute(attributeName = "Plotted")
        public Boolean getPlotted(){
            return plotted;
        }
        public void setPlotted(Boolean plotted){
            this.plotted = plotted;
        }
        @DynamoDBAttribute(attributeName = "Synced")
        public Boolean getSynced(){
            return synced;
        }
        public void setSynced(Boolean synced){
            this.synced = synced;
        }
        @DynamoDBAttribute(attributeName = "In")
        public int getInCount(){
            return inCount;
        }
        public void setInCount(int inCount){
            this.inCount = inCount;
        }
        @DynamoDBAttribute(attributeName = "Out")
        public int getOutCount(){
            return outCount;
        }
        public void setOutCount(int outCount){
            this.outCount = outCount;
        }
        @DynamoDBAttribute(attributeName = "APP")
        public float getApp(){
            return app;
        }
        public void setApp(float app){
            this.app = app;
        }
        //End of Resource Attributes
        //Timestamp Attributes
        @DynamoDBAttribute(attributeName = "Year")
        public String getYear(){
            return year;
        }
        public void setYear(String year){
            this.year = year;
        }
        @DynamoDBAttribute(attributeName = "Month")
        public String getMonth(){
            return month;
        }
        public void setMonth(String month){
            this.month = month;
        }
        @DynamoDBAttribute(attributeName = "Date")
        public String getDate(){
            return date;
        }
        public void setDate(String date){
            this.date = date;
        }
        @DynamoDBAttribute(attributeName = "Hour")
        public String getHour(){
            return hour;
        }
        public void setHour(String hour){
            this.hour = hour;
        }
        @DynamoDBAttribute(attributeName = "Minute")
        public String getMinute(){
            return minute;
        }
        public void setMinute(String minute){
            this.minute = minute;
        }
        @DynamoDBAttribute(attributeName = "Second")
        public String getSecond(){
            return second;
        }
        public void setSecond(String second){
            this.second = second;
        }
        //End of Timestamp Attributes
    }*/
    public class SyncTask extends AsyncTask<Void, Void, Void>{
        String uuidString;
        int no;
        SyncTask(String uid, int n){
            uuidString = uid;
            no = n;
        }
        @Override
        protected Void doInBackground(Void... voids) {

                /*try {
                    field = cl.getField("uuid");
                    int o = field.getInt(gx);
                    Toast.makeText(MainActivity.this, String.valueOf(o), Toast.LENGTH_LONG).show();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }*/

            //Time
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
            DateFormat yearForm = new SimpleDateFormat("yyyy");
            DateFormat monthForm = new SimpleDateFormat("MM");
            DateFormat dateForm = new SimpleDateFormat("dd");
            DateFormat hourForm = new SimpleDateFormat("HH");
            DateFormat minuteForm = new SimpleDateFormat("mm");
            DateFormat secondForm = new SimpleDateFormat("ss");
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
            yearForm.setTimeZone(timeZone);
            monthForm.setTimeZone(timeZone);
            dateForm.setTimeZone(timeZone);
            hourForm.setTimeZone(timeZone);
            minuteForm.setTimeZone(timeZone);
            secondForm.setTimeZone(timeZone);
            year = yearForm.format(calendar.getTime());
            month = monthForm.format(calendar.getTime());
            date = dateForm.format(calendar.getTime());
            hour = hourForm.format(calendar.getTime());
            minute = minuteForm.format(calendar.getTime());
            second = secondForm.format(calendar.getTime());
            String timestampFinal = year+"/"+month+"/"+date+" "+hour+":"+minute+":"+second;
            //End of Time
            Integer inDB = Integer.valueOf(inFin);
            Integer outDB = Integer.valueOf(outFin);
            com.geekydreams.ashioto.Ashioto db = new com.geekydreams.ashioto.Ashioto();
            db.setGateID(gateCode);
            db.setTimestamp(timestampFinal);
            db.setInCount(inDB);
            db.setOutCount(outDB);
            db.setLattitude(lat);
            db.setLongitude(longi);
            mapper.save(db);
            return null;
        }
        @Override
        public void onPostExecute(Void voids){
            Toast.makeText(getApplicationContext(), "Data Synced", Toast.LENGTH_SHORT).show();
            super.onPostExecute(voids);
        }
    }
    public class localSaveTask extends AsyncTask<Void, Void, Void>{

        int no;
        localSaveTask(int number){
            no = number;
        }
        @Override
        protected Void doInBackground(Void... params) {
            //Time
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
            DateFormat yearForm = new SimpleDateFormat("yyyy");
            DateFormat monthForm = new SimpleDateFormat("MM");
            DateFormat dateForm = new SimpleDateFormat("dd");
            DateFormat hourForm = new SimpleDateFormat("HH");
            DateFormat minuteForm = new SimpleDateFormat("mm");
            DateFormat secondForm = new SimpleDateFormat("ss");
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
            yearForm.setTimeZone(timeZone);
            monthForm.setTimeZone(timeZone);
            dateForm.setTimeZone(timeZone);
            hourForm.setTimeZone(timeZone);
            minuteForm.setTimeZone(timeZone);
            secondForm.setTimeZone(timeZone);
            String mYear = yearForm.format(calendar.getTime());
            String mMonth = monthForm.format(calendar.getTime());
            String mDate = dateForm.format(calendar.getTime());
            String mHour = hourForm.format(calendar.getTime());
            String mMinute = minuteForm.format(calendar.getTime());
            String mSecond = secondForm.format(calendar.getTime());
            Integer inDB = Integer.valueOf(inFin);
            Integer outDB = Integer.valueOf(outFin);
            Realm mRealm;
            mRealm = Realm.getDefaultInstance();
            mRealm.beginTransaction();
            localSave toCommit = mRealm.createObject(localSave.class);
            toCommit.setUid(no);
            toCommit.setInCount(inDB);
            toCommit.setOutCount(outDB);
            toCommit.setYear(mYear);
            toCommit.setMonth(mMonth);
            toCommit.setDate(mDate);
            toCommit.setHour(mHour);
            toCommit.setMinute(mMinute);
            toCommit.setSecond(mSecond);
            mRealm.commitTransaction();


            return null;
        }
        @Override
        public void onPostExecute(Void voids){
            super.onPostExecute(voids);
            Toast.makeText(bluetooth.this, "Saved Loally: " + String.valueOf(no), Toast.LENGTH_LONG).show();
        }
    }
}