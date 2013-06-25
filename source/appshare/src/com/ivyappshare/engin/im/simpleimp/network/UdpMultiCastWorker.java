package com.ivyappshare.engin.im.simpleimp.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

import com.ivyappshare.engin.control.LocalSetting;
import com.ivyappshare.engin.im.Person;
import com.ivyappshare.engin.im.simpleimp.protocol.PackageReceive;
import com.ivyappshare.engin.im.simpleimp.protocol.ProtocolUnPack;

/*
224.0.0.0～224.0.0.255为预留的组播地址（永久组地址），地址224.0.0.0保留不做分配，其它地址供路由协议使用；
224.0.1.0～224.0.1.255是公用组播地址，可以用于Internet；
224.0.2.0～238.255.255.255为用户可用的组播地址（临时组地址），全网范围内有效；
239.0.0.0～239.255.255.255为本地管理组播地址，仅在特定的本地范围内有效。
*/

public class UdpMultiCastWorker extends Worker {
	private static String TAG = "UdpMultiCastWorker";

	private static final int MAXBUF = 8192;

	private static final String MULTICASTADDR = "239.0.12.37";
	private static final int MULTICASTPORT = 8888;


	private PackageListener mPackageListener;
	private UdpMulticastRecvThread mUdpMultiRecvThread;
	private MulticastSocket mMulticastSocket;
	private InetAddress mAddress;

	public UdpMultiCastWorker() {
		mPackageListener = null;
		mUdpMultiRecvThread = null;
		mMulticastSocket = null;
		mAddress = null;

		CreateSocketTask a = new CreateSocketTask();
		a.execute();

		mUdpMultiRecvThread = new UdpMulticastRecvThread();
		mUdpMultiRecvThread.start();
	}

	//
    private class CreateSocketTask extends AsyncTask<Integer, Integer, MulticastSocket> {
        @Override
        protected MulticastSocket doInBackground(Integer... params) {
            Log.d(TAG, "Entry CreateUdpTask.");

            MulticastSocket multicastsocket = null;
            try{
                mAddress = InetAddress.getByName(MULTICASTADDR);
                multicastsocket = new MulticastSocket(MULTICASTPORT);
                multicastsocket.joinGroup(mAddress);
            } catch (SocketException se) {
                se.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Exit CreateUdpTask.");

            return multicastsocket;
        }

        @Override
        protected void onPostExecute(MulticastSocket mysocket) {
            completeCreateUdpSocket(mysocket);
        }
    }

    private void completeCreateUdpSocket(MulticastSocket mysocket) {
        // Log.d(TAG, "completeCreateUdpSocket in");
        mMulticastSocket = mysocket;
        // Log.d(TAG, "completeCreateUdpSocket out");
    }

	@Override
	public void registrPackageListener(PackageListener listener) {
	    mPackageListener = listener;
	}

	@Override
	public void release() {
	    if (mUdpMultiRecvThread != null) {
	        mUdpMultiRecvThread.setStop(true);

	        try {
	            mUdpMultiRecvThread.interrupt();
	            mUdpMultiRecvThread.join();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }

        if (mMulticastSocket != null) {
            try {
                mMulticastSocket.leaveGroup(mAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMulticastSocket.close();
            mMulticastSocket = null;
        }
	}

	@Override
	public void broadcast(String message, Person from) {
	    if (mMulticastSocket == null) {
	        return;
	    }

		byte[] msg = null;
		try {
            msg = message.getBytes(from.mNetworkCoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

		// Log.d(TAG, "broadcast : " + message + ", length = " + msg.length);

		DatagramPacket dp = new DatagramPacket(msg, msg.length, mAddress, MULTICASTPORT);
		try {
            mMulticastSocket.send(dp);
        } catch (IOException e) {
            Log.e(TAG, "send multi broadcast failed. " + e.getMessage());
        }
	}



	private class UdpMulticastRecvThread extends Thread {
		private boolean mIsStop;
		private MulticastSocket mMulticastSocketReceive;
		private InetAddress mAddressReceive;

		public UdpMulticastRecvThread () {
			mIsStop = false;
		}

		public void setStop(boolean isStop) {
			mIsStop = isStop;
			if (mMulticastSocketReceive != null) {
			    try {
			        mMulticastSocketReceive.leaveGroup(mAddressReceive);
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			    mMulticastSocketReceive.close();
			    mMulticastSocketReceive = null;
			}
		}

		public void run() {
            try{
                mAddressReceive = InetAddress.getByName(MULTICASTADDR);
                mMulticastSocketReceive = new MulticastSocket(MULTICASTPORT);
                mMulticastSocketReceive.joinGroup(mAddressReceive);
            } catch (SocketException se) {
                se.printStackTrace();
                return;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

			while (!mIsStop) {
				if (mPackageListener == null) {
                    continue;
                }

				byte[] recvsocketbuf = new byte[MAXBUF];
				DatagramPacket datapkt = new DatagramPacket(recvsocketbuf, recvsocketbuf.length);
				try {
				    mMulticastSocketReceive.receive(datapkt);
				} catch (IOException e) {
					Log.i(TAG, "UdpMultiCastWorker catch a execption : " + e.getMessage());
					continue;
				}

				/*Log.d(TAG, "datapkt.getlength = " + datapkt.getLength() +
						" datapkt.getAddress = " + datapkt.getAddress() +
						"datapkt.getData = " + datapkt.getData());*/

				PackageReceive packageReceive = ProtocolUnPack.unPack(datapkt);
				packageReceive.mPersonOther.mPort = LocalSetting.getInstance().getMySelf().mPort;

				if (packageReceive.mIsValied) {
					mPackageListener.receivedPackage(packageReceive);
				} else {
					Log.e(TAG, "The received data not valid.");
				}
				
			} // end while
		}

	}
	
	
}