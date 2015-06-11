package com.geekydreams.ashioto;

/**
 * Created by geek on 27/4/15.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    //Initialize Views
    TextView textResponse;
    EditText editTextAddress, editTextPort, toSend;
    Button buttonConnect, buttonClear, buttonSend;

    EditText inDB, outDB, appDB;
    //Amazon variables
    DynamoDBMapper mapper;

    //Strings
    String year;
    String month;
    String date;
    String hour;
    String minute;
    String second;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                MainActivity.this, // Context
                "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
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


    public class MyClientTask extends AsyncTask<Void, Void, Void> {

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
        @Override
        protected Void doInBackground(Void... voids) {
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
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();
            String inFin = inDB.getText().toString(),
                    outFin = outDB.getText().toString(),
                    appFin = appDB.getText().toString();
            Integer inDB = Integer.valueOf(inFin);
            Integer outDB = Integer.valueOf(outFin);

            Float appDB = Float.valueOf(appFin);
            Ashioto db = new Ashioto();
            db.setUuid(uuidString);
            db.setGateID(1);
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
            db.setPlotted(false);
            mapper.save(db);

            return null;
        }
        @Override
        public void onPostExecute(Void voids){
            Toast.makeText(getApplicationContext(), "Data Synced", Toast.LENGTH_SHORT).show();
            super.onPostExecute(voids);
        }
    }

    @DynamoDBTable(tableName = "Ashioto_test")
    public class Ashioto{
        private String year;
        private String month;
        private String date;
        private String hour;
        private String minute;
        private String second;
        private String uuid;
        private int gateID;
        private int inCount;
        private int outCount;
        private float app;
        private Boolean synced;
        private Boolean plotted;

        //Initialization Attributes
        @DynamoDBHashKey(attributeName = "uuid")
        public String getUuid(){
            return uuid;
        }
        public void setUuid(String uuid){
            this.uuid = uuid;
        }
        @DynamoDBIndexHashKey(attributeName = "GateID")
        public int getGateID(){
            return gateID;
        }
        public void setGateID(int gateID){
            this.gateID = gateID;
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
    }
}