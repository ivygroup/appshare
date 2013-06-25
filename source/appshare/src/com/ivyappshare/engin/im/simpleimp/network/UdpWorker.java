package com.ivyappshare.engin.im.simpleimp.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import android.os.AsyncTask;
import android.util.Log;

import com.ivyappshare.engin.control.LocalSetting;
import com.ivyappshare.engin.im.Person;
import com.ivyappshare.engin.im.simpleimp.protocol.PackageReceive;
import com.ivyappshare.engin.im.simpleimp.protocol.ProtocolUnPack;
import com.ivyappshare.engin.im.simpleimp.protocol.VersionControl;

public class UdpWorker extends Worker {
    private static String TAG = "UdpWorker";
    
    private DatagramSocket mDatagramSocket;
    private Person mBroadcastPerson;
    private Person mBroadcastPerson2;
    
    private PackageListener mPackageListener;
    private UdpRecvThread mRecvThread;
    
    protected UdpWorker() {
        mDatagramSocket = null;
        mBroadcastPerson = null;
        mBroadcastPerson2 = null;

        CreateSocketTask a = new CreateSocketTask();
        a.execute();
        
        mPackageListener = null;
        
        mRecvThread = new UdpRecvThread();
        mRecvThread.start();
    }

    @Override
    public void broadcast(String message, Person from) {
        if (mBroadcastPerson == null) {
            mBroadcastPerson = new Person();
            InetAddress ip = null;
            byte[] bs = new byte[]{(byte)255,(byte)255,(byte)255,(byte)255};
            try {
                ip = InetAddress.getByAddress(bs);
            } catch(java.net.UnknownHostException e){
                e.printStackTrace();
            }

            mBroadcastPerson.mIP = ip;
            mBroadcastPerson.mPort = LocalSetting.getInstance().getMySelf().mPort;
        }
        if (mBroadcastPerson2 == null) {
            if (LocalSetting.getInstance().getBroadCastAddress() != null) {
                mBroadcastPerson2 = new Person();
                mBroadcastPerson2.mIP = LocalSetting.getInstance().getBroadCastAddress();
                mBroadcastPerson2.mPort = LocalSetting.getInstance().getMySelf().mPort;
            }
        }

        if (mBroadcastPerson != null) {
            send_l(message, from, mBroadcastPerson, true);
        }

        if (mBroadcastPerson2 != null) {
            // Log.v(TAG, "The boradcast ip = " + mBroadcastPerson2.mIP.toString());
            send_l(message, from, mBroadcastPerson2, true);
        }
    }

    @Override
    public void send(String message, Person from, Person to) {
        send_l(message, from, to, false);
    }

    @Override
    public void sendRaw(String standardHead, byte []rawdata, Person from, Person to) {
        if (mDatagramSocket == null) {
            Log.e(TAG, "no datagramsocket, can't send message.");
            return;
        }

        if (!VersionControl.isIvyVersion(to.mProtocolVersion)) {
            return;
        }
        
        byte p[] = standardHead.getBytes();
        byte thebytes[] = new byte[p.length + rawdata.length];
        System.arraycopy(p, 0, thebytes, 0, p.length);
        System.arraycopy(rawdata, 0, thebytes, p.length, rawdata.length);

        send_l(thebytes, to);
    }

    private void send_l(String message, Person from, Person to, boolean isBroadcast) {
        if (mDatagramSocket == null) {
            Log.e(TAG, "no datagramsocket, can't send message.");
            return;
        }

        byte thebytes[] = null;
        try {
            if (isBroadcast) {
                thebytes = message.getBytes(from.mNetworkCoding);
            } else {
                thebytes = message.getBytes(to.mDynamicStatus.myEncodes.mDecode);
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return;
        }

        send_l(thebytes, to);
    }

    private void send_l(byte[] data, Person to) {
        DatagramPacket dpack = new DatagramPacket(
                data,
                data.length,
                to.mIP,
                to.mPort);
        //synchronized (mDatagramSocket) {
            if (mDatagramSocket == null || dpack == null || mDatagramSocket.isClosed()) {
                Log.i(TAG, "mDatagramSocket is null, so can't send this message.");
                return;
            }
            try {
                mDatagramSocket.send(dpack);
            } catch (IOException ex) {
                Log.e(TAG, "send message to " + to.mNickName + "(" + to.mIP.getHostAddress() + ") failed. " + ex.getMessage());
            } catch (NullPointerException exception) {
                Log.e(TAG, "Null pointer exception when send msg. " + exception.getMessage());
            }
        //}
    }

    @Override
    public void registrPackageListener(PackageListener listener) {
        mPackageListener = listener;
    }

    @Override
    public void release() {
        mRecvThread.setStop(true);
        try {
            mRecvThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "when join the udp receive thread, has a error. " + e.getMessage());
        }
    }


    // 
    private class CreateSocketTask extends AsyncTask<Integer, Integer, DatagramSocket> {
        @Override
        protected DatagramSocket doInBackground(Integer... params) {
            Log.d(TAG, "Entry CreateUdpTask.");

            // to create udp socket.
            DatagramSocket mySocket = null;
            try {
                mySocket = new DatagramSocket(null);
                mySocket.setReuseAddress(true);
                mySocket.bind(new InetSocketAddress(LocalSetting.getInstance().getMySelf().mPort));
                // mySocket = new DatagramSocket(mMyself.mPort);
            } catch (Exception e) {
                Log.e(TAG, "can't create socket on udp port. " + e.getMessage());
                e.printStackTrace();
            }

            Log.d(TAG, "Exit CreateUdpTask.");

            return mySocket;
        }

        @Override
        protected void onPostExecute(DatagramSocket mysocket) {
            completeCreateUdpSocket(mysocket);
        }
    }
    
    private void completeCreateUdpSocket(DatagramSocket mysocket) {
        // Log.d(TAG, "completeCreateUdpSocket in");
        mDatagramSocket = mysocket;
        // Log.d(TAG, "completeCreateUdpSocket out");
    }
    
    

    private class UdpRecvThread extends Thread {
        private boolean mIsStop;

        public UdpRecvThread() {
            mIsStop = false;
        }

        public void setStop(boolean isStop) {
            //synchronized (mDatagramSocket) {
                if (mDatagramSocket != null) {
                    mDatagramSocket.close();
                }
            //}
            mIsStop = isStop;
        }

        public void run() {
            final int MAXBUF = 8192;

            while (!mIsStop) {
                if (mDatagramSocket == null || mPackageListener == null) {
                    continue;
                }

                byte[] recvsocketbuf = new byte[MAXBUF];
                DatagramPacket datapkt = new DatagramPacket(recvsocketbuf, recvsocketbuf.length);
                try {
                    mDatagramSocket.receive(datapkt);
                } catch (IOException e) {
                    Log.i(TAG, "catch a execption when receive udp packet. " + e.getMessage());
                    continue;
                }

                PackageReceive packageReceive = ProtocolUnPack.unPack(datapkt);
                if (packageReceive.mIsValied) {
                    mPackageListener.receivedPackage(packageReceive);           
                } else {
                    Log.e(TAG, "The received data not valid.");
                }
            }

            // synchronized (mDatagramSocket) {
                mDatagramSocket = null;
            // }

            Log.d(TAG, "exit the thread.");
        }
    }
}
