package com.ivy.appshare.engin.control;

import java.io.File;
import java.util.HashMap;

import org.apache.http.impl.conn.IdleConnectionHandler;

import android.content.ContentProviderClient;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.ivy.appshare.MyApplication;
import com.ivy.appshare.engin.constdefines.IvyMessages;
import com.ivy.appshare.engin.data.ImData;
import com.ivy.appshare.engin.data.Table_GroupMessage;
import com.ivy.appshare.engin.data.Table_Message;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.utils.CommonUtils;

public class TranslateFileControl {
    private static final String TAG = "TranslateFileControl";


    private HashMap<String, Integer> mMapReceiveFileId;     // recceived files.  from im id to databases id.
    private HashMap<String, String> mMapOtherFilePersonId;  // scan file.  file path, person key + id.
    private TransProcessListener mTransListener;
    private GroupTransProcessListener mGroupTransListener;
    private PersonMessages mPersonMessages;
    private GroupMessages mGroupMessages;
    private ImData mImData;

    public TranslateFileControl(ImData imData, PersonMessages personMessages, GroupMessages groupMessages) {
        mMapReceiveFileId = new HashMap<String, Integer>();
        mMapOtherFilePersonId = new HashMap<String, String>();
        mImData = imData;
        mPersonMessages = personMessages;
        mGroupMessages = groupMessages;
    }

    public interface TransProcessListener{
        public void onSendFileProcess(int id, Person to, String name, FileType fileType, long pos, long total);
        public void onReceiveProcess(int id, Person from, String name, FileType fileType, long pos, long total); 
    }

    public interface GroupTransProcessListener{
        public void onSendFileProcess(int id, boolean isBroadcast, String groupName, String name, FileType fileType, long pos, long total);
        public void onReceiveProcess(int id, boolean isBroadcast, String groupName, Person from, String name, FileType fileType, long pos, long total); 
    }

    public synchronized void RegisterTransProcess(TransProcessListener listener) {
        mTransListener = listener;
    }

    public synchronized void UnRegisterTransProcess(TransProcessListener listener) {
        mTransListener = null;
    }
    
    public synchronized void RegisterGroupTransProcess(GroupTransProcessListener listener) {
        mGroupTransListener = listener;
    }

    public synchronized void UnRegisterGroupTransProcess(GroupTransProcessListener listener) {
        mGroupTransListener = null;
    }

    synchronized void onSendFileProcess(int id, Person to, String name, FileType fileType, long pos, long total) {
        if (mTransListener != null) {
            mTransListener.onSendFileProcess(id, to, name, fileType, pos, total);
        }
    }

    public synchronized void beginReceiveFile(String id, int databaseID) {
        mMapReceiveFileId.put(id, databaseID);
    }

    public synchronized void onReceiveProcess(Person from, String id, String name, FileType fileType, long pos, long total) {
        if (mTransListener == null) {
            return;
        }
        if (!mMapReceiveFileId.containsKey(id)) {
            return;
        }

        int newId = mMapReceiveFileId.get(id);
        mTransListener.onReceiveProcess(newId, from, name, fileType, pos, total);
    }

