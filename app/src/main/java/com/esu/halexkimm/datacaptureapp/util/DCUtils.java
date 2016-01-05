package com.esu.halexkimm.datacaptureapp.util;

import java.sql.Timestamp;

public class DCUtils {
    public static String buildTimeStampString() {
       return new Timestamp(System.currentTimeMillis()).toString();
    }
}
