package com.looyee.local;


import java.util.ResourceBundle;

public class NettyProperties {

    public static ResourceBundle BUNDLE;

    static {
        BUNDLE = ResourceBundle.getBundle("netty-client-properties");
    }

}
