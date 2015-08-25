package com.geekydreams.ashioto;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {


    public static DB ashiotoDB;
    //Amazon variables
    private static DynamoDBMapper mapper;
    private final OnClickListener sendClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            SyncTask syncTask = new SyncTask();
            syncTask.execute();
            System.gc();
        }
    };
    @Bind(R.id.localDBButton)
    Button saveLocalButton;
    @Bind(R.id.sendButton)
    Button buttonSend;
    @Bind(R.id.inSendDB)
    EditText inDB;
    @Bind(R.id.outSendDB)
    EditText outDB;
    //Ints
    int ud;
    AtomicInteger seq = new AtomicInteger();
    String sa;
    QueryResult result;
    Integer finUUID;
    //Strings
    private String year;
    private String month;
    private String date;
    private String hour;
    private String minute;
    private String second;
    //Shared Prefs
    private SharedPreferences uuidPrefs;
    private SharedPreferences.Editor uuidPrefsEditor;
    private int gateCode;
    private String longitude;
    private String latitude;

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
                DateFormat yearForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.YEAR_FIELD);
                DateFormat monthForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.MONTH_FIELD);
                DateFormat dateForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.DATE_FIELD);
                DateFormat hourForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.HOUR_OF_DAY0_FIELD);
                DateFormat minuteForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.MINUTE_FIELD);
                DateFormat secondForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.SECOND_FIELD);
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
                        outFin = outDB.getText().toString();
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
                realm.commitTransaction();
                realm.close();
            }
        });

        String u = "uuidP";
        uuidPrefs = getSharedPreferences(u, 0);
        uuidPrefsEditor = uuidPrefs.edit();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                MainActivity.this, // Context
                "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        // Initialize the Amazon Cognito credentials provider


        //Time Ends
        buttonSend.setOnClickListener(sendClickListener);

    }

    private class SyncTask extends AsyncTask<Void, Void, Void> {
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
            DateFormat yearForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.YEAR_FIELD);
            DateFormat monthForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.MONTH_FIELD);
            DateFormat dateForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.DATE_FIELD);
            DateFormat hourForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.HOUR_OF_DAY0_FIELD);
            DateFormat minuteForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.MINUTE_FIELD);
            DateFormat secondForm = DateFormat.getDateTimeInstance(DEFAULT_KEYS_SEARCH_LOCAL, DateFormat.SECOND_FIELD);
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
}