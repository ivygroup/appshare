package com.ivy.appshare.ui;

public class IvyInnerMessage {
    private static final String IVY_APP_INNER_MESSAGE = "IVY_APP_INNER_MESSAGE";
    public static final int IVY_APP_IAMHOTSPOT  = 0;
    public static final int IVY_APP_REQUEST     = 1;
    public static final int IVY_APP_ANSWERYES   = 2;
    public static final int IVY_APP_ANSWERNO    = 3;

    public static int parseIvyInnerMessage(String message) {
        if (!message.startsWith(IVY_APP_INNER_MESSAGE)) {
            return -1;
        }
        return Integer.valueOf(message.substring(message.lastIndexOf('_')+1));
    }

    public static String getIvyInnerMessage(int msgType) {
        String message = new StringBuilder(IVY_APP_INNER_MESSAGE).append('_').append(msgType).toString();
        return message;
    }
}
