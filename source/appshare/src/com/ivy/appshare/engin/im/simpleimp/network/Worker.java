package com.ivy.appshare.engin.im.simpleimp.network;

import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.engin.im.simpleimp.protocol.PackageReceive;

public abstract class Worker {
    public interface PackageListener {
        public void receivedPackage(PackageReceive packet);
    }

    public abstract void broadcast(String message, Person from);
    public void send(String message, Person from, Person to) {}
    public void sendRaw(String standardHead, byte []rawdata, Person from, Person to) {}
    public abstract void registrPackageListener(PackageListener listener);
    public abstract void release();



    private static Worker gUdpWorker = null;
    public static Worker getUdpWorker() {
        if (gUdpWorker == null) {
            gUdpWorker = new UdpWorker();
        }
        
        return gUdpWorker;
    }

    public static void releaseUdpWorker() {
        if (gUdpWorker == null) {
            return;
        }

        gUdpWorker.release();
        gUdpWorker = null;
    }

    private static Worker gUdpMultiCastWorker = null;
    public static Worker getMultiCastWorker() {
    	if (gUdpMultiCastWorker == null) {
    	    gUdpMultiCastWorker = new UdpMultiCastWorker();
    	}

    	return gUdpMultiCastWorker;
    }

    public static void releaseUdpMultiCastWorker() {
    	if (gUdpMultiCastWorker == null) {
    		return;
    	}

    	gUdpMultiCastWorker.release();
    	gUdpMultiCastWorker = null;
    }
}
