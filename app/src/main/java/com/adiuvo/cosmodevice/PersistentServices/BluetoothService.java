package com.adiuvo.cosmodevice.PersistentServices;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {
    final String TAG = "BluetoothService";
    BluetoothDevice thedevice;
    BluetoothSocket thesocket;
    // Binder given to clients
    private final IBinder binder = new BluetoothBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class BluetoothBinder extends Binder {
        public BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            Log.d(TAG, "getService: ");
            return BluetoothService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + intent);
        BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        if (!BA.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                BA.enable();
                if (thedevice == null) {
                    Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().equals("raspberrypi")) {
                                thedevice = device;
                            }
                        }
                    }
                }
                //If device is still null return 1
                if (thedevice == null) {
                    Log.d(TAG, "onBind: NO DEVICE FOUND");
                }
                if (thesocket == null){
                try {
                    thesocket = thedevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848"));
                    if (!thesocket.isConnected()){
                        Log.d(TAG, "onBind: Socket is created and connection is initialized"+thesocket);
                        thesocket.connect();
                    }
                    if(thesocket==null){
                        Log.d(TAG, "onBind: Socket initialization failed"+thesocket);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }

        }
        //Search for device with name raspberry pi if device is not initialized.
        return binder;
    }

    /** method for clients */
    public int getRandomNumber() {
        Log.d(TAG, "getRandomNumber: "+mGenerator.nextInt(100));
        return mGenerator.nextInt(100);
    }


    public int btSendCmd(Character send) {
        //Check Adapter is enabled

        //Send Command to Raspberry Pi.
        try {
            OutputStream outpacket = thesocket.getOutputStream();
            outpacket.write(send);
            InputStream inpacket = thesocket.getInputStream();
            int recv = inpacket.read();
            if (recv == send) {
                return 0;
            } else {
                return 1;
            }
        } catch (IOException e) {
            try {
                if (thesocket != null)
                    thesocket.close();
                thesocket = null;
            } catch (IOException e1) { ;
                e1.printStackTrace();
            }
            e.printStackTrace();
            return 1;
        }
    }

    public int getdepth() {
        //Check Adapter is enabled

        try {
            OutputStream outpacket = thesocket.getOutputStream();
            InputStream inpacket = thesocket.getInputStream();
            outpacket.write('m');
            int recv = inpacket.read();
            return recv;
        } catch (IOException e) {
            try {
                if (thesocket != null)
                    thesocket.close();
                thesocket = null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return 0;
    }

}
