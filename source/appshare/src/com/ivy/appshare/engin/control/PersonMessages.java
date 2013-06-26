package com.ivy.appshare.engin.control;

import java.util.HashMap;
import java.util.List;

import com.ivy.appshare.engin.data.ImData;
import com.ivy.appshare.engin.im.Person;

public class PersonMessages{
    private ImData mImData;

    private HashMap<String, Integer> mMapUnReadMessage;
    private PersonManager mPersonManager;

    PersonMessages(ImData imData) {
        mMapUnReadMessage = new HashMap<String, Integer>();
        init(imData);
    }

    private void init(ImData imData) {
        mImData = imData;
        mPersonManager = PersonManager.getInstance();

        // Add message read state
        mMapUnReadMessage = mImData.getUnReadMessage();

        HashMap<String, Person> mapPersons = new HashMap<String, Person>();

        List<Person> listPersonConversation = mImData.getHistoryPersons();
        int nConversationSize = listPersonConversation.size();
        for (int i=0; i<nConversationSize; i++) {
            Person person = listPersonConversation.get(i);
            String key = PersonManager.getPersonKey(person);
            if (mMapUnReadMessage.containsKey(key)) {
                person.mDynamicStatus.unReadMsgCount = mMapUnReadMessage.get(key);
            }
            mapPersons.put(key, person);
        }

        // init history person here
        mPersonManager.setHistoryPerson(mapPersons);
    }

    public void addMessage(Person person) {
        if (person == null) {
            return;
        }

        Person tmp = mPersonManager.getPerson(person);
        if (tmp != null) {
            tmp.mDynamicStatus.MessageCount++;
        }
    }

    public void deleteOneMessage(Person person) {
        if (person == null) {
            return;
        }

        Person tmp = mPersonManager.getPerson(person);
        if (tmp != null) {
            tmp.mDynamicStatus.MessageCount--;
        }
    }

    public void deleteMessage(Person person) {
        if (person == null) {
            return;
        }

        String key = PersonManager.getPersonKey(person);
        if (mMapUnReadMessage.containsKey(key)) {
            mMapUnReadMessage.remove(key);
        }

        Person tmp = mPersonManager.getPerson(person);
        if (tmp != null) {
            tmp.mDynamicStatus.MessageCount = 0;
            tmp.mDynamicStatus.unReadMsgCount = 0;
        }
    }

    public void addUnReadMessage(Person person) {
        if (person == null) {
            return;
        }

        int value = 0;
        String key = PersonManager.getPersonKey(person);
        if (mMapUnReadMessage.containsKey(key)) {
            value = mMapUnReadMessage.get(key) + 1;
        } else {
            value = 1;
        }
        mMapUnReadMessage.put(key, value);

        Person tmp = mPersonManager.getPerson(person);
        if (tmp != null) {
            tmp.mDynamicStatus.unReadMsgCount = value;
        }
    }
    
    public void clearUnReadMessage(Person person) {
        if (person == null) {
            return;
        }
        String key = PersonManager.getPersonKey(person);
        mMapUnReadMessage.remove(key);

        Person tmp = mPersonManager.getPerson(person);
        if (tmp != null) {
            tmp.mDynamicStatus.unReadMsgCount = 0;
        }
    }

    public void clearAllMessages() {
        mMapUnReadMessage.clear();

        List<Person> listPerson = mPersonManager.getPersonList();
        int nSize = listPerson.size();
        for (int i=0; i<nSize; i++)
        {
            Person person = listPerson.get(i);
            person.mDynamicStatus.unReadMsgCount = 0;
            person.mDynamicStatus.MessageCount = 0;
        }
    }

    public HashMap<String, Integer> getUnReadMessage() {
        return mMapUnReadMessage;
    }
}
