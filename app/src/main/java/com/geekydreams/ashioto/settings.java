package com.geekydreams.ashioto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;


public class settings extends ActionBarActivity {

	public SeekBar seek;
	public EditText normal;
	public EditText warn;
	public EditText over;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolset);
        toolbar.setTitleTextColor(getResources().getColor(R.color.primary_light));
        setSupportActionBar(toolbar);
        SeekBar seek = (SeekBar) findViewById(R.id.seekBar);
        EditText normal = (EditText) findViewById(R.id.textView3);
        EditText warn = (EditText) findViewById(R.id.editText);
        EditText over = (EditText) findViewById(R.id.editText2);
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