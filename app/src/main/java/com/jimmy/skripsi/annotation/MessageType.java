package com.jimmy.skripsi.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({MessageType.SENT, MessageType.RECEIVE})
public @interface MessageType {
    int SENT = 1;
    int RECEIVE = 2;
}
