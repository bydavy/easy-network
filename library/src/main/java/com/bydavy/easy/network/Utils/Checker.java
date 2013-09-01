package com.bydavy.easy.network.utils;

import javax.annotation.Nullable;

public class Checker {

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
            value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
            justification = "Throw a NPE on purpose")
    public static void nonNull(@Nullable Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
    }

    private Checker() {
        throw new IllegalAccessError();
    }
}
