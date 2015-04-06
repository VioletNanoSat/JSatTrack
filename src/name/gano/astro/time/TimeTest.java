package name.gano.astro.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.GregorianChronology;
import org.junit.Before;
import org.junit.Test;

public class TimeTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testTimeDateTime() {
		DateTime dt = new DateTime();
		Time t = new Time(dt);
		assertEquals(dt.getMillis(), t.getCurrentGregorianCalendar()
				.getTimeInMillis());
	}

	@Test
	public void testConvertJD2Calendar() {
		DateTime joda = new DateTime(GregorianChronology.getInstance())
				.withZoneRetainFields(DateTimeZone.UTC);
		Time gano = new Time(joda.getYear(), joda.getMonthOfYear(), joda
				.getDayOfMonth(), joda.getHourOfDay(), joda.getMinuteOfHour(),
				joda.getSecondOfMinute() + (joda.getMillisOfSecond() / 1000F));
		GregorianCalendar ganoCal = Time.convertJD2Calendar(gano
				.getJulianDate());
		assertTrue(String.format("Joda: %d, Gano: %d, Difference: %d", 
				joda.getMillis(), ganoCal.getTimeInMillis(), new 
				Period(joda.getMillis(), ganoCal.getTimeInMillis()).getDays()), 
				joda.getMillis() - ganoCal.getTimeInMillis() < 2);
		DateTime ganoJoda = gano.toDateTime();
		assertTrue(joda.isEqual(ganoJoda));
	}

	@Test
	public void jd2DateTime() {
		DateTime dt1 = new DateTime(2009, 5, 3, 9, 35, 50, 0,
				GregorianChronology.getInstanceUTC());
		DateTime dt2 = Time.dateTimeOfJd(2454954.89988);
		DateTime dt3 = new DateTime(Time.convertJD2Calendar(2454954.89988),
				GregorianChronology.getInstanceUTC());

		assertClose(dt1, dt2);
		assertClose(dt3, dt1);
		assertClose(dt2, dt3);
	}

	private static void assertClose(DateTime d1, DateTime d2) {
		Period p = (d1.isBefore(d2)) ? new Interval(d1, d2).toPeriod(PeriodType
				.dayTime()) : new Interval(d2, d1).toPeriod(PeriodType
				.dayTime());
		System.out.println(String.format(
				"Days: %d, Hours: %d, Mins: %d, Secs: %d, Millis: %d", p
						.getDays(), p.getHours(), p.getMinutes(), p
						.getSeconds(), p.getMillis()));
		assertTrue(String.format(
				"Days: %d, Hours: %d, Mins: %d, Secs: %d, Millis: %d", p
						.getDays(), p.getHours(), p.getMinutes(), p
						.getSeconds(), p.getMillis()), new Duration(d1, d2)
				.compareTo(Duration.standardSeconds(1L)) < 0);
	}

	@Test
	public void testParse() {
		Time t = new Time();
		t.setDateFormat(new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS z"));
		String s = t.getDateTimeStr();
		Time t2 = t.parse(s);
		assertEquals(String.format("expected: %f, was: %f", t.getMJD(), t
				.getMJD()), t.getMJD(), t2.getMJD(), 0.000001);
	}
}
