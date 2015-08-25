package com.geekydreams.ashioto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {



    @Bind(R.id.localDBButton) Button saveLocalButton;

    //Initialize Views
    TextView textResponse;
    EditText editTextAddress, editTextPort, toSend;
    Button buttonConnect, buttonClear, buttonSend;

    EditText inDB, outDB, appDB;
    //Amazon variables
    public static DynamoDBMapper mapper;

    //Ints
    int ud;
    int gateID;

    //Strings
    String year;
    String month;
    String date;
    String hour;
    String minute;
    String second;
    AtomicInteger seq = new AtomicInteger();
    public String u = "uuidP";
    String sa;

    //Shared Prefs
    SharedPreferences uuidPrefs;
    SharedPreferences.Editor uuidPrefsEditor;


    public static AmazonDynamoDBClient ddbClient;
    QueryResult result;

    public static DB ashiotoDB;

    int gateCode;
    String longitude, latitude;

    Integer finUUID;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences id = getSharedPreferences("settings", 0);
        longitude = id.getString("long", "0.000");
        latitude = id.getString("lat", "0.000");

        try {
            ashiotoDB = DBFactory.open(MainActivity.this, "ashiotoDB");
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

        saveLocalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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


                Realm realm  = Realm.getDefaultInstance();
                int cur = uuidPrefs.getInt("uuid", 0);
                int nxt = cur + 1;
                uuidPrefsEditor.putInt("uuid", nxt).apply();
                String inFin = inDB.getText().toString(),
                        outFin = outDB.getText().toString(),
                        appFin = appDB.getText().toString();
                Float appDB = Float.valueOf(appFin);
                Integer inDB = Integer.valueOf(inFin);
                Integer outDB = Integer.valueOf(outFin);

                realm.beginTransaction();
                localSave toCommit = realm.createObject(localSave.class);
                toCommit.setUid(nxt);
                toCommit.setInCount(inDB);
                toCommit.setOutCount(outDB);
                toCommit.setYear(year);
                toCommit.setMonth(month);
                toCommit.setDate(date);
                toCommit.setHour(hour);
                toCommit.setMinute(minute);
                toCommit.setSecond(second);
                toCommit.setSynced(false);
                toCommit.setApp(appDB);
                realm.commitTransaction();
                realm.close();
            }
        });

        gateID = id.getInt("gateID", 1);
        uuidPrefs = getSharedPreferences(u, 0);
        uuidPrefsEditor = uuidPrefs.edit();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                MainActivity.this, // Context
                "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        buttonConnect = (Button) findViewById(R.id.connect);
        buttonClear = (Button) findViewById(R.id.clear);
        buttonSend = (Button) findViewById(R.id.sendButton);
        editTextAddress = (EditText) findViewById(R.id.address);
        editTextPort = (EditText) findViewById(R.id.port);
        toSend = (EditText) findViewById(R.id.toSend);
        inDB = (EditText) findViewById(R.id.inSendDB);
        outDB = (EditText) findViewById(R.id.outSendDB);
        appDB = (EditText) findViewById(R.id.appSendDB);

        textResponse = (TextView) findViewById(R.id.response);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }
        });
        // Initialize the Amazon Cognito credentials provider


        //Time Ends
        buttonSend.setOnClickListener(sendClickListener);

    }


    OnClickListener sendClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            SyncTask syncTask = new SyncTask();
            syncTask.execute();
            System.gc();
        }
    };
    OnClickListener buttonConnectOnClickListener =
            new OnClickListener() {

                @Override
                public void onClick(View arg0) {
     /*
      * You have to verify editTextAddress and
      * editTextPort are input as correct format.
      */

                    MyClientTask myClientTask = new MyClientTask(
                            editTextAddress.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()));
                    myClientTask.execute();
                }
            };


    public class MyClientTask extends AsyncTask<Void, Void, Void>{

        String dstAddress;
        int dstPort;
        String response;

        MyClientTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                Socket socket = new Socket(dstAddress, dstPort);
                InputStream inputStream = socket.getInputStream();
                String s = toSend.getText().toString();
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
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }

    public class SyncTask extends AsyncTask<Void, Void, Void>{
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
//            Log.i("Res", gx.toString());

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
            String inFin = inDB.getText().toString(),
                    outFin = outDB.getText().toString();
            Integer inDB = Integer.valueOf(inFin);
            Integer outDB = Integer.valueOf(outFin);

            Ashioto db = new Ashioto();
            db.setGateID(gateCode);
            db.setTimestamp(timestampFinal);
            db.setInCount(inDB);
            db.setOutCount(outDB);
            db.setLattitude(latitude);
            db.setLongitude(longitude);
            mapper.save(db);

            return null;
        }
        @Override
        public void onPostExecute(Void voids){
            Toast.makeText(getApplicationContext(), "Data Synced", Toast.LENGTH_SHORT).show();
            super.onPostExecute(voids);
        }
    }
    public static Map<String, Object> getFieldNamesAndValues(final Object obj, boolean publicOnly)
            throws IllegalArgumentException,IllegalAccessException
    {
        Class<?> c1 = obj.getClass();
        Map<String, Object> map = new HashMap<>();
        Field[] fields = c1.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            if (publicOnly) {
                if (Modifier.isPublic(field.getModifiers())) {
                    Object value = field.get(obj);
                    map.put(name, value);
                }
            } else {
                field.setAccessible(true);
                Object value = field.get(obj);
                map.put(name, value);
            }
        }
        return map;
    }
/*
    public class localSaveTask extends AsyncTask<Void, Void, Void>{
        realm = Realm.getInstance(MainActivity.this);
        int no;
        localSaveTask(int number){
            no = number;
        }
        @Override
        protected Void doInBackground(Void... params) {
            String inFin = inDB.getText().toString(),
                    outFin = outDB.getText().toString();
            Integer inDB = Integer.valueOf(inFin);
            Integer outDB = Integer.valueOf(outFin);

            realm.beginTransaction();
            localSave toCommit = realm.createObject(localSave.class);
            toCommit.setUid(no);
            toCommit.setInCount(inDB);
            toCommit.setOutCount(outDB);
            toCommit.setYear(year);
            toCommit.setMonth(month);
            toCommit.setDate(date);
            toCommit.setHour(hour);
            toCommit.setMinute(minute);
            toCommit.setSecond(second);
            realm.commitTransaction();


            return null;
        }
        @Override
        public void onPostExecute(Void voids){
            super.onPostExecute(voids);
            Toast.makeText(MainActivity.this, "Saved Locally: " + String.valueOf(no), Toast.LENGTH_LONG).show();
        }
    }
*/

    Thread dbThread = new Thread(new Runnable() {
        Realm realm = Realm.getDefaultInstance();
        @Override
        public void run() {

        }
    });
}