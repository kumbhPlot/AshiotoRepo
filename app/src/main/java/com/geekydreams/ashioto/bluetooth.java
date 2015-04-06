package com.geekydreams.ashioto;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import com.geekydreams.ashioto.R;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter.LengthFilter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class bluetooth extends ActionBarActivity {

    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    private boolean mIsUserInitiatedDisconnect = false;

    // All controls here
    private TextView mTxtReceive;
    private TextView mTxtOut;
    private TextView mTxtdensity;
    private EditText mEditSend;
    private Button mBtnDisconnect;
    private Button mBtnSend;
    private Button mBtnClear;
    private Button mBtnClearInput;
    //  private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    private String inFin;
    Integer inInt;
    String outFin;
    Integer outInt;

    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;
    String strInput;

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(bluetooth.this, "Device Disconnected", Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tooltooth);
        setSupportActionBar(toolbar);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(Start.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(Start.DEVICE_UUID));
        mMaxChars = b.getInt(Start.BUFFER_SIZE);


        //mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        //mBtnSend = (Button) findViewById(R.id.btnSend);
        //mBtnClear = (Button) findViewById(R.id.btnClear);
        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        mTxtOut = (TextView) findViewById(R.id.txtOut);
        mTxtdensity = (TextView) findViewById(R.id.textView3);

        //mEditSend = (EditText) findViewById(R.id.editSend);
//        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
//        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
//        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);


        

        /*mBtnDisconnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsUserInitiatedDisconnect = true;
                new DisConnectBT().execute();
            }
        });*/

        /*mBtnSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    mBTSocket.getOutputStream().write(mEditSend.getText().toString().getBytes());
                    mTxtReceive.setText("");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });*/
/*
        mBtnClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mEditSend.setText("");
            }
        });

        mBtnClearInput.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mTxtReceive.setText("");
            }
        });*/
    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @SuppressWarnings("unused")
        @SuppressLint("NewApi")
        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                if (inputStream == null) {
                    Toast.makeText(bluetooth.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
                while (!bStop) {    //due to this programme run infinitely
                    final byte[][] buffer = {new byte[64]};//This is the buffer size, i.e. the amount we read in one run
                    if (inputStream.available() > 0) {
                        int stringStream = inputStream.read(buffer[0]);
                        int i;
                        /*
						 * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
						 */
                        for (i = 0; i <= buffer[0].length && buffer[0][i] != 0; i++) {
                            strInput = new String(buffer[0], 0, i);
                        }

						/*
						 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
						 */


                        mTxtReceive.post(new Runnable() {
                            @Override
                            public void run() {
                                //replace #with $




                                //OUT
                                int endOut = strInput.lastIndexOf("%");
                                int secOut = strInput.lastIndexOf("^");
                                final String outFin = strInput.substring(endOut + 1, secOut);


                                //String inString = String.valueOf(inInt);
                                //String outString = String.valueOf(outInt);
                                /*String strTemp = strTest.replace(strTest.charAt(endlastoccurrence),'$');

                                final String strEmpty="";
                                int iLastindex=strTest.lastIndexOf('$');
                                final String strTest2=strTest.substring(iLastindex, strTest.length());
                                strInput=strInput.replace("#", "$");*/
                                try {
                                    //mTxtOut.setText(inString);
                                    mTxtReceive.setText(outFin);
                                } catch (NullPointerException e){
                                    //mTxtOut.setText("Data Not Available");
                                    mTxtReceive.setText("Data Not Available");
//                                  mTxtdensity.setText("Data Not Available");
                                    e.printStackTrace();
                                }

                                /*if (densityFin != null) {
                                    mTxtdensity.append(densityFin);
                                } else {
                                    mTxtdensity.setText("Data Not Available");
                                }*/

                                //Uncomment below for testing
                                //mTxtReceive.append("\n");
                                //mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() +"\n");
/*
                                    int outlength = mTxtOut.getEditableText().length();
                                    if (outlength >= outFin.length()){
                                        mTxtOut.getEditableText().delete(0, outlength - outFin.length());
                                    }

                                    int txtLength = mTxtReceive.getEditableText().length();
                                    if(txtLength >= inFin.length()){
                                        mTxtReceive.getEditableText().delete(0, txtLength - inFin.length() );
                                    }
*/

                                  /*  if (chkScroll.isChecked()) { // Scroll only if this is checked
                                        scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
                                            @Override
                                            public void run() {
                                                scrollView.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                    }*/
                            }
                        });
                        mTxtOut.post(new Runnable() {
                            @Override
                            public void run() {
                                //IN
                                int endIn = strInput.lastIndexOf("#");
                                int secIn = strInput.lastIndexOf("$");
                                final String inFin = strInput.substring(endIn + 1, secIn);
                                try {
                                    mTxtOut.setText(inFin);
                                } catch (NullPointerException e){
                                    mTxtOut.setText("Data not available");
                                    e.printStackTrace();
                                }
                            }
                        });
                        mTxtdensity.post(new Runnable() {
                            @Override
                            public void run() {
                                //DENSITY
                                Integer inInt = Integer.parseInt(inFin);
                                Integer outInt = Integer.parseInt(outFin);
                                int density = inInt - outInt;
                                String densityFin = String.valueOf(density);
                                try {
                                    mTxtdensity.setText(densityFin);
                                } catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (mBTSocket.equals(null)) {
                        Toast.makeText(bluetooth.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                    Thread.sleep(50);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(bluetooth.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    //mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    Method m = mDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    BluetoothSocket tmp = (BluetoothSocket) m.invoke(mDevice, 1);
                    mBTSocket = tmp;
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
                // Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
                mHandler.sendEmptyMessage(0);

            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }

}