package com.ivy.appshare.engin.control;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.ivy.appshare.engin.im.Im;
import com.ivy.appshare.engin.im.Person;

import android.util.Log;

public class PersonManager {
    private static final String TAG = PersonManager.class.getSimpleName();

    private static PersonManager gPersonManager = null;
    public static PersonManager getInstance() {
        if (gPersonManager == null) {
            gPersonManager = new PersonManager();
        }
        return gPersonManager;
    }

    private HashMap<String, Person> mMapPersons;
    private HashMap<String, Person> mMapPersonsByIp;

    private PersonManager() {
        mMapPersons = new HashMap<String, Person>();
        mMapPersonsByIp = new HashMap<String, Person>();
    }

    public void setHistoryPerson(HashMap<String, Person> mapPersons) {
    	Log.d(TAG, "Set history person size " + mapPersons.size());
        synchronized (mMapPersons) {
            mMapPersons.clear();
            mMapPersons.putAll(mapPersons);
        }
    	dumpPersonList();
    }

    public Person addPerson(Person p) {
        if (p == null) {
            return null;
        }
        Person tmpPerson = getPerson(p);
        if (tmpPerson == null) {
        	p.mDynamicStatus.unReadMsgCount = p.mDynamicStatus.MessageCount = 0;
            synchronized (mMapPersons) {
                mMapPersons.put(getPersonKey(p), p);
            }
            mMapPersonsByIp.put(p.mIP.getHostAddress().toString(), p);
            return p;
        } else {
            tmpPerson.copyFromOther(p);
            // may be not store IP before
            mMapPersonsByIp.put(p.mIP.getHostAddress().toString(), tmpPerson);
            return tmpPerson;
        }
    }

    public Person removePerson(Person p) {
        Person tmp = getPerson(p);
        if (tmp != null) {
        	Log.d(TAG, "remove person " + p.mNickName + " Message count:" + tmp.mDynamicStatus.MessageCount);
        	tmp.mState = Im.State_OffLine;
        	mMapPersonsByIp.remove(p.mIP.getHostAddress().toString());
        }
        return tmp;
    }

    public void clearAll() {
        synchronized (mMapPersons) {
        	for(Map.Entry<String, Person> value: mMapPersons.entrySet()) {
        		Person person = value.getValue();
    			person.mState = Im.State_OffLine;
        	}
        }

        mMapPersonsByIp.clear();
    }

    private class ComparatorPerson implements Comparator<Person> {
        public int compare(Person arg0, Person arg1) {
//        	// compare priority
//        	// 1, is active
//        	// 2, unread message count
//        	// 3, conversation message count
//        	// 4, name no case
//        	int value0 = arg0.mDynamicStatus.isActive?1:0;
//        	int value1 = arg1.mDynamicStatus.isActive?1:0;
//        	int diffactive = value1 - value0;
//        	if (diffactive != 0) {
//        		return diffactive;
//        	}
//
//        	int diffunread = arg1.mDynamicStatus.unReadMsgCount - arg0.mDynamicStatus.unReadMsgCount;
//        	if (diffunread != 0) {
//        		return diffunread;
//        	}
//
//        	int diffcount = arg1.mDynamicStatus.MessageCount - arg0.mDynamicStatus.MessageCount;
//        	if (diffcount != 0) {
//        		return diffcount;
//        	}
            if (arg0 == null || arg0.mNickName == null) {
                return -1;
            }
            if (arg1 == null || arg1.mNickName == null) {
                return 1;
            }
            return arg0.mNickName.compareToIgnoreCase(arg1.mNickName);
        }
    }

    private void dumpPersonList() {
    	synchronized (mMapPersons) {
	    	for(Map.Entry<String, Person> value: mMapPersons.entrySet()) {
	    		Person person = value.getValue();
	    		Log.d(TAG, "Person: " + person.mNickName + " Count:" + person.mDynamicStatus.MessageCount
	    				+ " personState:" + person.mState + " UnRead:" + person.mDynamicStatus.unReadMsgCount);
	    	}
    	}
    }

    public List<Person> getPersonList() {
    	//dumpPersonList();

        List<Person> listPerson = null;
        synchronized (mMapPersons) {
            listPerson = new ArrayList<Person>(mMapPersons.values());
        }

        Collections.sort(listPerson, new ComparatorPerson());
        return listPerson;
    }

    public static String getPersonKey(Person person) {
        if (person == null || person.mMac == null) {
            return null;
        }
        // String key = person.mName + person.mIP.getHostAddress().toString();
        String key = new String(person.mMac.replace(":", ""));
        return key;
    }

    public Person getPerson(Person person) {
        if (person == null) {
            return null;
        }
        String key = getPersonKey(person);
        synchronized (mMapPersons) {
            if (mMapPersons.containsKey(key)) {
                return (Person)mMapPersons.get(key);
            }
        }
        return null;
    }

    public Person getPerson(String key) {
        if (key == null) {
            return null;
        }
        synchronized (mMapPersons) {
            if (mMapPersons.containsKey(key)) {
                return (Person)mMapPersons.get(key);
            }
        }
        return null;
    }
    
    public Person getPersonByIP(InetAddress ip) {
        if (ip == null) {
            return null;
        }
        String key = ip.getHostAddress().toString();
        if (mMapPersonsByIp.containsKey(key)) {
            return (Person)mMapPersonsByIp.get(key);
        }
        return null;
    }

    public int getActivePersonCount() {
        List<Person> listPerson = null;
        synchronized (mMapPersons) {
            listPerson = new ArrayList<Person>(mMapPersons.values());
        }

        int count = 0;
        for (int i = 0; i < listPerson.size(); ++i) {
            Person person = listPerson.get(i);
            if (person.isOnline()) {
                ++count;
            }
        }
        return count;
    }
}
