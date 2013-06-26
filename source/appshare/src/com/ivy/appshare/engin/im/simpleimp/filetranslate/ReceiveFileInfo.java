package com.ivy.appshare.engin.im.simpleimp.filetranslate;

import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.simpleimp.protocol.ImMessages;
import com.ivy.appshare.engin.im.simpleimp.protocol.VersionControl;

import android.util.Log;

//fileID:filename:size:mtime:fileattr[:extend-attr=val1
//[,val2...][:extend-attr2=...]]:\a:fileID...
//(size, mtime, and fileattr describe hex format.
// If a filename contains ':', please replace with "::".)

public class ReceiveFileInfo {
    //	fileID:filename:size:mtime:fileattr
    private static String TAG = "ReceiveFileInfo";

    public String mRecvFileId;
    public String mRecvFileName;
    public long mRecvFileSize;
    public long mTime;
    public String mFileAttr;
    public FileType mFileType;
    public boolean mIsGroupSend;

    // attachment
    // fileID:filename:size:mtime:fileattr
    //      [:extend-attr=val1[,val2...][:extend-attr2=...]]
    //      :\a:fileID...
    
    public ReceiveFileInfo(String version, String additional) {
        byte[] bt = {0x0a};
        String[] count = additional.split(new String(bt));
        String[] fileinfo = count[0].split(":");

        mRecvFileId = fileinfo[0];    // dec number
        mRecvFileName = fileinfo[1];
        mRecvFileSize = Long.parseLong(fileinfo[2], 16);
        mTime = Long.parseLong(fileinfo[3], 16);
        mFileAttr = fileinfo[4];

        if ((VersionControl.getIvyVersion(version) >= 1) && fileinfo.length >= 6) {
            String []attrvalStrings = fileinfo[5].split("=");
            if (attrvalStrings.length < 2
                    || Long.parseLong(attrvalStrings[0], 16) != ImMessages.IVY_FILE_TYPE_ATTR) {
                mFileType = FileType.FileType_OtherFile;
            } else {
                long value = Long.parseLong(attrvalStrings[1], 16);
                if (value == ImMessages.IVY_FILE_TYPE_VAL_APP) {
                    mFileType = FileType.FileType_App;
                } else if (value == ImMessages.IVY_FILE_TYPE_VAL_CONTACT) {
                    mFileType = FileType.FileType_Contact;
                } else if (value == ImMessages.IVY_FILE_TYPE_VAL_PICTURE) {
                    mFileType = FileType.FileType_Picture;
                } else if (value == ImMessages.IVY_FILE_TYPE_VAL_MUSIC) {
                    mFileType = FileType.FileType_Music;
                } else if (value == ImMessages.IVY_FILE_TYPE_VAL_VIDEO) {
                    mFileType = FileType.FileType_Video;
                } else if (value == ImMessages.IVY_FILE_TYPE_VAL_OTHERFILE) {
                    mFileType = FileType.FileType_OtherFile;
                } else if (value == ImMessages.IVY_FILE_TYPE_VAL_RECORD) {
                    mFileType = FileType.FileType_Record;
                } else {
                    mFileType = FileType.FileType_OtherFile;
                }
            }
        } else {
            mFileType = FileType.FileType_OtherFile;
        }

        if ((VersionControl.getIvyVersion(version) >= 2) && fileinfo.length >= 7) {
            String []attrval = fileinfo[6].split("=");
            if (attrval.length < 2 || Long.parseLong(attrval[0], 16) != ImMessages.IVY_GROUP_CHAT_ATTR) {
                mIsGroupSend = false;
            } else {
                if (Long.parseLong(attrval[1], 16) == ImMessages.IVY_GROUP_CHAT_VAL_TRUE) {
                    mIsGroupSend = true;
                } else {
                    mIsGroupSend = false;
                }
            }
        } else {
            mIsGroupSend = false;
        }

        //		Log.d(TAG, "mSenderFieldId = " + mSenderFieldId 
        //				+ "mSenderFileName = " + mSenderFileName
        //				+ "mSenderFileSize = " + mSenderFileSize
        //				+ "mSenderTime = " + mSenderTime
        //				+ "mFileAttr = " + mFileAttr);
    }

}