    public synchronized boolean endReceiveFile(Person from, String id, String name, FileType fileType) {
        if (!mMapReceiveFileId.containsKey(id)) {
            return false;
        }

        int newId = mMapReceiveFileId.get(id);
        mMapReceiveFileId.remove(id);

        String path = new StringBuilder(LocalSetting.getInstance().getLocalFileReceivePath(fileType)).
                append(name).toString();

        if (fileType.ordinal() == FileType.FileType_OtherFile.ordinal()) {
            Log.d(TAG, "processOtherFile " + path);
            String value = PersonManager.getPersonKey(from) + "/" + newId;
            mMapOtherFilePersonId.put(path, value);

            MediaScannerConnection.scanFile(MyApplication.getInstance(), new String[] {path}, new String[] {null},
                    new OnScanCompletedListener() {
                        
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d(TAG, "onScanCompleted " + path + " uri " + uri);

                            if (!mMapOtherFilePersonId.containsKey(path)) {
                                Log.e(TAG, "Cannot find path " + path + "in the map in onScanCompleted");
                                return;
                            }
                            String value = mMapOtherFilePersonId.get(path);
                            mMapOtherFilePersonId.remove(path);
                            String personKey = value.substring(0, value.lastIndexOf('/'));
                            int id = Integer.valueOf(value.substring(value.lastIndexOf('/')+1));

                            Person from = PersonManager.getInstance().getPerson(personKey);
                            if (from == null) {
                                Log.e(TAG, "Cannot get person by key " + personKey);
                                return;
                            }

                            FileType newFileType = getFileTypeByPathAndUri(path, uri);
                            String newFilePath = path;

                            mImData.updateMessageState(id, Table_Message.STATE_OK);
                            if (newFileType.ordinal() != FileType.FileType_OtherFile.ordinal()) {
                            	newFilePath = LocalSetting.getInstance().getLocalFileReceivePath(newFileType) + 
                            			path.substring(path.lastIndexOf('/')+1);
                            	new File(path).renameTo(new File(newFilePath));
                            	mImData.updateMessageTypeAndContent(id, newFileType.ordinal(), newFilePath);

                                ContentProviderClient mediaProvider = MyApplication.getInstance().
                                		getContentResolver().acquireContentProviderClient("media");
                                try {
									mediaProvider.delete(uri, null, null);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
                                MediaScannerConnection.scanFile(MyApplication.getInstance(), // re-scan file
                                		new String[] {newFilePath}, new String[] {null}, null);
                            }
                            IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_UPDATE, 
                                    Table_Message.STATE_OK, id, newFileType.ordinal(), newFilePath, false, from);
                        }
                    });
        } else {
            switch (fileType) {
                case FileType_Picture:
                case FileType_Music:
                case FileType_Video: {
                    MediaScannerConnection.scanFile(
                            MyApplication.getInstance(),
                            new String[] { path },
                            new String[] { null },
                            null);
                }
                break;
            }
            mImData.updateMessageState(newId, Table_Message.STATE_OK);
            IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_UPDATE, Table_Message.STATE_OK, newId, fileType.ordinal(), path, false, from);
        }
        return true;
    }

    public synchronized boolean removeReceiveFileTaskOnly(String id, String name) {
        if (!mMapReceiveFileId.containsKey(id)) {
            return false;
        }
        int newId = mMapReceiveFileId.get(id);
        mMapReceiveFileId.remove(id);

        mImData.updateMessageState(newId, Table_Message.STATE_FAILED);
        IvyMessages.sendMessageIntent(IvyMessages.VALUE_MESSAGETYPE_UPDATE, Table_Message.STATE_FAILED, newId, -1, name, false, null);

        return true;
    }

    synchronized void onSendGroupFileProcess(int id, boolean isBroadcast, String groupName, 
            String name, FileType fileType, long pos, long total) {
        if (mGroupTransListener != null) {
            mGroupTransListener.onSendFileProcess(id, isBroadcast, groupName, name, fileType, pos, total);
        }
    }

    public synchronized void onReceiveGroupProcess(boolean isBroadcast, String groupName, 
            Person from, String id, String name, FileType fileType, long pos, long total) {
        if (mGroupTransListener == null) {
            return;
        }
        if (!mMapReceiveFileId.containsKey(id)) {
            return;
        }

        int newId = mMapReceiveFileId.get(id);
        mGroupTransListener.onReceiveProcess(newId, isBroadcast, groupName, from, name, fileType, pos, total);
    }

    public synchronized boolean removeReceiveGroupFileTaskOnly(boolean isBroadcast, String groupName, String id, String name) {
        if (!mMapReceiveFileId.containsKey(id)) {
            return false;
        }
        int newId = mMapReceiveFileId.get(id);
        mMapReceiveFileId.remove(id);

        mImData.updateGroupMessageState(newId, Table_Message.STATE_FAILED);
        mGroupMessages.updateGroupMessageState(newId, Table_Message.STATE_FAILED);
        IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_UPDATE, Table_Message.STATE_FAILED, 
                newId, -1, null, isBroadcast, groupName, false);

        return true;
    }

    public synchronized boolean endReceiveGroupFile(boolean isBroadcast, String groupName, Person from, 
            String id, String name, FileType fileType) {
        if (!mMapReceiveFileId.containsKey(id)) {
            return false;
        }

        int newId = mMapReceiveFileId.get(id);
        mMapReceiveFileId.remove(id);

        String path = new StringBuilder(LocalSetting.getInstance().getLocalFileReceivePath(fileType)).
                append(name).toString();

        if (fileType.ordinal() == FileType.FileType_OtherFile.ordinal()) {
            Log.d(TAG, "processOtherGroupFile " + path);
            String value = String.valueOf(isBroadcast) + "/" + groupName + "/" + newId;
            mMapOtherFilePersonId.put(path, value);

            MediaScannerConnection.scanFile(
                    MyApplication.getInstance(), new String[] {path}, new String[] {null},
                    new OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d(TAG, "onScanCompleted " + path + " uri " + uri);

                            if (!mMapOtherFilePersonId.containsKey(path)) {
                                Log.e(TAG, "Cannot find path " + path + "in the map in onScanCompleted");
                                return;
                            }
                            String value = mMapOtherFilePersonId.get(path);
                            mMapOtherFilePersonId.remove(path);
                            boolean isBroadcast = Boolean.valueOf(value.substring(0, value.indexOf('/')));
                            String groupName = value.substring(value.indexOf('/')+1, value.lastIndexOf('/'));
                            int id = Integer.valueOf(value.substring(value.lastIndexOf('/')+1));

                            FileType newFileType = getFileTypeByPathAndUri(path, uri);
                            String newFilePath = path;

                            mImData.updateGroupMessageState(id, Table_Message.STATE_OK);
                            mGroupMessages.updateGroupMessageState(id, Table_Message.STATE_OK);
                            if (newFileType.ordinal() != FileType.FileType_OtherFile.ordinal()) {
                            	newFilePath = LocalSetting.getInstance().getLocalFileReceivePath(newFileType) + 
                            			path.substring(path.lastIndexOf('/')+1);
                            	new File(path).renameTo(new File(newFilePath));

                            	mImData.updateGroupMessageTypeAndContent(id, newFileType.ordinal(), newFilePath);
                                mGroupMessages.updateGroupMessageTypeAndContent(id, newFileType, newFilePath);

                                ContentProviderClient mediaProvider = MyApplication.getInstance().
                                		getContentResolver().acquireContentProviderClient("media");
                                try {
									mediaProvider.delete(uri, null, null);
								} catch (RemoteException e) {
									e.printStackTrace();
								}

                                MediaScannerConnection.scanFile(MyApplication.getInstance(), // re-scan file
                                		new String[] {newFilePath}, new String[] {null}, null);
                            }

                            IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_UPDATE, Table_Message.STATE_OK, 
                                    id, newFileType.ordinal(), newFilePath, isBroadcast, groupName, false);
                        }
                    });
        } else {
            switch (fileType) {
                case FileType_Picture:
                case FileType_Music:
                case FileType_Video: {
                    MediaScannerConnection.scanFile(MyApplication.getInstance(), new String[] {path}, new String[] {null}, null);
                }
                break;
            }
            mImData.updateGroupMessageState(newId, Table_Message.STATE_OK);
            mGroupMessages.updateGroupMessageState(newId, Table_Message.STATE_OK);
            IvyMessages.sendGroupMessageIntent(IvyMessages.VALUE_MESSAGETYPE_UPDATE, Table_Message.STATE_OK, 
                    newId, fileType.ordinal(), path, isBroadcast, groupName, false);
        }
        return true;
    }

    public FileType getFileTypeByPathAndUri(String path, Uri uri) {
        FileType newFileType = FileType.FileType_OtherFile;
        int pos = path.lastIndexOf('.');
        int length = path.length();

        // first compare by uri
        if (uri != null) {
            String strUri = uri.toString();
            // get the file type
            if (strUri.startsWith(CommonUtils.IMAGE_URI)) {
                newFileType = FileType.FileType_Picture;
            } else if (strUri.startsWith(CommonUtils.VIDEO_URI)) {
                newFileType = FileType.FileType_Video;
            } else if (strUri.startsWith(CommonUtils.AUDIO_URI)) {
                newFileType = FileType.FileType_Music;
            }
        }
        // second compare by suffix
        if (newFileType.ordinal() == FileType.FileType_OtherFile.ordinal()) {
            if (length > 4 && pos > 0 && pos < length-3) {
                String suffix = path.substring(pos+1);
                if (suffix.compareToIgnoreCase("APK") == 0) {
                    newFileType = FileType.FileType_App;
                } else if (suffix.compareToIgnoreCase("VCF") == 0) {
                    newFileType = FileType.FileType_Contact;
                }
            }
        }

        return newFileType;
    }
}
