package com.geekydreams.ashioto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;


public class settings extends AppCompatActivity {
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
    //Prefs
    SharedPreferences settingsPrefs;
    SharedPreferences.Editor settingsEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences area = getSharedPreferences(areaPref, 0);
        final SharedPreferences.Editor prefsEditor = area.edit();



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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}