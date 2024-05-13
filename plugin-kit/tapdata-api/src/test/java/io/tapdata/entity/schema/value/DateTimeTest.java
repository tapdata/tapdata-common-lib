package io.tapdata.entity.schema.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateTimeTest {
    @Test
    void autofillWithZeroTest(){
        DateTime dateTime = new DateTime();
        String str = "24-0-1-0-0-0-0";
        String actual = dateTime.autofillWithZero(str, 6);
        String except = "0024-00-01 00:00:00";
        Assertions.assertEquals(except,actual);
        String actual1 = dateTime.autofillWithZero(str, 2);
        String except1 = "0024-00-01";
        Assertions.assertEquals(except1,actual1);
    }
}
