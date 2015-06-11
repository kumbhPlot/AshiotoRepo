package com.geekydreams.ashioto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;


public class settings extends AppCompatActivity {
    //Views
    public SeekBar seek;
    public EditText normal;
    public EditText warn;
    public EditText over;
    public EditText areaView;
    //Strings
    public String areaPref = "areaPref";
    String areaString;
    String savedBefore;
    String floatString;
    //Ints and floats
    Float areaFloat;
    //SharedPrefs and editors


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences area = getSharedPreferences(areaPref, 0);
        final SharedPreferences.Editor prefsEditor = area.edit();

        seek = (SeekBar) findViewById(R.id.seekBar);
        normal = (EditText) findViewById(R.id.textView3);
        warn = (EditText) findViewById(R.id.editText);
        over = (EditText) findViewById(R.id.editText2);
        areaView = (EditText) findViewById(R.id.areaVal);

        SharedPreferences getArea = getSharedPreferences(areaPref, 0);
        float areaF = getArea.getFloat(areaPref, 0);
        savedBefore = String.valueOf(areaF);

        areaView.setText(savedBefore);


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
}