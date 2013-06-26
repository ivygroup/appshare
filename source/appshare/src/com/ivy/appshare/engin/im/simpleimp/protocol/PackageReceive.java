package com.ivy.appshare.engin.im.simpleimp.protocol;

import com.ivy.appshare.engin.im.Person;

// Ver(1) : PacketNo : SenderName : SenderHost : CommandNo : AdditionalSection
public class PackageReceive {
	public boolean mIsValied;
	public String mPacketNo;
	public Person mPersonOther;
	public long mCommand;
	public String mAdditionalSection;
	public byte[] mAdditionalRawData;  // Now, thie field only used for IPTUX_SENDICON command.
	public boolean mIsUdp;	// true is udp, false is tcp.

	public PackageReceive() {
		mPersonOther = new Person();
		mIsValied = false;
		mIsUdp = true;
	}
}
