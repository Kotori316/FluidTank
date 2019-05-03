package com.kotori316.fluidtank;

public class Utils {
    public static int toInt(long l) {
        int i = (int) l;
        if (i == l) {
            return i;
        } else {
            return l > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

}
