package info.oury.androidtest;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleManager {

    private static BleManager instance = null;
    private static ScanBleResult scanBleResult;

    private static BluetoothManager btManager;
    private static BluetoothAdapter btAdapter;
    private static BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static Boolean btScanning = false;
    private static int deviceIndex = 0;
    private static ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    private static BluetoothGatt bluetoothGatt;

    // Stops scanning after 5 seconds.
    private static Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;
    private MainActivity mainActivity;
    private MainActivity.ScanBleCallback scanBleCallback;

    public static BleManager getInstance() {
        if (instance == null) {
            instance = new BleManager();
        }
        return instance;
    }

    public void init(final MainActivity mainActivity) {

        this.mainActivity = mainActivity;

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (mainActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
//            final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
//            builder.setTitle("This app needs location access");
//            builder.setMessage("Please grant location access so this app can detect peripherals.");
//            builder.setPositiveButton(android.R.string.ok, null);
//            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    mainActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
//                }
//            });
//            builder.show();
        }

        btManager = (BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    public void scan(MainActivity.ScanBleCallback scanBleCallback) {
        this.scanBleResult = new ScanBleResult();
        this.scanBleCallback = scanBleCallback;
        disconnect();
        startScanning();
        scanBleCallback.onScanned(this.scanBleResult);

    }

    public void sendGarageCommand() {
        if (scanBleResult.isGarageConnected()) {
            scanBleResult.getGarageTXCharacteristic().setValue("toto");
            bluetoothGatt.writeCharacteristic(scanBleResult.getGarageTXCharacteristic());
        }
    }

    public ScanBleResult getScanBleResult() {
        return this.scanBleResult;
    }

    public void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        deviceIndex = 0;
        devicesDiscovered.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        btScanning = false;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getAddress().equalsIgnoreCase(scanBleResult.getGarageId())) {
                scanBleResult.setGarageBluetoothDevice(result.getDevice());
                bluetoothGatt = result.getDevice().connectGatt(mainActivity, false, btleGattCallback);
            }
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            switch (newState) {
                case 0:
                    if (gatt.getDevice().equals(scanBleResult.getGarageBLEDevice())) {
//                        scanBleResult.setGarageConnected(false);
                    }
                    break;
                case 2:
                    if (gatt.getDevice().equals(scanBleResult.getGarageBLEDevice())) {
//                        scanBleResult.setGarageConnected(true);
                    }

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Snackbar.make(mainActivity.getCurrentFocus(), "An error occurs with connected device", Snackbar.LENGTH_LONG)
//                                    .show();
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (gatt.getDevice().equals(scanBleResult.getGarageBLEDevice())) {
                scanBleResult.setGarageGatt(gatt);

                for (BluetoothGattService gattService : gatt.getServices()) {
                    if (gattService.getUuid().equals(UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"))){
                        List<BluetoothGattCharacteristic> gattCharacteristics =
                                gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic gattCharacteristic :
                                gattCharacteristics) {
                            if (gattCharacteristic.getUuid().equals(UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"))){
                                scanBleResult.setGarageTXCharacteristic(gattCharacteristic);
                                scanBleResult.setGarageConnected(true);
                                mainActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        scanBleCallback.onScanned(scanBleResult);
                                    }
                                });
                            }
                        }
                    }
                }
            }
//            Snackbar.make(mainActivity.getCurrentFocus(), "Services discovered", Snackbar.LENGTH_LONG)
//                    .show();
        }

    };

    public void disconnect() {
        if (scanBleResult.isGarageConnected()) {
            scanBleResult.getGarageGatt().disconnect();
        }
    }
}
