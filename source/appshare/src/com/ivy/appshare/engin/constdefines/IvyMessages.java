package com.ivy.appshare.engin.constdefines;

import android.content.Intent;
import android.util.Log;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.engin.connection.ConnectionState;
import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.im.Person;

public class IvyMessages {
    private static final String TAG = "IvyMessages";

    // notifaction state
    public static final int NOTIFICATION_STATE_NONE = 0;
    public static final int NOTIFICATION_STATE_BACKGROUND = 101;
    public static final int NOTIFICATION_STATE_MESSAGE_ONE = 102;
    public static final int NOTIFICATION_STATE_MESSAGE_SOME = 103;
    public static final int NOTIFICATION_STATE_MESSAGE_GROUP= 104;


    // private intent, only for my apk.
    public static final String INTENT_MESSAGE = "com.ivshare.message";
    public static final String INTENT_GROUP_MESSAGE = "com.ivyshare.groupmessage";
    public static final String INTENT_PERSON = "com.ivyshare.person";
    public static final String INTENT_NETWORK_AIRPLANE = "com.ivyshare.network.airplane";	// now not use this intent.
    public static final String INTENT_NETWORK_STATECHANGE = "com.ivyshare.network.statechange";
    public static final String INTENT_NETWORK_FINISHSCANIVYROOM = "com.ivyshare.network.finishscanivyroom";
    public static final String INTENT_NETWORK_DISCOVERYWIFIP2P = "com.ivyshare.network.discoverywifip2p";


    // intent parameter for INTENT_MESSAGE
    public static final String PARAMETER_MESSAGE_TYPE = "parameter_message_type"; // using in INTENT_MESSAGE and INTENT_GROUP_MESSAGE
    public static final String PARAMETER_MESSAGE_ID = "parameter_message_id";
    public static final String PARAMETER_MESSGAE_STATE = "parameter_message_state";
    public static final String PARAMETER_MESSAGE_FILE_TYPE = "parameter_message_file_type";
    public static final String PARAMETER_MESSAGE_FILE_VALUE = "parameter_message_value";
    public static final String PARAMETER_MESSAGE_SELF = "parameter_message_self";
    public static final String PARAMETER_MESSAGE_PERSON = "parameter_message_person";

    // intent parameter value.  // using in INTENT_MESSAGE and INTENT_GROUP_MESSAGE
    public static final int VALUE_MESSAGETYPE_NEW = 100;
    public static final int VALUE_MESSAGETYPE_DELETE = 101;
    public static final int VALUE_MESSAGETYPE_UPDATE = 102;
    public static final int VALUE_MESSAGETYPE_RECOVER = 103;

    // intent parameter for INTENT_GROUP_MESSAGE
    public static final String PARAMETER_GROUP_MESSAGE_TYPE = "parameter_group_message_type"; // using in INTENT_MESSAGE and INTENT_GROUP_MESSAGE
    public static final String PARAMETER_GROUP_MESSAGE_ID = "parameter_group_message_id";
    public static final String PARAMETER_GROUP_MESSAGE_STATE = "parameter_group_message_state";
    public static final String PARAMETER_GROUP_MESSAGE_FILE_TYPE = "parameter_group_message_file_type";
    public static final String PARAMETER_GROUP_MESSAGE_FILE_VALUE = "parameter_group_message_value";
    public static final String PARAMETER_GROUP_MESSAGE_BROADCAST = "parameter_group_message_broadcast";
    public static final String PARAMETER_GROUP_MESSAGE_GROUPNAME = "parameter_group_message_groupname";
    public static final String PARAMETER_GROUP_MESSAGE_SELF = "parameter_group_message_self";

    // intent parameter for INTENT_PERSON
    public static final String PARAMETER_PERSON_TYPE = "parameter_person_type";
    public static final String PARAMETER_PERSON_VALUE = "parameter_person_value";

    // intnet parameter value for INTENT_PERSON TYPE
    public static final int VALUE_PERSONTYPE_NEW_USER                 = 0;
    public static final int VALUE_PERSONTYPE_SOMEONE_ABSENCE          = 1;
    public static final int VALUE_PERSONTYPE_SOMEONE_EXIT             = 3;
    public static final int VALUE_PERSONTYPE_CLEAR_ALL                = 4;
    public static final int VALUE_PERSONTYPE_UNREAD_MESSAGECHANGE     = 5;

    public static final int VALUE_GROUP_UNREAD_MESSAGECHANGE     		= 6;

    // intent parameter for INTENT_NETWORK_AIRPLANE
    public static final String PARAMETER_NETWORK_AIRPLANE_FLAG = "parameter_network_airplane_flag";

    // intent value for INTENT_NETWORK_AIRPLANE
    public static final int VALUE_NETWORK_AIRPLANE_FLAG_TRUE = 1;
    public static final int VALUE_NETWORK_AIRPLANE_FLAG_FALSE = 0;

    // intent parameter for INTENT_NETWORK_STATECHANGE
    public static final String PARAMETER_NETWORK_STATECHANGE_TYPE = "parameter_network_statechange_type"; // the value defined in ConnectionState class.
    public static final String PARAMETER_NETWORK_STATECHANGE_STATE = "parameter_network_statechange_state"; // the value defined in ConnectionState class.
    public static final String PARAMETER_NETWORK_STATECHANGE_SSID = "parameter_network_statechange_ssid";   // the value is the string of ssid.

