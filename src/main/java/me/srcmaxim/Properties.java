package me.srcmaxim;

import java.nio.charset.Charset;

public final class Properties {

    public static final String host = "localhost";
    public static final int port = 6001;

    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int BSIZE = 1024;

    private Properties() {
    }
}
