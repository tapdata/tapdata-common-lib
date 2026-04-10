package io.tapdata.entity.schema.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
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
	@Nested
	class DateTimeTransformation{
        @Test
		void test_Time_before_1970(){
			DateTime dateTime1 = new DateTime(new Date(-662673535171L));
			DateTime dateTime2 = new DateTime(new java.sql.Date(-662673535171L));
			DateTime dateTime3 = new DateTime(new java.sql.Time(-662673535171L));
			DateTime dateTime4 = new DateTime(new Timestamp(-662673535171L));
			DateTime dateTime5 = new DateTime(-662673535171L);
			DateTime dateTime6 = new DateTime(Instant.ofEpochMilli(-662673535171L));
			Assertions.assertEquals(-662673536, dateTime1.getSeconds());
			Assertions.assertEquals(-662673536, dateTime2.getSeconds());
			Assertions.assertEquals(-662673536, dateTime3.getSeconds());
			Assertions.assertEquals(-662673536, dateTime4.getSeconds());
			Assertions.assertEquals(-662673536, dateTime5.getSeconds());
			Assertions.assertEquals(-662673536, dateTime6.getSeconds());

			Assertions.assertEquals(829000000, dateTime1.getNano());
			Assertions.assertEquals(829000000, dateTime2.getNano());
			Assertions.assertEquals(829000000, dateTime3.getNano());
			Assertions.assertEquals(829000000, dateTime4.getNano());
			Assertions.assertEquals(829000000, dateTime5.getNano());
			Assertions.assertEquals(829000000, dateTime6.getNano());

			Assertions.assertTrue(dateTime1.toTimestamp().equals(new Timestamp(-662673535171L)));
			Assertions.assertTrue(dateTime2.toTimestamp().equals(new Timestamp(-662673535171L)));
			Assertions.assertTrue(dateTime3.toTimestamp().equals(new Timestamp(-662673535171L)));
			Assertions.assertTrue(dateTime4.toTimestamp().equals(new Timestamp(-662673535171L)));
			Assertions.assertTrue(dateTime5.toTimestamp().equals(new Timestamp(-662673535171L)));
			Assertions.assertTrue(dateTime6.toTimestamp().equals(new Timestamp(-662673535171L)));


			Assertions.assertEquals(dateTime1.toLocalDateTime(), LocalDateTime.of(1949, 1, 1, 4, 1, 4, 829000000));
			Assertions.assertEquals(dateTime2.toLocalDateTime(), LocalDateTime.of(1949, 1, 1, 4, 1, 4, 829000000));
			Assertions.assertEquals(dateTime3.toLocalDateTime(), LocalDateTime.of(1949, 1, 1, 4, 1, 4, 829000000));
			Assertions.assertEquals(dateTime4.toLocalDateTime(), LocalDateTime.of(1949, 1, 1, 4, 1, 4, 829000000));
			Assertions.assertEquals(dateTime5.toLocalDateTime(), LocalDateTime.of(1949, 1, 1, 4, 1, 4, 829000000));
			Assertions.assertEquals(dateTime6.toLocalDateTime(), LocalDateTime.of(1949, 1, 1, 4, 1, 4, 829000000));
		}

		@Test
		void test_now(){
			DateTime dateTime1 = new DateTime(new Date(1723086539829L));
			DateTime dateTime2 = new DateTime(new java.sql.Date(1723086539829L));
			DateTime dateTime3 = new DateTime(new java.sql.Time(1723086539829L));
			DateTime dateTime4 = new DateTime(new Timestamp(1723086539829L));
			DateTime dateTime5 = new DateTime(1723086539829L);
			DateTime dateTime6 = new DateTime(Instant.ofEpochMilli(1723086539829L));
			Assertions.assertEquals(1723086539, dateTime1.getSeconds());
			Assertions.assertEquals(1723086539, dateTime2.getSeconds());
			Assertions.assertEquals(1723086539, dateTime3.getSeconds());
			Assertions.assertEquals(1723086539, dateTime4.getSeconds());
			Assertions.assertEquals(1723086539, dateTime5.getSeconds());
			Assertions.assertEquals(1723086539, dateTime6.getSeconds());

			Assertions.assertEquals(829000000, dateTime1.getNano());
			Assertions.assertEquals(829000000, dateTime2.getNano());
			Assertions.assertEquals(829000000, dateTime3.getNano());
			Assertions.assertEquals(829000000, dateTime4.getNano());
			Assertions.assertEquals(829000000, dateTime5.getNano());
			Assertions.assertEquals(829000000, dateTime6.getNano());

			Assertions.assertTrue(dateTime1.toTimestamp().equals(new Timestamp(1723086539829L)));
			Assertions.assertTrue(dateTime2.toTimestamp().equals(new Timestamp(1723086539829L)));
			Assertions.assertTrue(dateTime3.toTimestamp().equals(new Timestamp(1723086539829L)));
			Assertions.assertTrue(dateTime4.toTimestamp().equals(new Timestamp(1723086539829L)));
			Assertions.assertTrue(dateTime5.toTimestamp().equals(new Timestamp(1723086539829L)));
			Assertions.assertTrue(dateTime6.toTimestamp().equals(new Timestamp(1723086539829L)));

			Assertions.assertEquals(dateTime1.toLocalDateTime(), LocalDateTime.of(2024, 8, 8, 3, 8, 59, 829000000));
			Assertions.assertEquals(dateTime2.toLocalDateTime(), LocalDateTime.of(2024, 8, 8, 3, 8, 59, 829000000));
			Assertions.assertEquals(dateTime3.toLocalDateTime(), LocalDateTime.of(2024, 8, 8, 3, 8, 59, 829000000));
			Assertions.assertEquals(dateTime4.toLocalDateTime(), LocalDateTime.of(2024, 8, 8, 3, 8, 59, 829000000));
			Assertions.assertEquals(dateTime5.toLocalDateTime(), LocalDateTime.of(2024, 8, 8, 3, 8, 59, 829000000));
			Assertions.assertEquals(dateTime6.toLocalDateTime(), LocalDateTime.of(2024, 8, 8, 3, 8, 59, 829000000));
		}

		@Test
		void test_0001_01_01() {
			LocalDateTime localDateTime = LocalDateTime.of(1, 1, 1, 0, 0, 0);
			DateTime dateTime = new DateTime(localDateTime);
			Assertions.assertEquals(java.sql.Date.valueOf("0001-01-01"), dateTime.toSqlDate());
			Assertions.assertEquals(Date.from(Instant.parse("0001-01-01T00:00:00Z")), dateTime.toDate());
		}

		@Test
		void test_9999_12_31() {
			LocalDateTime localDateTime = LocalDateTime.of(9999, 12, 31, 0, 0, 0);
			DateTime dateTime = new DateTime(localDateTime);
			Assertions.assertEquals(java.sql.Date.valueOf("9999-12-31"), dateTime.toSqlDate());
			Assertions.assertEquals(Date.from(Instant.parse("9999-12-31T00:00:00Z")), dateTime.toDate());
		}

	}

	@Nested
	@DisplayName("JS/Java Date delegate methods test")
	class DateDelegateMethodsTest {

		// 2024-05-08T16:30:45.123Z => UTC: year=2024 month=4(0-indexed) date=8 day=3(Wed) hours=16 minutes=30 seconds=45 millis=123
		private final Instant testInstant = Instant.parse("2024-05-08T16:30:45.123Z");

		@Test
		@DisplayName("getMonth should return correct month with UTC timezone")
		void testGetMonthUTC() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			assertEquals(4, dt.getMonth()); // May = 4 (0-indexed)
		}

		@Test
		@DisplayName("getMonth should respect DateTime's own timezone")
		void testGetMonthWithTimezone() {
			// 2024-12-31T23:30:00Z => in UTC+8 it's 2025-01-01 07:30:00 => month should be 0 (January)
			DateTime dt = new DateTime(Instant.parse("2024-12-31T23:30:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals(0, dt.getMonth()); // January in GMT+8
		}

		@Test
		@DisplayName("getFullYear should respect DateTime's own timezone")
		void testGetFullYearWithTimezone() {
			DateTime dt = new DateTime(Instant.parse("2024-12-31T23:30:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals(2025, dt.getFullYear()); // 2025 in GMT+8
		}

		@Test
		@DisplayName("getDate should respect DateTime's own timezone")
		void testGetDateWithTimezone() {
			DateTime dt = new DateTime(Instant.parse("2024-12-31T23:30:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals(1, dt.getDate()); // 1st in GMT+8
		}

		@Test
		@DisplayName("getHours should respect DateTime's own timezone")
		void testGetHoursWithTimezone() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			// 16:30 UTC => 00:30 next day in GMT+8
			assertEquals(0, dt.getHours());
		}

		@Test
		@DisplayName("getMinutes returns correct minutes")
		void testGetMinutes() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			assertEquals(30, dt.getMinutes());
		}

		@Test
		@DisplayName("getDateSeconds returns correct seconds (0-59)")
		void testGetDateSeconds() {
			DateTime dt = new DateTime(testInstant);
			assertEquals(45, dt.getDateSeconds());
		}

		@Test
		@DisplayName("getMilliseconds returns correct milliseconds")
		void testGetMilliseconds() {
			DateTime dt = new DateTime(testInstant);
			assertEquals(123, dt.getMilliseconds());
		}

		@Test
		@DisplayName("getTime returns epoch milliseconds")
		void testGetTime() {
			DateTime dt = new DateTime(testInstant);
			assertEquals(testInstant.toEpochMilli(), dt.getTime());
		}

		@Test
		@DisplayName("getYear returns year minus 1900")
		void testGetYear() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			assertEquals(124, dt.getYear()); // 2024 - 1900
		}

		@Test
		@DisplayName("getDay returns correct day of week")
		void testGetDay() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			assertEquals(3, dt.getDay()); // Wednesday = 3
		}

		// UTC methods
		@Test
		@DisplayName("getUTCMonth returns month in UTC regardless of timezone")
		void testGetUTCMonth() {
			DateTime dt = new DateTime(Instant.parse("2024-12-31T23:30:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals(11, dt.getUTCMonth()); // December in UTC = 11
		}

		@Test
		@DisplayName("getUTCFullYear returns year in UTC")
		void testGetUTCFullYear() {
			DateTime dt = new DateTime(Instant.parse("2024-12-31T23:30:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals(2024, dt.getUTCFullYear()); // still 2024 in UTC
		}

		@Test
		@DisplayName("getUTCHours returns hours in UTC")
		void testGetUTCHours() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals(16, dt.getUTCHours()); // 16 in UTC regardless of timezone
		}

		// To methods
		@Test
		@DisplayName("toDateString returns JS-style date string in UTC")
		void testToDateStringUTC() {
			// 2024-05-08 is a Wednesday
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			assertEquals("Wed May 08 2024", dt.toDateString());
		}

		@Test
		@DisplayName("toDateString respects DateTime's own timezone")
		void testToDateStringWithTimezone() {
			// 2024-12-31T23:30:00Z => in GMT+8 it's 2025-01-01
			DateTime dt = new DateTime(Instant.parse("2024-12-31T23:30:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			assertEquals("Wed Jan 01 2025", dt.toDateString());
		}

		@Test
		@DisplayName("toDateString for single-digit day pads with zero")
		void testToDateStringSingleDigitDay() {
			// 2024-01-01 is a Monday
			DateTime dt = new DateTime(Instant.parse("2024-01-01T12:00:00.000Z"));
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			assertEquals("Mon Jan 01 2024", dt.toDateString());
		}

		@Test
		@DisplayName("toISOString returns ISO 8601 UTC format")
		void testToISOString() {
			DateTime dt = new DateTime(testInstant);
			assertEquals("2024-05-08T16:30:45.123Z", dt.toISOString());
		}

		@Test
		@DisplayName("toJSON returns same as toISOString")
		void testToJSON() {
			DateTime dt = new DateTime(testInstant);
			assertEquals(dt.toISOString(), dt.toJSON());
		}

		@Test
		@DisplayName("valueOf returns epoch milliseconds")
		void testValueOf() {
			DateTime dt = new DateTime(testInstant);
			assertEquals(dt.getTime(), dt.valueOf());
		}

		// setFullYear
		@Test
		@DisplayName("setFullYear modifies year correctly")
		void testSetFullYear() {
			DateTime dt = new DateTime(testInstant);
			dt.setTimeZone(TimeZone.getTimeZone("UTC"));
			dt.setFullYear(2000);
			assertEquals(2000, dt.getFullYear());
			assertEquals(4, dt.getMonth()); // month unchanged
		}

		// setMilliseconds
		@Test
		@DisplayName("setMilliseconds modifies milliseconds correctly")
		void testSetMilliseconds() {
			DateTime dt = new DateTime(testInstant);
			dt.setMilliseconds(456);
			assertEquals(456, dt.getMilliseconds());
		}
	}

	@Nested
	@DisplayName("Object-aware date operations")
	class ObjectAwareDateOperationsTest {

		@Test
		@DisplayName("compare helpers should support DateTime, Date and Instant")
		void testCompareHelpersSupportMixedTypes() {
			Instant instant = Instant.parse("2024-03-01T10:15:30Z");
			DateTime dateTime = new DateTime(instant);
			Date date = Date.from(instant);
			Instant later = instant.plusSeconds(1);

			assertEquals(0, DateTime.compare(dateTime, date));
			Assertions.assertTrue(DateTime.isBefore(date, later));
			Assertions.assertTrue(DateTime.isAfter(later, dateTime));
			Assertions.assertTrue(DateTime.isEqual(dateTime, instant));
		}

		@Test
		@DisplayName("same day and month should use the provided zone")
		void testSameDayAndMonthUseProvidedZone() {
			Instant sameDayLeft = Instant.parse("2024-03-31T23:30:00Z");
			Instant sameDayRight = Instant.parse("2024-04-01T00:15:00Z");
			ZoneId zoneId = ZoneId.of("Asia/Shanghai");

			Assertions.assertTrue(DateTime.isSameDay(sameDayLeft, sameDayRight, zoneId));
			Assertions.assertFalse(DateTime.isSameDay(sameDayLeft, sameDayRight, ZoneOffset.UTC));
			Assertions.assertTrue(DateTime.isSameMonth(sameDayLeft, sameDayRight, zoneId));
			Assertions.assertFalse(DateTime.isSameMonth(sameDayLeft, sameDayRight, ZoneOffset.UTC));
		}

		@Test
		@DisplayName("add helpers should return new DateTime instances and keep source timezone")
		void testAddHelpersReturnNewDateTime() {
			DateTime source = new DateTime(Instant.parse("2024-01-31T23:30:40Z"));
			source.setTimeZone(TimeZone.getTimeZone("GMT+8"));

			DateTime plusYears = DateTime.addYears(source, 1);
			DateTime plusMonths = DateTime.addMonths(source, 1);
			DateTime plusDays = DateTime.addDays(source, 2);
			DateTime plusHours = DateTime.addHours(source, 3);
			DateTime plusMinutes = DateTime.addMinutes(source, 15);
			DateTime plusSeconds = DateTime.addSeconds(source, 20);

			assertEquals(Instant.parse("2025-01-31T23:30:40Z"), plusYears.toInstant());
			assertEquals(Instant.parse("2024-02-29T23:30:40Z"), plusMonths.toInstant());
			assertEquals(Instant.parse("2024-02-02T23:30:40Z"), plusDays.toInstant());
			assertEquals(Instant.parse("2024-02-01T02:30:40Z"), plusHours.toInstant());
			assertEquals(Instant.parse("2024-01-31T23:45:40Z"), plusMinutes.toInstant());
			assertEquals(Instant.parse("2024-01-31T23:31:00Z"), plusSeconds.toInstant());
			assertEquals("GMT+08:00", plusMonths.getTimeZone().getID());
			assertEquals(Instant.parse("2024-01-31T23:30:40Z"), source.toInstant());
		}

		@Test
		@DisplayName("diff helpers should return signed duration values")
		void testDiffHelpersReturnSignedDuration() {
			Instant left = Instant.parse("2024-03-02T01:02:03Z");
			Date right = Date.from(Instant.parse("2024-03-01T00:00:00Z"));

			assertEquals(90123000L, DateTime.diffMillis(left, right));
			assertEquals(90123L, DateTime.diffSeconds(left, right));
			assertEquals(1502L, DateTime.diffMinutes(left, right));
			assertEquals(25L, DateTime.diffHours(left, right));
			assertEquals(1L, DateTime.diffDays(left, right));
			assertEquals(-90123000L, DateTime.diffMillis(right, left));
		}
	}
}
