package com.ivy.appshare.engin.im.simpleimp.filetranslate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import com.ivy.appshare.engin.im.simpleimp.protocol.ImMessages;

import android.util.Log;

public class TcpHeadIconSender extends Thread {
    private static final String TAG = "TcpHeadIconSender";
    private static final int BUFFER_LENGTH = ImMessages.DEFAULT_BUFFER_LENGTH;

    private Socket mSocket;
    private String mFilePathName;

    public TcpHeadIconSender(String filePathName, Socket socket) {
        mSocket = socket;
        mFilePathName = filePathName;
    }

    @Override
    public void run() {
        if (mFilePathName == null || mSocket == null) {
            return;
        }
        File file = new File(mFilePathName);
        if (!file.exists()) {
            return;
        }

        BufferedOutputStream output2Socket = null;
        BufferedInputStream inputFromFile = null;
        try {
            output2Socket = new BufferedOutputStream(mSocket.getOutputStream());
            inputFromFile = new BufferedInputStream(new FileInputStream(file));

            int rlen = 0;
            long allTransSize = 0;
            byte[] readBuffer = new byte[BUFFER_LENGTH];
            while((rlen = inputFromFile.read(readBuffer)) != -1) {
                output2Socket.write(readBuffer, 0, rlen);
                allTransSize += rlen;
            }
            output2Socket.flush();
            Log.d(TAG, "trans my icon, length = " + allTransSize);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (output2Socket != null) {
                try {
                    output2Socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputFromFile != null) {
                try {
                    inputFromFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (mSocket != null) {
                try {
                    mSocket.shutdownInput();
                } catch (IOException e1) {
                }
                try {
                    mSocket.shutdownOutput();
                } catch (IOException e1) {
                }
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
