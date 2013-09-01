package com.bydavy.easy.network.utils;

import javax.annotation.Nonnull;

public class LogHelper {

    private static String messageFormatter(@Nonnull String tag, @Nonnull String message) {
        return tag + " - " + message;
    }

    public static void e(@Nonnull String tag, @Nonnull String errorId, @Nonnull String msg) {
        System.err.println(messageFormatter(tag, msg));
    }

    public static void e(@Nonnull String tag, @Nonnull String errorId, @Nonnull String msg, @Nonnull Throwable e) {
        System.err.println(messageFormatter(tag, msg) + " " + e.toString());
    }

    public static void d(@Nonnull String tag, @Nonnull String msg) {
        System.out.println(messageFormatter(tag, msg));
    }

    public static void w(@Nonnull String tag, @Nonnull String msg) {
        System.out.println(messageFormatter(tag, msg));
    }

    public static void i(@Nonnull String tag, @Nonnull String msg) {
        System.out.println(messageFormatter(tag, msg));
    }

    public static void v(@Nonnull String tag, @Nonnull String msg) {
        System.out.println(messageFormatter(tag, msg));
    }

    public static void wtf(@Nonnull String tag, @Nonnull String msg) {
        System.out.println(messageFormatter(tag, msg));
    }

    private LogHelper() {
        throw new IllegalAccessError();
    }

}
