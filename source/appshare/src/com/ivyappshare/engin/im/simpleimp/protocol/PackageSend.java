package com.ivyappshare.engin.im.simpleimp.protocol;

import com.ivyappshare.engin.im.Person;

public class PackageSend {
	public Person mToPerson;
	public StringBuffer mSendMessage;
	public byte[] mSendRawData;    // now, this field only used for IPTUX_SENDICON

	public PackageSend () {
		mToPerson = null;
		mSendMessage = null;
		mSendRawData = null;
	}

	//
	private static long mPacketNo = 1;

	public static String getPacketNo(String str) {
		String[] tmp = str.split(":");
		return tmp[1];
	}
	
	public static PackageSend createCommand(Person from, Person to, long command, String additional) {
        PackageSend packageSend = new PackageSend();

        packageSend.mToPerson = to;

        StringBuffer tmpBuffer = new StringBuffer();
        tmpBuffer = PackageSend.packHead(from);
        tmpBuffer.append(String.valueOf(command) + ":");
        if (additional != null) {
            tmpBuffer.append(additional);
        }
        packageSend.mSendMessage = new StringBuffer(tmpBuffer);

        return packageSend;
	}

	public static StringBuffer packHead(Person from) {
	    // Ver(1) : PacketNo : SenderName : SenderHost : CommandNo : AdditionalSection
	    // Now the head not include CommandNo and AdditionalSection.

	    StringBuffer strbuf = new StringBuffer();
	    strbuf.append(from.mProtocolVersion).append(":");
        strbuf.append(mPacketNo++).append(":");
	    strbuf.append(from.mName.replace(':', ';')).append(":");
	    strbuf.append(from.mHost.replace(':', ';')).append(":");

	    return strbuf;
	}
}
