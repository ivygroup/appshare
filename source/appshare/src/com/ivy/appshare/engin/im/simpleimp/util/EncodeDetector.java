package com.ivy.appshare.engin.im.simpleimp.util;

import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Person.MyEncodes;
import com.ivy.appshare.engin.im.simpleimp.protocol.ImMessages;
import com.ivy.appshare.engin.im.simpleimp.protocol.VersionControl;

public class EncodeDetector {
    private static final String TAG = "EncodeDetector";
    private static final int MAX_MESSAGE_TO_DETECT = 10;

    public static void detect(Person from, byte[] data) {
        Person person = PersonManager.getInstance().getPersonByIP(from.mIP);
        if (person != null) {
            detect_l(person, data);
            from.mDynamicStatus = person.mDynamicStatus;
        } else {
            detect_l(from, data);
        }
    }

    private static void detect_l(Person from, byte[] data) {
        if (from.mDynamicStatus.myEncodes.mHasDetected) {
            return;
        }

        if (VersionControl.isIvyVersion(from.mProtocolVersion)) {
            /*if ( LocalSetting.getInstance().getMySelf().mIP.getHostAddress().compareTo(from.mIP.getHostAddress()) != 0) {
                Log.d(TAG, "this is our user, using utf-8 encode. " + from.mIP.getHostAddress());
            }*/
            from.mDynamicStatus.myEncodes.mHasDetected = true;
            from.mDynamicStatus.myEncodes.mDecode = ImMessages.DEFAULT_ENCODE;
            return;
        }

        String preString = new String(data);
        String tmp[] = preString.split(":", 6);
        if (tmp.length < 6) {
            return;
        }

        long command = ImMessages.GET_MODE(Long.parseLong(tmp[4]));
        long option = ImMessages.GET_OPT(Long.parseLong(tmp[4]));
        if (command == ImMessages.IPMSG_BR_ENTRY
                || command == ImMessages.IPMSG_ANSENTRY
                || command == ImMessages.IPMSG_BR_ABSENCE) {
            if (from.mDynamicStatus.myEncodes.mHasDetectFirstMsg) {
                return;
            } else {
                String enc = native_possibleEncoding(data);
                // Log.d(TAG, "the enc is " + enc);
                setEncode(from.mDynamicStatus.myEncodes, enc);
                from.mDynamicStatus.myEncodes.mHasDetectFirstMsg = true;
            }
        } else if ((command == ImMessages.IPMSG_SENDMSG)
                && (option & ImMessages.IPMSG_FILEATTACHOPT) == 0) {
            String enc = native_possibleEncoding(data);
            setEncode(from.mDynamicStatus.myEncodes, enc);
        } else if (command == ImMessages.IPMSG_GETFILEDATA) {
            String enc = native_possibleEncoding(data);
            setEncode(from.mDynamicStatus.myEncodes, enc);
        } else {
            return;
        }
    }

    private static void setEncode(MyEncodes myEncodes, String enc) {
        if (enc.equals("ASCII")) {
            /*if (!myEncodes.mHasDetected) {
                myEncodes.mDecode = "utf-8";
            }*/
            return;
        }

        if (myEncodes.mMapEncodes.containsKey(enc)) {
            Integer count =  myEncodes.mMapEncodes.get(enc);
            count++;
            myEncodes.mMapEncodes.put(enc, Integer.valueOf(count));
        } else {
            myEncodes.mMapEncodes.put(enc, Integer.valueOf(1));
        }

        String resultEnc = calculateMaxEncode(myEncodes.mMapEncodes);
        if (myEncodes.mMapEncodes.get(resultEnc) >= MAX_MESSAGE_TO_DETECT
                && resultEnc.equals(enc)) {
            Log.d(TAG, "detected the person's encode. " + myEncodes.mDecode);
            myEncodes.mDecode = resultEnc;
            myEncodes.mHasDetected = true;
        } else {
            myEncodes.mDecode = enc;
        }
    }

    private static String calculateMaxEncode(HashMap<String, Integer> mapEncodes) {
        Iterator it = mapEncodes.entrySet().iterator();

        HashMap.Entry maxentry = null;
        while (it.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) it.next();
            String key = (String)entry.getKey();
            Integer value = (Integer)entry.getValue();

            if (maxentry == null) {
                maxentry = entry;
            } else {
                Integer maxvalue = (Integer)maxentry.getValue();
                if (value > maxvalue) {
                    maxentry = entry;
                }
            }
        }
        
        if (maxentry != null) {
            return (String)maxentry.getKey();
        } else {
            return ImMessages.DEFAULT_ENCODE;
        }
    }

    static{
        System.loadLibrary("getip");
    }

    private native static String native_possibleEncoding(byte[] data);
}
