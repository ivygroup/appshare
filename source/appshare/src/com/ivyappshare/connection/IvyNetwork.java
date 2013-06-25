package com.ivyappshare.connection;

public class IvyNetwork {
    private static IvyNetwork gInstance = new IvyNetwork();

    public static IvyNetwork getInstance() {
        return gInstance;
    }


    private IvyNetService mIvyNetService;

    private IvyNetwork() {
        mIvyNetService = null;
    }

    public void init(IvyNetService service) {
        if (mIvyNetService == null) {
            mIvyNetService = service;
        }
    }

    public void uninit() {
        mIvyNetService = null;
    }

    public IvyNetService getIvyNetService() {
        return mIvyNetService;
    }
}
