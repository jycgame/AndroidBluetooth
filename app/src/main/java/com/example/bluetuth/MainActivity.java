package com.example.bluetuth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter adapter;
    TableLayout table;
    EditText infoText;
    Button scanButton, serverButton, clientButton;
    boolean receiverRegistered;

    HashMap<String, DeviceInfo> blueToothMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiverRegistered = false;
        adapter = BluetoothAdapter.getDefaultAdapter();
        scanButton = (Button) findViewById(R.id.button2);
        serverButton = (Button) findViewById(R.id.button3);
        clientButton = (Button) findViewById(R.id.button4);
        infoText = (EditText) findViewById(R.id.editText12);

        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Bluetooth paired", Toast.LENGTH_LONG).show();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!adapter.isEnabled()) {
                    adapter.enable();
                    return;
                }

                blueToothListenScan();
                adapter.startDiscovery();

                blueToothMap.clear();
                table = (TableLayout)findViewById(R.id.tableLayout);
                for (int i = 0; i < table.getChildCount(); ++i) {
                    TableRow row = (TableRow) table.getChildAt(i);
                    EditText et = (EditText) row.getChildAt(0);
                    et.setText("");
                    Button button = (Button) row.getChildAt(1);
                    button.setEnabled(false);
                }
                scanButton.setEnabled(false);
                infoText.setText("扫描开始");
            }
        });

        table = (TableLayout)findViewById(R.id.tableLayout);
        for (int i = 0; i < table.getChildCount(); ++i) {
            TableRow row = (TableRow) table.getChildAt(i);
            EditText et = (EditText) row.getChildAt(0);
            Button button = (Button) row.getChildAt(1);
            et.setFocusable(false);
            et.setClickable(true);
            button.setEnabled(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BluetoothDevice device = null;
                    Collection<DeviceInfo> dis = blueToothMap.values();
                    for (Iterator iterator = dis.iterator(); iterator.hasNext(); ) {
                        DeviceInfo info = (DeviceInfo) iterator.next();
                        if (info.button == v) {
                            device = info.device;
                            break;
                        }
                    }

                    if (device != null) {
                        blueToothPairDevice(device);
                    }
                    else {
                        Log.d("MyTag", "No suitable device found!");
                    }
                }
            });
        }

        infoText.setFocusable(false);
        infoText.setClickable(false);

        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        for (Iterator iterator = bondedDevices.iterator(); iterator.hasNext(); ) {
            BluetoothDevice device = (BluetoothDevice) iterator.next();
            Log.d("MyTag", "已绑定mac： " + device.getAddress());
            Log.d("MyTag", "已绑定名字：" + device.getName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (receiverRegistered) {
            unregisterReceiver(mReceiver);
        }
        receiverRegistered = false;
    }

    private void blueToothInit() {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void blueToothListenScan() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mReceiver, filter);
        receiverRegistered = true;
    }

    private void blueToothStartScan() {
        if (!adapter.isEnabled()) {
            blueToothEnable();
        }
        // start scan @ STEP N @
        adapter.startDiscovery();
    }

    // from url
    // http://www.londatiga.net/it/programming/android/how-to-programmatically-pair-or-unpair-android-bluetooth-device/
    private void blueToothPairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        }catch (Exception e) {
            e.printStackTrace();
        }

        // we will get result from Intent filter below
    }

    private void blueToothUnpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int DISCOVER_DURATION = 300;
    // our request code (must be greater than zero)
    private static final int REQUEST_BLU = 1;

    private void blueToothEnable() {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }

    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {
            //ENABLED successfully
        }
        else {
            //USER CANCELLED
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanButton.setEnabled(true);
                infoText.setText("扫描结束");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //发现蓝牙设备，加入到列表中
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                if (!blueToothMap.containsKey(deviceAddress)) {
                    DeviceInfo di = new DeviceInfo();
                    di.address = deviceAddress;
                    di.name = deviceName;
                    int index = blueToothMap.size();
                    TableRow row = (TableRow) table.getChildAt(index);
                    EditText et = (EditText) row.getChildAt(0);
                    Button button = (Button) row.getChildAt(1);
                    di.button = button;
                    di.device = device;

                    et.setText(deviceName != null ? deviceName : deviceAddress);
                    button.setEnabled(true);
                    button.setText("匹配");

                    blueToothMap.put(deviceAddress, di);
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    ParcelUuid[] uuids = device.getUuids();
                    Toast.makeText(getApplicationContext(), "Bluetooth paired", Toast.LENGTH_LONG).show();

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(getApplicationContext(), "Bluetooth unpaired", Toast.LENGTH_LONG).show();                }
            }
        }
    };

    private void createBluetoothServer(BluetoothAdapter adapter, UUID uuid) {
        try {
            BluetoothServerSocket server = adapter.listenUsingRfcommWithServiceRecord("BluetoothServer", uuid);
            BluetoothSocket clientSocket = server.accept();
            server.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}