    // intent value for intent_network_statechange
    //      using the enum of connection.implement.ConnectionManagement.ConnectionState

    // intent parameter for INTENT_NETWORK_FINISHSCANIVYROOM
    public static final String PARAMETER_NETWORK_FINISHSCANIVYROOM_ISCLEAR = "parameter_network_finishscanivyroom_isclear";
    




    //
    public static boolean sendMessageIntent(int messageType, int messageState, int id,
            int type, String content, boolean isMeSay, Person person) {
        Intent intent = new Intent(IvyMessages.INTENT_MESSAGE);
        intent.putExtra(IvyMessages.PARAMETER_MESSAGE_TYPE, messageType);
        intent.putExtra(IvyMessages.PARAMETER_MESSAGE_ID, id);
        intent.putExtra(IvyMessages.PARAMETER_MESSGAE_STATE, messageState);
        intent.putExtra(IvyMessages.PARAMETER_MESSAGE_FILE_TYPE, type);
        intent.putExtra(IvyMessages.PARAMETER_MESSAGE_FILE_VALUE, content);
        intent.putExtra(IvyMessages.PARAMETER_MESSAGE_SELF, isMeSay);
        intent.putExtra(IvyMessages.PARAMETER_MESSAGE_PERSON, PersonManager.getPersonKey(person));
        MyApplication.getInstance().sendOrderedBroadcast(intent, null);
        return true;
    }

    public static boolean sendPersonBroadCast(int type, Person person) {
        Intent intent = new Intent(IvyMessages.INTENT_PERSON);
        intent.putExtra(IvyMessages.PARAMETER_PERSON_TYPE, type);
        intent.putExtra(IvyMessages.PARAMETER_PERSON_VALUE, PersonManager.getPersonKey(person));
        MyApplication.getInstance().sendBroadcast(intent);
        return true;
    }

    public static void sendGroupMessageIntent(int messageType, int messageState, int id,
            int type, String content, boolean isBroadcast, String groupName, boolean isMeSay) {
        Intent intent = new Intent(IvyMessages.INTENT_GROUP_MESSAGE);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_TYPE, messageType);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_ID, id);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_STATE, messageState);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_FILE_TYPE, type);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_FILE_VALUE, content);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_BROADCAST, isBroadcast);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_GROUPNAME, groupName);
        intent.putExtra(IvyMessages.PARAMETER_GROUP_MESSAGE_SELF, isMeSay);

        MyApplication.getInstance().sendOrderedBroadcast(intent, null);
    }

    /*
    public static boolean sendNetworkAirPlaneFlag(boolean isAirPlane) {
        Intent intent = new Intent(IvyMessages.INTENT_NETWORK_AIRPLANE);
        int b = isAirPlane?IvyMessages.VALUE_NETWORK_AIRPLANE_FLAG_TRUE:IvyMessages.VALUE_NETWORK_AIRPLANE_FLAG_FALSE;
        intent.putExtra(IvyMessages.PARAMETER_NETWORK_AIRPLANE_FLAG, b);

        MyApplication.getInstance().sendBroadcast(intent);
        return true;
    }*/
    public static boolean sendNetworkStateChange(int connectionType, int state, String id) {
        if (connectionType == ConnectionState.CONNECTION_TYPE_WIFIP2P
                || ConnectionState.getConnectionTypeByStatus(state) == ConnectionState.CONNECTION_TYPE_WIFIP2P) {
            // TODO: now, we not support wifip2p.
            return true;
        }
        // id: for wifi or ivy_wifi, is ssid.
        //     for wifip2p it is macAddress.
        Log.d(TAG, "sendNetworkStateChange : " + state + ", id = " + id);
        Intent intent = new Intent(IvyMessages.INTENT_NETWORK_STATECHANGE);
        intent.putExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_TYPE, connectionType);
        intent.putExtra(IvyMessages.PARAMETER_NETWORK_STATECHANGE_STATE, state);
        intent.putExtra(PARAMETER_NETWORK_STATECHANGE_SSID, id);

        MyApplication.getInstance().sendBroadcast(intent);
        return true;
    }
    public static boolean sendNetworkFinishScanIvyRoom() {
        Intent intent = new Intent(IvyMessages.INTENT_NETWORK_FINISHSCANIVYROOM);
        intent.putExtra(PARAMETER_NETWORK_FINISHSCANIVYROOM_ISCLEAR, false);

        MyApplication.getInstance().sendBroadcast(intent);
        return true;
    }
    public static boolean sendNetworkClearIvyRoom() {
        Intent intent = new Intent(IvyMessages.INTENT_NETWORK_FINISHSCANIVYROOM);
        intent.putExtra(PARAMETER_NETWORK_FINISHSCANIVYROOM_ISCLEAR, true);

        MyApplication.getInstance().sendBroadcast(intent);
        return true;
    }
    public static boolean sendNetworkDiscoveryWifiP2p() {
        Intent intent = new Intent(IvyMessages.INTENT_NETWORK_DISCOVERYWIFIP2P);

        MyApplication.getInstance().sendBroadcast(intent);
        return true;
    }
}
