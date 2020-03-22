package com.looyee.origin;


import java.util.ResourceBundle;

public class NettyProperties {

    public static ResourceBundle BUNDLE;

    static {
        BUNDLE = ResourceBundle.getBundle("netty-server-properties");
    }

}
