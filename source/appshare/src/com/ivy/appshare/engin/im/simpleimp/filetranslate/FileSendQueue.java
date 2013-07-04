package com.ivy.appshare.engin.im.simpleimp.filetranslate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.Im.FileType;
import com.ivy.appshare.engin.im.simpleimp.protocol.Sender;

public class FileSendQueue {
    private static final String TAG = "FileSendQueue";

    //
    private static Sender gSender = null;
    private static HashMap<String, FileSendQueue> gInstance = null;
    public static FileSendQueue getInstance(Person toPerson) {
        if (toPerson == null || toPerson.mIP == null) {
            return null;
        }

        String key = toPerson.mIP.getHostAddress();
        if (!gInstance.containsKey(key)) {
            FileSendQueue fileSendQueue = new FileSendQueue(gSender);
            gInstance.put(key, fileSendQueue);
            return fileSendQueue;
        } else {
            return gInstance.get(key);
        }
    }

    public static List<FileSendQueue> getInstance() {
        return new ArrayList<FileSendQueue>(gInstance.values());
    }

    public static void createInstance(Sender sender) {
        if (gInstance != null) {
            return;
        }

        gSender = sender;
        gInstance = new HashMap<String, FileSendQueue>();
    }

    public static void destroyInstance() {
        if (gInstance == null) {
            return;
        }

        ArrayList<FileSendQueue> array = new ArrayList<FileSendQueue>(gInstance.values());
        for (int i = 0; i < array.size(); ++ i) {
            FileSendQueue queue = array.get(i);
            queue.stopSendThread();
        }
        gInstance.clear();
        gInstance = null;
    }

    //
    private class Task {
        public int mSenderType;
        public long mUserId;
        public Person mToPerson;
        public String mMsg;
        public String mFilename;
        public FileType mFileType;

        public Task(int senderType, long id, Person to, String msg, String filename, FileType type) {
            mSenderType = senderType;
            mUserId = id;
            mToPerson = to;
            mMsg = msg;
            mFilename = filename;
            mFileType = type;
        }
    }
    private Queue<Task> mTasks;
    private SendThread mSendThread;
    private boolean mStop;
    private Sender mSender;
    private boolean mIsSending;

    private FileSendQueue(Sender sender) {
        mTasks = new LinkedBlockingQueue<FileSendQueue.Task>();
        mStop = false;
        mSendThread = new SendThread();
        mSendThread.start();
        mSender = sender;
        mIsSending = false;
    }


    public static final int SenderType_Common = 1;
    public static final int SenderType_Group = 2;
    public synchronized void addTask(int senderType, long id, Person to, String msg, String filename, FileType type) {
        Task task = new Task(senderType, id, to, msg, filename, type);
        if (!mTasks.offer(task)) {
            Log.e(TAG, "the queue is full, cant add new task.");
        }
        mSendThread.interrupt();
    }

    public synchronized void finishTask(long id) {
        Task task = mTasks.peek();
        if (task == null || task.mUserId != id) {
            Log.e(TAG, "when finish task occore a error, not match the head task.");
            return;
        }
        mTasks.poll();
        mIsSending = false;
        mSendThread.interrupt();
    }

    public synchronized boolean removeTask(long id) {
        Task task = mTasks.peek();
        if (task == null) {
            return false;
        }
        if (task != null && task.mUserId == id) {
            Log.e(TAG, "Cant remove current task.");
            return false;
        }

        for (Task tmp: mTasks) {
            if (tmp != null && tmp.mUserId == id) {
                return mTasks.remove(tmp);
            }
        }
        return false;
    }

    public synchronized void clear() {
        mTasks.clear();
        mIsSending = false;
        mSendThread.interrupt();
    }

    private synchronized Task getTheHeadTask() {
        return mTasks.peek();
    }

    private synchronized void lockTask() {
        mIsSending = true;
    }

    private void stopSendThread() {
        mStop = true;

        mSendThread.interrupt();
        try {
            mSendThread.join();
        } catch (Exception e) {
            Log.i(TAG, "when join mSendThread, occore a error. " + e.getMessage());
        }
    }


    private class SendThread extends Thread {
        @Override
        public void run() {
            while (!mStop) {
                if (mIsSending) {
                    try {
                        Thread.sleep(1000 * 60 * 5);
                    } catch (InterruptedException e) {
                        //　no code.
                    }
                    continue;
                }

                Task task = getTheHeadTask();
                if (task == null) {
                    try {
                        Thread.sleep(1000 * 60 * 5);
                    } catch (InterruptedException e) {
                        //　no code.
                    }
                    continue;
                }

                if (task.mSenderType == SenderType_Common) {
                    mSender.sendFile(task.mUserId, task.mToPerson, task.mMsg, task.mFilename, task.mFileType);
                    lockTask();
                } else if (task.mSenderType == SenderType_Group) {
                    mSender.sendGroupFile(task.mUserId, task.mToPerson, task.mMsg, task.mFilename, task.mFileType);
                    lockTask();
                } else {
                    Log.e(TAG, "not right sender type. please check the code");
                }
            }
        }
    }
}
