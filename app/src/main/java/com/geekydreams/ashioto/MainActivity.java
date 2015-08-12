package com.geekydreams.ashioto;

/**
 * Created by geek on 27/4/15.
 */

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
import com.snappydb.SnappydbException;

public class MainActivity extends AppCompatActivity {

    //Initialize Views
    TextView textResponse;
    EditText editTextAddress, editTextPort, toSend;
    Button buttonConnect, buttonClear, buttonSend;

    EditText inDB, outDB, appDB;
    //Amazon variables
    DynamoDBMapper mapper;

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


    AmazonDynamoDBClient ddbClient;
    QueryResult result;

    public static DB ashiotoDB;

    int gateCode;

    Integer finUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences id = getSharedPreferences("settings", 0);

        try {
                if (MainActivity.ashiotoDB.exists("gateID")) {
                try {
                    gateCode = MainActivity.ashiotoDB.getInt("gateID");
                } catch (SnappydbException e) {
                    e.printStackTrace();
                }
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

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

        //Get previous commit
        ud = uuidPrefs.getInt("n", 0);
    }


    OnClickListener sendClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
//            int nxt = seq.incrementAndGet();
            ud += 1;
            uuidPrefsEditor.putInt("n", ud).apply();

            String uid = String.valueOf(ud);
            SyncTask syncTask = new SyncTask(uid, ud);
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

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
        String uuidString;
        int no;
        SyncTask(String uid, int n){
            uuidString = uid;
            no = n;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            Ashioto findLast = new Ashioto();
            findLast.setVlotted(1);
            Integer n = 1;
            Integer Plotted = 1;

            Condition hash = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withS(Plotted.toString()));

            Condition range = new Condition()
                    .withComparisonOperator(ComparisonOperator.GE.toString())
                    .withAttributeValueList(new AttributeValue().withN(n.toString()));

            HashMap<String, Condition> hashMap = new HashMap<>();
            hashMap.put("Plotted", hash);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(findLast)
                    .withIndexName("Plotted-n-index")
                    .withRangeKeyCondition("n", range)
                    .withScanIndexForward(false)
                    .withLimit(1)
                    .withConsistentRead(false);

            PaginatedQueryList res = mapper.query(Ashioto.class, queryExpression);
            Object gx = res.get(0);
            Map saf = null;

            Field field;
            try {
                saf = getFieldNamesAndValues(gx, false);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (saf != null) {
                Log.i("NNNN", saf.toString());
                Integer f = (Integer) saf.get("uuid");
                finUUID = f+1;
                Log.i("NNNN", f.toString());
            }

                /*try {
                    field = cl.getField("uuid");
                    int o = field.getInt(gx);
                    Toast.makeText(MainActivity.this, String.valueOf(o), Toast.LENGTH_LONG).show();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }*/

            String ho = res.toString();
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
            //End of Time
            String inFin = inDB.getText().toString(),
                    outFin = outDB.getText().toString(),
                    appFin = appDB.getText().toString();
            Integer inDB = Integer.valueOf(inFin);
            Integer outDB = Integer.valueOf(outFin);

            Float appDB = Float.valueOf(appFin);
            Ashioto db = new Ashioto();
            db.setUuid(finUUID);
            db.setN(finUUID);
            db.setGateID(gateCode);
            db.setInCount(inDB);
            db.setOutCount(outDB);
            db.setApp(appDB);
            db.setYear(year);
            db.setMonth(month);
            db.setDate(date);
            db.setHour(hour);
            db.setMinute(minute);
            db.setSecond(second);
            db.setSynced(true);
//            db.setPlotted(false);
            db.setVlotted(0);
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
        Class<? extends Object> c1 = obj.getClass();
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = c1.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (publicOnly) {
                if(Modifier.isPublic(fields[i].getModifiers())) {
                    Object value = fields[i].get(obj);
                    map.put(name, value);
                }
            }
            else {
                fields[i].setAccessible(true);
                Object value = fields[i].get(obj);
                map.put(name, value);
            }
        }
        return map;
    }

}