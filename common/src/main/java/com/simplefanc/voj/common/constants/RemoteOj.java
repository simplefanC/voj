package com.simplefanc.voj.common.constants;

public enum RemoteOj {
    HDU("HDU"),

    CF("CF"),

    POJ("POJ"),

    JSK("JSK"),

    MXT("MXT"),

    TKOJ("TKOJ"),

    EOJ("EOJ"),

    HDU_REMOTERemoteOj_ACCOUNT("Hdu Remote Judge Account"),

    CF_REMOTERemoteOj_ACCOUNT("Codeforces Remote Judge Account");

    private final String name;

    RemoteOj(String remoteJudgeName) {
        this.name = remoteJudgeName;
    }

    public static RemoteOj getTypeByName(String judgeName) {
        if (judgeName == null) return null;
        for (RemoteOj remoteJudge : RemoteOj.values()) {
            if (remoteJudge.getName().equals(judgeName)) {
                return remoteJudge;
            }
        }
        return null;
    }

    public static Boolean isRemoteOJ(String name) {
        for (RemoteOj remoteOJ : RemoteOj.values()) {
            if (remoteOJ.getName().equals(name)) {
                return true;
            }
        }
        return false;

    }

    public String getName() {
        return name;
    }
}
