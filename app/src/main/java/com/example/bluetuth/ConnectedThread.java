package com.example.bluetuth;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    BluetoothSocket socket = null;
    private final InputStream inStream;
    private final OutputStream outStream;

    public ConnectedThread(BluetoothSocket socket) {
        this.socket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        inStream = tmpIn;
        outStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes;

        while(true) {
            try {
                bytes = inStream.read(buffer);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Write(byte[] stream) {
        try {
            outStream.write(stream);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Cancel() {
        try {
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}