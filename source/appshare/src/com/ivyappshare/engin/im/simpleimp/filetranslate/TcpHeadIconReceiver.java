package com.ivyappshare.engin.im.simpleimp.filetranslate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import android.util.Log;

import com.ivyappshare.engin.control.LocalSetting;
import com.ivyappshare.engin.control.LocalSetting.UserIconEnvironment;
import com.ivyappshare.engin.im.Person;
import com.ivyappshare.engin.im.simpleimp.protocol.ImMessages;
import com.ivyappshare.engin.im.simpleimp.protocol.PackageSend;
import com.ivyappshare.engin.im.simpleimp.util.NotifactionEngin;

public class TcpHeadIconReceiver extends Thread {
    private static final String TAG = TcpHeadIconReceiver.class.getSimpleName();
    private static final int BUFFE_LENGTH = ImMessages.DEFAULT_BUFFER_LENGTH;


    private static HashMap<String, Person> gMapDownloadingPersons = new HashMap<String, Person>();  // key is ip
    public synchronized static boolean isDownloading(Person p) {
        if (gMapDownloadingPersons == null) {
            gMapDownloadingPersons = new HashMap<String, Person>();  // key is ip
        }
        if (p == null || p.mIP == null) {
            return true;
        }
        if (gMapDownloadingPersons.containsKey(p.mIP.getHostAddress())) {
            return true;
        } else {
            return false;
        }
    }
    private synchronized static void addNewDownload(Person p) {
        gMapDownloadingPersons.put(p.mIP.getHostAddress(), p);
    }
    private synchronized static void removeDownload(Person p) {
        gMapDownloadingPersons.remove(p.mIP.getHostAddress());
    }


    private Person mFromPerson;
    private NotifactionEngin mNotifactionEngin;
    private UserIconEnvironment mUserIconEnvironment;

    public TcpHeadIconReceiver(Person from, NotifactionEngin engin) {
        mFromPerson = from;
        mNotifactionEngin = engin;
        mUserIconEnvironment = LocalSetting.getInstance().getUserIconEnvironment();
    }

    @Override
    public void run() {
        if (mFromPerson == null || mFromPerson.mIP == null) {
            return;
        }
        if (isDownloading(mFromPerson)) {
            return;
        }
        addNewDownload(mFromPerson);

        Socket socket = null;
        BufferedOutputStream bufferedOutputStream = null;
        BufferedOutputStream output2file = null;
        BufferedInputStream inputFromSocket = null;
        
        try {
            socket = new Socket(mFromPerson.mIP.getHostAddress(), LocalSetting.getInstance().getMySelf().mPort);
            // Log.d(TAG, "Connect the sender successfully, sender ip = " + mFromPerson.mIP.getHostAddress());
            
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());     

            // Receive file
            String filenameString = mUserIconEnvironment.getFriendHeadFullPath(mFromPerson.mImage);
            File receiveFile = new File(filenameString);

            //Now: If the file is exist, delete it.
            if (receiveFile.exists()) {
                receiveFile.delete();
            }

            //Send the Tcp Msg to receive file data.
            PackageSend packageSend = PackageSend.createCommand(
                    LocalSetting.getInstance().getMySelf(),
                    mFromPerson, ImMessages.IVY_GETHEADICON, null);

            byte []sendmsg = packageSend.mSendMessage.toString().getBytes();      
            bufferedOutputStream.write(sendmsg, 0, sendmsg.length);
            bufferedOutputStream.flush();

            // Begin to receive file....
            Log.d(TAG, "Begin to receive file....");
            output2file = new BufferedOutputStream(new FileOutputStream(receiveFile));
            inputFromSocket = new BufferedInputStream(socket.getInputStream());

            byte[] readBuffer = new byte[BUFFE_LENGTH];
            int len = 0;
            while ((len = inputFromSocket.read(readBuffer)) != -1) {
                // Log.d(TAG, "read len = " + len);
                output2file.write(readBuffer, 0, len);
                output2file.flush();
            }

            mNotifactionEngin.onSomeoneHeadIcon(mFromPerson);
            Log.d(TAG, "receive file successfully, filename = " + filenameString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e(TAG, "remote ip address error");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Create file failed");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO ERROR");
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bufferedOutputStream = null;
            }

            if (output2file != null) {
                try {
                    output2file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                output2file = null;
            }

            if (inputFromSocket != null) {
                try {
                    inputFromSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputFromSocket = null;
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }

        }

        removeDownload(mFromPerson);
    } // end run
}
