package com.ivyappshare.engin.im.simpleimp.protocol;



public class VersionControl {
    public enum VersionType {
        IPMSG_STANDARD,
        IPMSG_IPTUX,
        IPMSG_FEIQ,
        IPMSG_OTHER_EXTENDS,
        IVY_CURRENT_VERSION,
        IVY_SMALLER_THAN_CURVERSION,
        IVY_BIGGER_THAN_CURVERSION,
        UNKNOWN,
    }

    public static VersionType getVersionType(String versionString) {
        if (versionString.equalsIgnoreCase("1")) {
            return VersionType.IPMSG_STANDARD;
        } else if (versionString.equalsIgnoreCase(ImMessages.IPMSG_VERSION)) {
            return VersionType.IVY_CURRENT_VERSION;
        } else if (versionString.startsWith("1_ivy_")) {
            if (getIvyVersion(versionString) > getIvyCurrentVersion()) {
                return VersionType.IVY_SMALLER_THAN_CURVERSION;
            } else {
                return VersionType.IVY_BIGGER_THAN_CURVERSION;
            }
        } else if (versionString.length() >= 9 && versionString.substring(0, 9).equals("1_iptux_0")) {
            return VersionType.IPMSG_IPTUX;
        } else  if (versionString.length() >= 6 && versionString.substring(0, 6).equals("1_lbt6")) {
            return VersionType.IPMSG_FEIQ;
        } else if (versionString.startsWith("1")) {
            return VersionType.IPMSG_OTHER_EXTENDS;
        } else {
            return VersionType.UNKNOWN;
        }
    }

    public static int getIvyVersion(String versionString) {
        if (!versionString.startsWith(ImMessages.IPMSG_VERSION)) {
            return 0;
        } else {
            String str = versionString.substring("1_ivy_".length());
            String versionAndMacString[] = str.split("_");
            return Integer.valueOf(versionAndMacString[0]);
        }
    }

    public static String getMacAddressFromVersion(String versionString) {
        if (versionString == null) {
            return null;
        }

        if (versionString.startsWith(ImMessages.IPMSG_VERSION)) {
            String str = versionString.substring("1_ivy_".length());
            String versionAndMacString[] = str.split("_", 2);
            if (versionAndMacString.length < 2) {
                return null;
            }
            return versionAndMacString[1];
        }

        // TODO: we can extract Mac address from feiQ.

        return null;
    }

    public static int getIvyCurrentVersion() {
        String str = ImMessages.IPMSG_VERSION.substring("1_ivy_".length());
        return Integer.valueOf(str);
    }

    public static boolean isIvyVersion(String versionString) {
        return (getIvyVersion(versionString) > 0);
    }

    public static String getDefaultVersionIcon(VersionType type) {
        switch (type) {
            case IPMSG_STANDARD:
            case IPMSG_OTHER_EXTENDS:
                return "head_version_icon_ipmsg.png";
            case IPMSG_IPTUX:
                return "head_version_icon_iptux.png";
            case IPMSG_FEIQ:
                return "head_version_icon_feiq.png";
        }

        return null;
    }
}
