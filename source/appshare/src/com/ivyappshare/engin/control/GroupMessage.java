package com.ivyappshare.engin.control;

import com.ivyappshare.engin.im.Person;

public class GroupMessage extends ChatMessage {
    public boolean mIsBroadCast;       // 1, is broadcast.  2, is special group.
    public String mGroupName;    // when grouptype is 1, this field is null.
                                  // when grouptype is 2, this field is the groupname of this message.
    public Person mFromPerson;   // userid and mac must matched with Table_Users fields.
}