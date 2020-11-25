package de.mbrauner.unifibackup;

import java.io.File;

public class UnifiProperties extends java.util.Properties {

    private static final long serialVersionUID = -6861275733829677276L;

    public static final String USER = "user";
    public static final String PW = "pw";
    public static final String HOST = "host";
    public static final String REMOTE = "remotepath";
    public static final String LOCALPATH = "localpath";

    public static final String REMOTE_DEFAULT = File.separatorChar + "data" + File.separatorChar + "unifi" + File.separatorChar + "data"
        + File.separatorChar + "backup" + File.separatorChar + "autobackup" + File.separatorChar + "autobackup*.unf";
    public static final String LOCALPATH_DEFAULT = "." + File.separatorChar;

}
