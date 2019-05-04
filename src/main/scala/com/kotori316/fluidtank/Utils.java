package com.kotori316.fluidtank;

import java.util.Optional;

import scala.Option;

public class Utils {
    public static int toInt(long l) {
        int i = (int) l;
        if (i == l) {
            return i;
        } else {
            return l > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    public static <T> Optional<T> toJava(Option<T> option) {
        if (option != null && option.isDefined()) {
            return Optional.ofNullable(option.get());
        } else {
            return Optional.empty();
        }
    }

}
