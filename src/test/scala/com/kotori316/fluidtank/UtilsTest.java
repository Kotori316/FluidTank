package com.kotori316.fluidtank;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import scala.Option;
import scala.Some;
import scala.jdk.javaapi.OptionConverters;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest extends BeforeAllTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, -1, Integer.MIN_VALUE + 1, Integer.MIN_VALUE,})
    void inInt(int i) {
        assertEquals(i, Utils.toInt(i));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L + Integer.MAX_VALUE, 100L + Integer.MAX_VALUE, Long.MAX_VALUE - 1L, Long.MAX_VALUE})
    void overMax(long l) {
        assertEquals(Integer.MAX_VALUE, Utils.toInt(l));
    }

    @Test
    void toJava1() {
        var opt = Optional.of("a");
        var scalaOpt = Option.apply("a");
        assertEquals(opt, OptionConverters.toJava(scalaOpt));
    }

    @Test
    void toJava2() {
        Optional<String> opt = Optional.empty();
        Option<String> scalaOpt = Option.empty();
        assertEquals(opt, OptionConverters.toJava(scalaOpt));
    }
    @Test
    void toJava3(){
        Optional<String> opt = Optional.empty();
        Option<String> scalaOpt = Some.apply(null);
        assertEquals(opt, OptionConverters.toJava(scalaOpt));
    }
}
