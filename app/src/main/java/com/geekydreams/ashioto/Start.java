package com.geekydreams.ashioto;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.annotations.RealmModule;


public class Start extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    uploadDB mUploadDB;
    CognitoCachingCredentialsProvider credentialsProvider1;

    public static DB localDB;

    private Button mBtnSearch;
    private Button mBtnConnect;
    private ListView mLstDevices;
    private BluetoothAdapter mBTAdapter;
    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;

    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
    // (http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createInsecureRfcommSocketToServiceRecord%28java.util.UUID%29)

    private int mBufferSize = 50000; //Default
    public static final String DEVICE_EXTRA = "com.geekydreams.ashioto.SOCKET";
    public static final String DEVICE_UUID = "com.geekydreams.ashioto.uuid";
    private static final String DEVICE_LIST = "com.geekydreams.ashioto.devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.geekydreams.ashioto.devicelistselected";
    public static final String BUFFER_SIZE = "com.geekydreams.ashioto.buffersize";

    TextView areaHint;
    public String areaPref = "areaPref";

    FragmentDrawer drawerFragment;

    @RealmModule(classes = {localSave.class})
    public static class ashiotoModule{

    }
    public static AmazonDynamoDBClient dbClient;
    public static DynamoDBMapper mapper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUploadDB = new uploadDB(this);
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        dbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(dbClient);

        RealmConfiguration configuration = new RealmConfiguration.Builder(getApplicationContext())
                .name("ashioto.realm")
                .schemaVersion(1)
                .setModules(new ashiotoModule())
                .build();
        Realm.setDefaultConfiguration(configuration);
        try {
            localDB = new SnappyDB.Builder(getApplicationContext())
                    .name("ashiotoDB")
                    .build();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolhome);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        drawerFragment.setDrawerListener(this);
        // ActivityHelper.initialize(this); //This is to ensure that the rotation persists across activities and not just this one
        SharedPreferences getArea = getSharedPreferences(areaPref, 0);
        float area = getArea.getFloat(areaPref, 0);
        String areaString = String.valueOf(area);

        try {
            MainActivity.ashiotoDB = DBFactory.open(Start.this, "Ashioto");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        mBtnSearch = (Button) findViewById(R.id.btnSearch);

        mLstDevices = (ListView) findViewById(R.id.lstDevices);
        /*
		 *Check if there is a savedInstanceState. If yes, that means the onCreate was probably triggered by a configuration change
		 *like screen rotate etc. If that's the case then populate all the views that are necessary here
		 */
        if (savedInstanceState != null) {
            ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
            if (list != null) {
                initList(list);
                MyAdapter adapter = (MyAdapter) mLstDevices.getAdapter();
                int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);
                if (selectedIndex != -1) {
                    adapter.setSelectedIndex(selectedIndex);
                    mBtnConnect.setEnabled(true);
                }
            } else {
                initList(new ArrayList<BluetoothDevice>());
            }

        } else {
            initList(new ArrayList<BluetoothDevice>());
        }


        mBtnSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mBTAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBTAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
                } else if (!mBTAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, BT_ENABLE_REQUEST);
                } else {
                    new SearchDevices().execute();
                }
            }
        });
        //Directly Search Devices without clicking
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBTAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
        } else if (!mBTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, BT_ENABLE_REQUEST);
        } else {
            new SearchDevices().execute();
        }

    }

    /**
     * Called when the screen rotates. If this isn't handled, data already generated is no longer available
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MyAdapter adapter = (MyAdapter) (mLstDevices.getAdapter());
        ArrayList<BluetoothDevice> list = (ArrayList<BluetoothDevice>) adapter.getEntireList();

        if (list != null) {
            outState.putParcelableArrayList(DEVICE_LIST, list);
            int selectedIndex = adapter.selectedIndex;
            outState.putInt(DEVICE_LIST_SELECTED, selectedIndex);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_ENABLE_REQUEST:
                if (resultCode == RESULT_OK) {
                    msg("Bluetooth Enabled successfully");
                    new SearchDevices().execute();
                } else {
                    msg("Bluetooth couldn't be enabled");
                }

                break;
            case SETTINGS: //If the settings have been updated
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String uuid = prefs.getString("prefUuid", "Null");
                mDeviceUUID = UUID.fromString(uuid);
                String bufSize = prefs.getString("prefTextBuffer", "Null");
                mBufferSize = Integer.parseInt(bufSize);

                String orientation = prefs.getString("prefOrientation", "Null");
                if (orientation.equals("Landscape")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (orientation.equals("Portrait")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (orientation.equals("Auto")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Quick way to call the Toast
     *
     * @param str
     */
    private void msg(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize the List adapter
     *
     * @param objects
     */
    private void initList(List<BluetoothDevice> objects) {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        mLstDevices.setAdapter(adapter);
        mLstDevices.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                BluetoothDevice device = ((MyAdapter) (mLstDevices.getAdapter())).getSelectedItem();
                Intent intent = new Intent(getApplicationContext(), bluetooth.class);
                intent.putExtra(DEVICE_EXTRA, device);
                intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        changeActivty(position);
    }
    private void changeActivty(int position){
        switch (position){
            case 0:
                Intent intent = new Intent(getApplicationContext(), settings.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * Searches for paired devices. Doesn't do a scan! Only devices which are paired through Settings->Bluetooth
     * will show up with this. I didn't see any need to re-build the wheel over here
     *
     * @author ryder
     */
    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

        @Override
        protected List<BluetoothDevice> doInBackground(Void... params) {
            Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
            List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : pairedDevices) {

                //check if the devicename starts with HC then only add into the list
                if (device.getName().startsWith("HC")) {
                    listDevices.add(device);
                }

            }
            return listDevices;

        }

        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices) {
            super.onPostExecute(listDevices);
            if (listDevices.size() > 0) {
                MyAdapter adapter = (MyAdapter) mLstDevices.getAdapter();
                adapter.replaceItems(listDevices);
            } else {
                msg("No paired devices found, please pair your serial BT device and try again");
            }
        }

    }

    /**
     * Custom adapter to show the current devices in the list. This is a bit of an overkill for this
     * project, but I figured it would be good learning
     * Most of the code is lifted from somewhere but I can't find the link anymore
     *
     * @author ryder
     */
    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {
        private int selectedIndex;
        private Context context;
        private int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;
        }

        public void setSelectedIndex(int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem() {
            return myList.get(selectedIndex);
        }

        @Override
        public int getCount() {
            return myList.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return myList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView tv;
        }

        public void replaceItems(List<BluetoothDevice> list) {
            myList = list;
            notifyDataSetChanged();
        }

        public List<BluetoothDevice> getEntireList() {
            return myList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            ViewHolder holder;
            if (convertView == null) {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();

                holder.tv = (TextView) vi.findViewById(R.id.lstContent);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex) {
                holder.tv.setBackgroundColor(selectedColor);
            } else {
                holder.tv.setBackgroundColor(Color.WHITE);
            }
            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n   " + device.getAddress());

            return vi;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }
    getLastDB mGetLast;
        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(Start.this, settings.class));
                break;
            case R.id.action_connect:
                startActivity(new Intent(Start.this, MainActivity.class));
                break;
            case R.id.action_upload:
                mGetLast = new getLastDB();
                uploadDB mUpload = new uploadDB(Start.this);
                mUpload.execute();
                Log.i("GGG", "Reached");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public class uploadDB extends AsyncTask<Void, Void, Void>{
        Context context;
        RealmResults<localSave> results;
        Realm realm;
        uploadDB(Context ctx){
            context = ctx;
            realm = Realm.getInstance(context);
            results = realm.where(localSave.class).equalTo("synced", false).findAll();
        }
        int lastUid = 0;

        String year, month, date, hour, minute, second;
        ArrayList<Ashioto> ashiotoArrayList = new ArrayList<>();
        @Override
        protected Void doInBackground(Void... params) {

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
            try {
                lastUid = mGetLast.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            realm.beginTransaction();
            for (localSave ls : results){
                Ashioto localSaveAshioto = new Ashioto();
                localSaveAshioto.setN(lastUid);
                localSaveAshioto.setUuid(lastUid);
                localSaveAshioto.setYear(year);
                localSaveAshioto.setMonth(month);
                localSaveAshioto.setDate(date);
                localSaveAshioto.setHour(hour);
                localSaveAshioto.setMinute(minute);
                localSaveAshioto.setSecond(second);
                localSaveAshioto.setVlotted(0);
                localSaveAshioto.setInCount(ls.getInCount());
                localSaveAshioto.setOutCount(ls.getOutCount());
                localSaveAshioto.setSynced(true);
                ashiotoArrayList.add(localSaveAshioto);
                ls.setSynced(true);
                realm.commitTransaction();
                Log.i("GGG", "SAving");
                Start.mapper.save(localSaveAshioto);
            }
            realm.close();
            return null;
        }
        @Override
        protected void onPostExecute(Void voids){
            super.onPostExecute(voids);
            Toast.makeText(getApplicationContext(), "Synced to database", Toast.LENGTH_LONG).show();
        }

    }
    public class getLastDB extends AsyncTask<Void, Void, Integer> {
        int finUUID = 0;
        Context context;
        AmazonDynamoDBClient dbClient;
        DynamoDBMapper mMapper;
        getLastDB(){
            credentialsProvider1 = new CognitoCachingCredentialsProvider(Start.this,
                    "us-east-1:08e41de7-9cb0-40d6-9f04-6f8956ed25bb", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider1);
            mMapper = new DynamoDBMapper(dynamoDBClient);
        }

        @Override
        protected Integer doInBackground(Void... params) {
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

            PaginatedQueryList res = mMapper.query(Ashioto.class, queryExpression);
            if (res.size() > 0) {
                Object gx = res.get(0);
                Map saf = null;
                try {
                    saf = MainActivity.getFieldNamesAndValues(gx, false);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (saf != null) {
                    Log.i("NNNN", saf.toString());
                    Integer f = (Integer) saf.get("uuid");
                    finUUID = f+1;
                    Log.i("NNNN", f.toString());
                }
            }
            else{
                finUUID = 1;
            }
            return finUUID;
        }
    }
}