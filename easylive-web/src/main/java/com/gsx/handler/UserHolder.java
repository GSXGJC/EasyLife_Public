package com.gsx.handler;

public class UserHolder {
    public static ThreadLocal<String> userIdThreadLocal = new ThreadLocal<>();



    public static void setUserId(String userId) {
        UserHolder.userIdThreadLocal.set(userId);
    }

    public static String getUserId() {
        return UserHolder.userIdThreadLocal.get();
    }

    public static void remove() {
        UserHolder.userIdThreadLocal.remove();
    }
}
