package com.ivy.appshare.engin.im.simpleimp.util;

import java.util.List;

import android.util.Log;

import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.im.Im;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.simpleimp.protocol.Sender;

public class KeepAlive extends Thread {
    private static final String TAG = KeepAlive.class.getSimpleName();
    private static final int SLEEP_TIME = 1000 * 3; // 3 sec
    private static final int ALLOW_ALIVE_TIME = 1000 * 15;  // 15 sec

    private boolean mStop;
    private Sender mSender;
    private NotifactionEngin mNotifactionEngin;

    public KeepAlive(Sender sender, NotifactionEngin notifactionEngin) {
        mStop = false;
        mSender = sender;
        mNotifactionEngin = notifactionEngin;
    }

    public void stopMe() {
        mStop = true;
    }
    
    @Override
    public void run() {
        while (!mStop) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }

            if (mStop) {
                continue;
            }

            if (LocalSetting.getInstance().getMySelf().mState == Im.State_OffLine) {
                continue;
            }

            // check timeout person.
            List<Person> persons = PersonManager.getInstance().getPersonList();
            for (int i = 0; i < persons.size(); ++i) {
                Person p = persons.get(i);
                if (p == null) {
                    continue;
                }
                checkOnePerson(p);
            } // end for

            mSender.upLine();
        } // end while
    }

    private void checkOnePerson(Person person) {
        long curTime = System.currentTimeMillis();
        long lastTime = person.mDynamicStatus.lastActiveTime;

        if (lastTime == 0) {
            return;
        }

        if (person.isOnline()) {
            long duration = curTime - lastTime;
            if (duration > ALLOW_ALIVE_TIME) {
                Person tmpPerson = PersonManager.getInstance().removePerson(person);
                mNotifactionEngin.onSomeoneExit(tmpPerson);
                Log.i(TAG, "person " + person.mNickName + " (" + person.mIP.getHostAddress().toString()
                        + ") alive timeout, remove it. time duration = " + duration/1000);
            }
            /*if (duration > SLEEP_TIME) {
                Log.i(TAG, "person " + p.mNickName + " (" + p.mIP.getHostAddress().toString()
                        + ").  time duration = " + duration/1000);
            } //*/
        }
    }
}
