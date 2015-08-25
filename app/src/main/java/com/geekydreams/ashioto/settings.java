package com.geekydreams.ashioto;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.snappydb.SnappydbException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class settings extends AppCompatActivity implements LocationListener {
    private final View.OnClickListener getGPS = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gpsTask getGPSTask = new gpsTask();
            getGPSTask.execute();
        }
    };
    public String settingPref = "settings";
    String savedBefore;
    //Prefs
    SharedPreferences settingsPrefs;
    @Bind(R.id.gpsButton)
    Button gpsButton;
    //Views
    private NumberPicker numPick;
    private boolean loop = true;
    private SharedPreferences.Editor settingsEditor;
    private final View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int gateInt = numPick.getValue();
            settingsEditor.putInt("gateID", gateInt).apply();
            try {
                Start.localDB.putInt("gateID", gateInt);
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }
    };
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolset);
        setSupportActionBar(toolbar);

        Button saveBut = (Button) findViewById(R.id.saveBut);
        numPick = (NumberPicker) findViewById(R.id.numPick);
        numPick.setMinValue(1);
        numPick.setMaxValue(10);

        saveBut.setOnClickListener(save);

/*        float savedArea = area.getFloat(areaPref, areaFloat);

        String savedAreaString = String.valueOf(savedArea);
        Log.d("TEST", savedAreaString);*/
        /*if (savedArea != 20)
            areaView.setText(savedAreaString);*/
        Log.d("TEST", "savedArea Success");
        //Processes

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