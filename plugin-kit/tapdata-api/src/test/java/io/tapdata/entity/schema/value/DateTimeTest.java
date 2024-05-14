package io.tapdata.entity.schema.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateTimeTest {
    @Test
    void autofillWithZeroTest(){
        DateTime dateTime = new DateTime();
        String str = "24-0-1-0-0-0-0";
        String actual = dateTime.autofillWithZero(str, DateTime.DATETIME_TYPE);
        String except = "0024-00-01 00:00:00";
        Assertions.assertEquals(except,actual);
        String actualDate = dateTime.autofillWithZero(str, DateTime.DATE_TYPE);
        String exceptDate = "0024-00-01";
        Assertions.assertEquals(exceptDate,actualDate);
        String time = "0-0-0";
        String actualTime = dateTime.autofillWithZero(time, DateTime.TIME_TYPE);
        String exceptTime = "00:00:00";
        Assertions.assertEquals(exceptTime,actualTime);
        String year = "1";
        String actualYear = dateTime.autofillWithZero(year, DateTime.YEAR_TYPE);
        String exceptYear = "0001";
        Assertions.assertEquals(exceptYear,actualYear);
    }
}
