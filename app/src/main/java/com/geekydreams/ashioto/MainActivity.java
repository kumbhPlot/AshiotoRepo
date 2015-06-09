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
import java.util.TimeZone;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.services.dynamodbv2.model.*;

public class MainActivity extends Activity {

    TextView textResponse;
    EditText editTextAddress, editTextPort, toSend;
    Button buttonConnect, buttonClear;
    DynamoDBMapper mapper;

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

        editTextAddress = (EditText) findViewById(R.id.address);
        editTextPort = (EditText) findViewById(R.id.port);
        buttonConnect = (Button) findViewById(R.id.connect);
        buttonClear = (Button) findViewById(R.id.clear);
        textResponse = (TextView) findViewById(R.id.response);
        toSend = (EditText) findViewById(R.id.toSend);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }
        });
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                MainActivity.this, // Context
                "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);


        //Time
        Calendar calendar = Calendar.getInstance();

        DateFormat yearForm = new SimpleDateFormat("yyyy");
        DateFormat monthForm = new SimpleDateFormat("MM");
        DateFormat dateForm = new SimpleDateFormat("dd");
        DateFormat hourForm = new SimpleDateFormat("HH");
        DateFormat minuteForm = new SimpleDateFormat("mm");
        DateFormat secondForm = new SimpleDateFormat("ss");
        TimeZone timeZone = TimeZone.getTimeZone("IST");
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
        //Time Ends
    }

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
    @DynamoDBTable(tableName = "Ashioto")
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