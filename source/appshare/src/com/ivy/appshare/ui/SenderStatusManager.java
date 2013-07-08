package com.ivy.appshare.ui;

import com.ivy.appshare.engin.control.PersonManager;
import com.ivy.appshare.engin.im.Person;

public class SenderStatusManager {
    private Status mStatus;
    private Person mPersonTo;

    public enum Status {
        NOTREADY,
        READY,
        ASKDLG,
        WORKING;
    }

    public SenderStatusManager() {
        mStatus = Status.NOTREADY;
        mPersonTo = null;
    }

    public void setStatus(Status status) {
        mStatus = status;
        if (status != Status.WORKING) {
            mPersonTo = null;
        }
    }

    public void setToPersonForWorking(Person to) {
        mPersonTo = to;
    }

    public boolean isReady() {
        return mStatus == Status.READY;
    }

    public boolean canEndCurrentWorkSession(Person to) {
        if (mStatus != Status.WORKING) {
            return false;
        }
        if (mPersonTo == null || to == null) {
            return false;
        }
        String personKey1 = PersonManager.getPersonKey(mPersonTo);
        String personKey2 = PersonManager.getPersonKey(to);
        if (personKey1.equals(personKey2)) {
            return true;
        } else {
            return false;
        }
    }
}
