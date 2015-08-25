package com.geekydreams.ashioto;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Toast;

import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;


public class settings extends AppCompatActivity implements LocationListener {
    //Views
    public SeekBar seek;
    public EditText normal;
    public EditText warn;
    public EditText over;
    public EditText areaView;
    public NumberPicker numPick;
    public Button saveBut;
    //Strings
    public String areaPref = "areaPref";
    public String settingPref = "settings";
    String areaString;
    String savedBefore;
    //Ints and floats
    Float areaFloat;
    int normInt;
    int warnInt;
    int overInt;
    int gateInt;
    boolean loop = true;
    //Prefs
    SharedPreferences settingsPrefs;
    SharedPreferences.Editor settingsEditor;
    @Bind(R.id.gpsButton) Button gpsButton;

    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        SharedPreferences area = getSharedPreferences(areaPref, 0);
        final SharedPreferences.Editor prefsEditor = area.edit();

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
        provider = locationManager.getBestProvider(criteria, true);

        CardView normCard = (CardView) findViewById(R.id.cardNorm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolset);
        ViewCompat.setElevation(toolbar, 16);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(normCard, 16);

        saveBut = (Button) findViewById(R.id.saveBut);
        numPick = (NumberPicker) findViewById(R.id.numPick);
        numPick.setMinValue(1);
        numPick.setMaxValue(10);
        seek = (SeekBar) findViewById(R.id.seekBar);
        normal = (EditText) findViewById(R.id.textView3);
        warn = (EditText) findViewById(R.id.editText);
        over = (EditText) findViewById(R.id.editText2);
        areaView = (EditText) findViewById(R.id.areaVal);

        settingsPrefs = getSharedPreferences(settingPref, 0);
        settingsEditor = settingsPrefs.edit();
        SharedPreferences getArea = getSharedPreferences(areaPref, 0);
        float areaF = getArea.getFloat(areaPref, 0);
        savedBefore = String.valueOf(areaF);

        areaView.setText(savedBefore);

        saveBut.setOnClickListener(save);

/*        float savedArea = area.getFloat(areaPref, areaFloat);

        String savedAreaString = String.valueOf(savedArea);
        Log.d("TEST", savedAreaString);*/
        /*if (savedArea != 20)
            areaView.setText(savedAreaString);*/
        Log.d("TEST", "savedArea Success");
        //Processes
        areaView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                areaString = areaView.getText().toString();
                areaFloat = Float.parseFloat(areaString);
                prefsEditor.putFloat(areaPref, areaFloat).apply();
            }
        });

        gpsButton.setOnClickListener(getGPS);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
    View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gateInt = numPick.getValue();
            settingsEditor.putInt("gateID", gateInt).apply();
            areaString = areaView.getText().toString();
            areaFloat = Float.parseFloat(areaString);
            settingsEditor.putFloat(areaPref, areaFloat).apply();
            try {
                Start.localDB.putInt("gateID", gateInt);
                Start.localDB.putFloat(areaPref, areaFloat);
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }
    };

    View.OnClickListener getGPS = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gpsTask getGPSTask = new gpsTask();
            getGPSTask.execute();
            }
    };

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class gpsTask extends AsyncTask<Void, Void, Void>{
        private String latS,longi;
        @Override
        protected Void doInBackground(Void... params) {
            if (loop){
                Looper.prepare();
                loop = false;
            }
            if (provider!=null && !provider.equals("")){
                locationManager.requestLocationUpdates(provider,20000, 0, settings.this);
                Location location = locationManager.getLastKnownLocation(provider);
                double lat = location.getLatitude();
                double longitude = location.getLongitude();
                String latString = String.valueOf(lat);
                String longString = String.valueOf(longitude);
                latS = latString;
                longi = String.valueOf(location.getAccuracy());
                settingsEditor.putString("lat", latString).apply();
                settingsEditor.putString("long", longString).apply();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            Toast.makeText(settings.this, "Lat: " + latS + "\nLong: "+ longi, Toast.LENGTH_LONG).show();
        }
    }
}