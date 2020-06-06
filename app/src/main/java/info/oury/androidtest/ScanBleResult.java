package info.oury.androidtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class ScanBleResult {

    private String garageId = "3C:71:BF:48:3C:1A";
    private boolean garageConnected = false;
    private BluetoothDevice garageBLEDevice;
    private BluetoothGatt garageGatt;
    private BluetoothGattCharacteristic garageTXCharacteristic;

    public String getGarageId() {
        return garageId;
    }

    public boolean isGarageConnected() {
        return this.garageConnected;
    }

    public void setGarageConnected(boolean garageConnected) {
        this.garageConnected = garageConnected;
    }

    public void setGarageBluetoothDevice(BluetoothDevice device) {
        this.garageBLEDevice = device;
    }

    public BluetoothDevice getGarageBLEDevice() {
        return garageBLEDevice;
    }

    public void setGarageGatt(BluetoothGatt gatt) {
        this.garageGatt = gatt;
    }

    public BluetoothGatt getGarageGatt() {
        return garageGatt;
    }

    public void setGarageTXCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.garageTXCharacteristic = characteristic;
    }

    public BluetoothGattCharacteristic getGarageTXCharacteristic() {
        return garageTXCharacteristic;
    }
}
