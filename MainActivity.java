package imran.app.timesup;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    //UI initializations
    ActionBar actionBar;
    ViewPager viewPager;
    Boolean flag;
    private Chronometer timer;
    private TextView SetTime_text;
    private long time = 0, time_elapsed =0;
    private Button start, pause, reset;

    //Set Time Dialog declarations
    NumberPicker npMinutes, npSeconds;
    AlertDialog alertDw;

    //Bluetooth declarations
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1; //does not matter what value (>0)

    //private BluetoothDevice mDevice;
    //private ArrayAdapter<String> BTArrayAdapter;

    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectThread mConnectThread;
    private String Toast_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetupUI();
    }

    private void SetupUI() {
        //Initializing ActionBar
        actionBar = getActionBar();

        //Establish Bluetooth Connection
        BT_Setup();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new page_adapter(fragmentManager));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText("Stopwatch").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Countdown").setTabListener(this));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    //***************************Number Picker Dialog*****************************//
    public void showPickerDialog(View view) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog, null);

        npMinutes = (NumberPicker) v.findViewById(R.id.numberPickerMinutes);
        npSeconds = (NumberPicker) v.findViewById(R.id.numberPickerSeconds);

        npMinutes.setMaxValue(59);
        npMinutes.setMinValue(0);

        npSeconds.setMaxValue(59);
        npSeconds.setMinValue(0);

        npMinutes.setWrapSelectorWheel(true);
        npSeconds.setWrapSelectorWheel(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(v);
        builder.setTitle("Set Time:");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast_Text = npMinutes.getValue() + ":" + npSeconds.getValue();
                Toast.makeText(getBaseContext(), Toast_Text, Toast.LENGTH_SHORT).show();


                SetTime_text.setText(new StringBuilder().append(pad(npMinutes.getValue()))
                        .append(":").append(pad(npSeconds.getValue())));
            }

            private String pad(int c) {
                if (c >= 10)
                    return String.valueOf(c);

                else
                    return "0" + String.valueOf(c);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDw.dismiss();
            }
        });
        alertDw = builder.create();
        alertDw.show();
    }

    //******************************Establishing Bluetooth Connection************************//
    public void BT_Setup(){
        //BTAdapter needed for BT activity - Asking user for connection
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            Toast.makeText(getBaseContext(),
                    "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }else {
            if (!mBluetoothAdapter.isEnabled()) {
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                String scanModeChanged = mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
                String beDiscoverable = mBluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
                IntentFilter filter = new IntentFilter(scanModeChanged);

                startActivityForResult(new Intent(beDiscoverable), REQUEST_ENABLE_BT);
            }else if (mBluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals("BTBEE PRO")) {
                            Toast_Text = "Found: " + device.getName()+ " in paired list.";
                            Toast.makeText(getBaseContext(), Toast_Text, Toast.LENGTH_SHORT).show();
                            mmDevice = device;
                        }
                    }
                }
                mConnectThread = new ConnectThread(mmDevice);
                mConnectThread.start();
            }
        }
    }
    //Continued Bluetooth Activity
    @Override
    protected void onActivityResult(int RequestCode, int ResultCode, Intent data){
        if (RequestCode == REQUEST_ENABLE_BT){
            Toast.makeText(getBaseContext(), "Bluetooth ON",
                    Toast.LENGTH_SHORT).show();

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("BTBEE PRO")) {
                        Toast_Text = "Found: " + device.getName()+ " in paired list.";
                        Toast.makeText(getBaseContext(), Toast_Text, Toast.LENGTH_SHORT).show();
                        mmDevice = device;
                    }
                }
            }
            mConnectThread = new ConnectThread(mmDevice);
            mConnectThread.start();
        }
    }
    private class ConnectThread extends Thread{

        public ConnectThread (BluetoothDevice device){
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                }
                catch (IOException closeException) { }
                return;
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            }
            catch (IOException e) { }
        }
    }//end ConnectThread

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}

class page_adapter extends android.support.v4.app.FragmentStatePagerAdapter{
    public page_adapter(android.support.v4.app.FragmentManager fm) {super(fm);}
    @Override
    public android.support.v4.app.Fragment getItem(int i) {

        android.support.v4.app.Fragment fragment = null;
        if (i==0)fragment = new StopWatch();
        if (i==1)fragment = new CountDown();
        return fragment;
    }
    @Override
    public int getCount() {return 2;}
}
