package io.tapdata.entity.schema.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Class DateTime Test")
class DateTimeTest {
	@Test
	void autofillWithZeroTest() {
		DateTime dateTime = new DateTime();
		String str = "24-0-1-0-0-0-0";
		String actual = dateTime.autofillWithZero(str, DateTime.DATETIME_TYPE);
		String except = "0024-00-01 00:00:00";
		assertEquals(except, actual);
		String actualDate = dateTime.autofillWithZero(str, DateTime.DATE_TYPE);
		String exceptDate = "0024-00-01";
		assertEquals(exceptDate, actualDate);
		String time = "0-0-0";
		String actualTime = dateTime.autofillWithZero(time, DateTime.TIME_TYPE);
		String exceptTime = "00:00:00";
		assertEquals(exceptTime, actualTime);
		String year = "1";
		String actualYear = dateTime.autofillWithZero(year, DateTime.YEAR_TYPE);
		String exceptYear = "0001";
		assertEquals(exceptYear, actualYear);
	}

	@Test
	void testContainsIllegal() {
		DateTime illegalDateTime = new DateTime("24-0-1", DateTime.DATE_TYPE);
		Assertions.assertTrue(illegalDateTime.isContainsIllegal());
		DateTime dateTime = new DateTime(new Date());
		Assertions.assertFalse(dateTime.isContainsIllegal());
	}

	@Nested
	@DisplayName("Method toFormatString test")
	class ToFormatStringTest {
		@Test
		@DisplayName("test to format yyyyMMdd")
		void testToFormatyyyyMMdd() {
			Instant instant = Instant.parse("2024-05-08T16:00:00.000Z");
			DateTime dateTime = new DateTime(instant);
			String yyyyMMdd = dateTime.toFormatString("yyyyMMdd");
			assertEquals("20240509", yyyyMMdd);
		}
	}

	@Nested
	@DisplayName("Method zoneOffset test")
	class ZoneOffsetTest {
		@Test
		@DisplayName("test TimeZone=GMT+8")
		void test1() {
			TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
			DateTime dateTime = new DateTime(Instant.parse("2024-05-08T00:00:00.000Z"));
			dateTime.setTimeZone(timeZone);
			ZoneOffset zoneOffset = dateTime.zoneOffset();
			assertEquals(ZoneOffset.ofHours(8), zoneOffset);
		}

		@Test
		@DisplayName("test TimeZone = null, expect GMT")
		void test2() {
			DateTime dateTime = new DateTime(Instant.parse("2024-05-08T16:00:00.000Z"));
			ZoneOffset zoneOffset = dateTime.zoneOffset();
			assertEquals(ZoneOffset.ofHours(0), zoneOffset);
		}
	}

	@Nested
	@DisplayName("Method toLocalDateTime test")
	class ToLocalDateTimeTest {
		@Test
		@DisplayName("test main process")
		void test1() {
			DateTime dateTime = new DateTime(Instant.parse("2024-05-24T01:12:33.123Z"));
			LocalDateTime localDateTime = dateTime.toLocalDateTime();
			assertEquals(LocalDateTime.of(2024, 5, 24, 1, 12, 33, 123000000), localDateTime);
		}

		@Test
		@DisplayName("test TimeZone=GMT-1")
		void test2() {
			DateTime dateTime = new DateTime(Instant.parse("2024-05-24T01:12:33.123Z"));
			dateTime.setTimeZone(TimeZone.getTimeZone("GMT-1"));
			LocalDateTime localDateTime = dateTime.toLocalDateTime();
			assertEquals(LocalDateTime.of(2024, 5, 24, 0, 12, 33, 123000000), localDateTime);
		}
	}

	@Nested
	class toFormatStringV2Test {
		@Test
		@DisplayName("test format=yyyy-MM-dd HH:mm:ss.SSS")
		void test1() {
			DateTime dateTime = new DateTime(Instant.parse("2024-05-24T01:12:33.123Z"));
			String format = dateTime.toFormatStringV2("yyyy-MM-dd HH:mm:ss.SSS");
			assertEquals("2024-05-24 01:12:33.123", format);
		}

		@Test
		@DisplayName("test format=yyyy-MM-dd HH:mm:ss.SSS, TimeZone=GMT-1")
		void test2() {
			DateTime dateTime = new DateTime(Instant.parse("2024-05-24T01:12:33.123Z"));
			dateTime.setTimeZone(TimeZone.getTimeZone("GMT-1"));
			String format = dateTime.toFormatStringV2("yyyy-MM-dd HH:mm:ss.SSS");
			assertEquals("2024-05-24 00:12:33.123", format);
		}

		@Test
		void test3() {
			DateTime dateTime = new DateTime(Instant.parse("2024-05-24T16:00:00.000Z"));
			String format = dateTime.toFormatStringV2("yyyyMMdd");
			assertEquals("20240524", format);
		}
	}
}
