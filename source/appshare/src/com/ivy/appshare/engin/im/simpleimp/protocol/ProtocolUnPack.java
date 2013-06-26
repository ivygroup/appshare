package com.ivy.appshare.engin.im.simpleimp.protocol;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Socket;

import android.util.Log;

import com.ivy.appshare.engin.im.simpleimp.util.EncodeDetector;

public class ProtocolUnPack {
    private static String TAG = "ProtocolUnpack";

    public static PackageReceive unPack(DatagramPacket packet) {
        PackageReceive receive = new PackageReceive();
        receive.mPersonOther.mIP = packet.getAddress();
        receive.mPersonOther.mPort = packet.getPort();
        receive.mIsUdp = true;

        /*Log.d(TAG, "Receive a udp data from ("
                + receive.mPersonOther.mIP.getHostAddress() + ", "
                + receive.mPersonOther.mPort + ")"); */

        if (packet.getData() == null || packet.getLength() <= 0) {
            return receive;
        }
        
        byte data[] = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());

        return unPack_l(receive, data);
    }

    public static PackageReceive unPack(Socket socket, byte[] data) {
        PackageReceive toReturn = new PackageReceive();
        toReturn.mPersonOther.mIP = socket.getInetAddress();
        toReturn.mPersonOther.mPort = socket.getPort();
        toReturn.mIsUdp = false;

        return unPack_l(toReturn, data);
    }

    private static PackageReceive unPack_l(PackageReceive toReturn, byte[] data) {
        // extract protocol version.
        String str = new String(data);
        String tmp[] = str.split(":", 6);
        if (tmp.length >= 6) {
            toReturn.mPersonOther.mProtocolVersion = tmp[0];
            toReturn.mPacketNo = tmp[1];
            toReturn.mCommand = Long.parseLong(tmp[4]);
        } else {
            toReturn.mPersonOther.mProtocolVersion = ImMessages.IPMSG_VERSION;
        }

        // detect encode.
        EncodeDetector.detect(toReturn.mPersonOther, data);
        String datatmpString = null;
        try {
            datatmpString = new String(data, toReturn.mPersonOther.mDynamicStatus.myEncodes.mDecode);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "not support the encode. " + e.getMessage());
            return null;
        }

        // unpack 
        return unPack_l(toReturn, datatmpString);
    }

    private static PackageReceive unPack_l(PackageReceive toReturn, String data) {
        String tmp[] = data.split(":", 6);
        if (tmp.length < 6) {
            Log.d(TAG, "received a package from (" + toReturn.mPersonOther.mIP.getHostAddress().toString()
                    + "), but it is not valid. " + data);
            toReturn.mIsValied = false;
            return toReturn;
        }

        /*Log.d(TAG, "received a package from (" + toReturn.mPersonOther.mIP.getHostAddress().toString()
                + "), " + datatmpString);
        Log.d(TAG, "command = " + tmp[4]);*/
        
        toReturn.mPersonOther.mProtocolVersion = tmp[0];
        toReturn.mPacketNo = tmp[1];
        toReturn.mPersonOther.mName = tmp[2];
        toReturn.mPersonOther.mHost = tmp[3];
        toReturn.mPersonOther.mMac = VersionControl.getMacAddressFromVersion(tmp[0]);
        if (toReturn.mPersonOther.mMac == null) {
            toReturn.mPersonOther.mMac = toReturn.mPersonOther.mIP.getHostAddress().toString() + ", " + toReturn.mPersonOther.mName;
        }
        toReturn.mCommand = Long.parseLong(tmp[4]);
        toReturn.mAdditionalSection = (tmp[5]).replace("\r\n", "\n");
        toReturn.mIsValied = true;
        return toReturn;
    }



    private static PackageReceive preUnPackRawData(PackageReceive toReturn, byte[] data) {
        // This function is for IPTUX_SENDICON, but we now use TCP to translate headicon, so now not use this method.
        int i = 0;
        for (i = 0; i < data.length; i++)
            if (data[i] == 0)
                break;

        if (i < data.length)
        {
            byte[] tmpbuf = new byte[i];
            System.arraycopy(data, 0, tmpbuf, 0, i);
            String preString = null;
            /*try
            {
                preString = new String(tmpbuf, LocalSetting.getInstance().getMySelf().mNetworkCoding);
            } catch (UnsupportedEncodingException ex)
            {
                ex.printStackTrace();
                return null;
            } */
            preString = new String(tmpbuf);

            String tmp[] = preString.split(":", 6);
            if (tmp.length >= 6) {
                long command = ImMessages.GET_MODE(Long.parseLong(tmp[4]));
                if (command == ImMessages.IPTUX_SENDICON) {
                    // Log.d(TAG, "receaved a IPTUX_SENDICON command, so we only have rawdata. command = 0X" + Long.toHexString(command));
                    toReturn.mPersonOther.mProtocolVersion = tmp[0];
                    toReturn.mPacketNo = tmp[1];
                    toReturn.mPersonOther.mName = tmp[2];
                    toReturn.mPersonOther.mHost = tmp[3];
                    toReturn.mPersonOther.mMac = VersionControl.getMacAddressFromVersion(tmp[0]);
                    if (toReturn.mPersonOther.mMac == null) {
                        toReturn.mPersonOther.mMac = toReturn.mPersonOther.mIP.getHostAddress().toString() + ", " + toReturn.mPersonOther.mName;
                    }
                    toReturn.mCommand = Long.parseLong(tmp[4]);
                    toReturn.mAdditionalSection = null;
                    toReturn.mAdditionalRawData = new byte[data.length - i - 1];
                    System.arraycopy(data, i+1, toReturn.mAdditionalRawData, 0, data.length - i - 1);
                    toReturn.mIsValied = true;
                    return toReturn;
                }
            }
        }

        return null;
    }
}
