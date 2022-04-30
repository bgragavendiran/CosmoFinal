package com.adiuvo.cosmodevice;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adiuvo.cosmodevice.PersistentServices.BluetoothService;

public class MainActivity extends AppCompatActivity {
    BluetoothService mService;
String TAG="MainActivity.class";
    boolean mBound = false;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: CLICKED");
                if (mBound) {
                    // Call a method from the LocalService.
                    // However, if this call were something that might hang, then this request should
                    // occur in a separate thread to avoid slowing down the activity performance.
                    int num = mService.getRandomNumber();
                    Toast.makeText(MainActivity.this, "number: " + num, Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(MainActivity.this, BluetoothService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) service;
            mService = binder.getService();
            Log.d(TAG, "onServiceConnected: ");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}