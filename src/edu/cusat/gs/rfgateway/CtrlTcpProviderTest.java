package edu.cusat.gs.rfgateway;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import edu.cusat.common.GTG;
import edu.cusat.common.GroundPassRequest;
import edu.cusat.common.GroundPassResponse;

public class CtrlTcpProviderTest {

	private static final String CUSAT_TLE = "1 99999U          06001.79166667  .00000672  00000-0  82060-4 0 00008\n2 99999 028.4862 000.0150 0012465 203.9215 156.1523 14.82201410000010";

	private static final int TEST_PORT = 5555;

	/**
	 * Running ground pass prediction manually with CUSAT_TLE and HB Observatory
	 * for 10 days starting with Jan 1, 2006 @ midnight
	 **/
	private static final String[] TEST_RESULT = "#,Rise Time,Set Time,Duration [Min]\n1,01 Jan 2006 01:24:18.648 UTC,01 Jan 2006 01:31:36.225 UTC,7.29\n2,01 Jan 2006 03:04:35.910 UTC,01 Jan 2006 03:15:29.936 UTC,10.9\n3,01 Jan 2006 04:46:32.232 UTC,01 Jan 2006 04:58:02.666 UTC,11.51\n4,01 Jan 2006 06:29:23.128 UTC,01 Jan 2006 06:39:37.333 UTC,10.24\n5,01 Jan 2006 08:14:26.104 UTC,01 Jan 2006 08:18:37.819 UTC,4.2\n6,02 Jan 2006 01:39:55.279 UTC,02 Jan 2006 01:49:27.743 UTC,9.54\n7,02 Jan 2006 03:21:09.427 UTC,02 Jan 2006 03:32:34.688 UTC,11.42\n8,02 Jan 2006 05:03:30.391 UTC,02 Jan 2006 05:14:44.310 UTC,11.23\n9,02 Jan 2006 06:46:54.568 UTC,02 Jan 2006 06:55:37.347 UTC,8.71\n10,03 Jan 2006 00:16:09.750 UTC,03 Jan 2006 00:22:37.974 UTC,6.47\n11,03 Jan 2006 01:56:05.828 UTC,03 Jan 2006 02:06:50.283 UTC,10.74\n12,03 Jan 2006 03:37:55.079 UTC,03 Jan 2006 03:49:29.182 UTC,11.57\n13,03 Jan 2006 05:20:39.463 UTC,03 Jan 2006 05:31:12.086 UTC,10.54\n14,03 Jan 2006 07:05:08.932 UTC,03 Jan 2006 07:10:49.096 UTC,5.67\n15,04 Jan 2006 00:31:32.844 UTC,04 Jan 2006 00:40:41.422 UTC,9.14\n16,04 Jan 2006 02:12:35.611 UTC,04 Jan 2006 02:23:57.629 UTC,11.37\n17,04 Jan 2006 03:54:50.652 UTC,04 Jan 2006 04:06:13.439 UTC,11.38\n18,04 Jan 2006 05:38:04.766 UTC,04 Jan 2006 05:47:19.103 UTC,9.24\n19,04 Jan 2006 23:08:08.491 UTC,04 Jan 2006 23:13:31.655 UTC,5.39\n20,05 Jan 2006 00:47:36.976 UTC,05 Jan 2006 00:58:08.850 UTC,10.53\n21,05 Jan 2006 02:29:18.441 UTC,05 Jan 2006 02:40:54.297 UTC,11.6\n22,05 Jan 2006 04:11:56.529 UTC,05 Jan 2006 04:22:44.914 UTC,10.81\n23,05 Jan 2006 05:56:02.940 UTC,05 Jan 2006 06:02:47.948 UTC,6.75\n24,05 Jan 2006 23:23:12.988 UTC,05 Jan 2006 23:31:52.129 UTC,8.65\n25,06 Jan 2006 01:04:02.675 UTC,06 Jan 2006 01:15:19.048 UTC,11.27\n26,06 Jan 2006 02:46:11.481 UTC,06 Jan 2006 02:57:40.989 UTC,11.49\n27,06 Jan 2006 04:29:16.682 UTC,06 Jan 2006 04:38:57.902 UTC,9.69\n28,06 Jan 2006 22:00:21.695 UTC,06 Jan 2006 22:04:10.553 UTC,3.81\n29,06 Jan 2006 23:39:09.656 UTC,06 Jan 2006 23:49:25.464 UTC,10.26\n30,07 Jan 2006 01:20:42.531 UTC,07 Jan 2006 01:32:17.923 UTC,11.59\n31,07 Jan 2006 03:03:14.439 UTC,07 Jan 2006 03:14:15.788 UTC,11.02\n32,07 Jan 2006 04:47:03.232 UTC,07 Jan 2006 04:54:39.291 UTC,7.6\n33,07 Jan 2006 22:14:56.378 UTC,07 Jan 2006 22:22:59.322 UTC,8.05\n34,07 Jan 2006 23:55:30.835 UTC,08 Jan 2006 00:06:38.872 UTC,11.13\n35,08 Jan 2006 01:37:33.040 UTC,08 Jan 2006 01:49:06.922 UTC,11.56\n36,08 Jan 2006 03:20:30.193 UTC,08 Jan 2006 03:30:33.944 UTC,10.06\n37,08 Jan 2006 05:06:27.963 UTC,08 Jan 2006 05:08:37.245 UTC,2.15\n38,08 Jan 2006 22:30:44.135 UTC,08 Jan 2006 22:40:39.999 UTC,9.93\n39,09 Jan 2006 00:12:07.509 UTC,09 Jan 2006 00:23:40.048 UTC,11.54\n40,09 Jan 2006 01:54:33.280 UTC,09 Jan 2006 02:05:44.741 UTC,11.19\n41,09 Jan 2006 03:38:07.856 UTC,09 Jan 2006 03:46:25.144 UTC,8.29\n42,09 Jan 2006 21:06:43.890 UTC,09 Jan 2006 21:14:02.240 UTC,7.31\n43,09 Jan 2006 22:47:00.241 UTC,09 Jan 2006 22:57:57.101 UTC,10.95\n44,10 Jan 2006 00:28:55.441 UTC,10 Jan 2006 00:40:31.278 UTC,11.6\n45,10 Jan 2006 02:11:45.209 UTC,10 Jan 2006 02:22:07.428 UTC,10.37\n46,10 Jan 2006 03:56:43.679 UTC,10 Jan 2006 04:01:12.843 UTC,4.49\n47,10 Jan 2006 21:22:20.634 UTC,10 Jan 2006 21:31:52.357 UTC,9.53\n48,10 Jan 2006 23:03:33.465 UTC,10 Jan 2006 23:15:00.736 UTC,11.45"
			.split("\n");

	private static final DateTimeFormatter dtparser = DateTimeFormat
			.forPattern("DD MMM yyyy HH:mm:ss.SSS 'UTC'");

	@Test
	public void testRun() throws IOException {
		// Create test object
		// CtrlTcpProvider testee = new CtrlTcpProvider(new
		// GroundStation("HBO",new double[]{42.457222,-76.384444,579}, 0),
		// TEST_PORT);

		// Init XStream
		XStream x = new XStream();
		x.alias("GroundPassRequest", GroundPassRequest.class);
		x.alias("GroundPassResponse", GroundPassResponse.class);

		// Connect
		Socket s = new Socket("localhost", TEST_PORT);
		// s.setSoTimeout(100);
		OutputStream sout = s.getOutputStream();
		InputStream sin = s.getInputStream();
		sout.write(GTG.HELLO_BYTE);
		sout.flush();
		assertEquals(GTG.HELLO_BYTE, sin.read());

		// Calculate fake gp's
		sout.write(GTG.CTRL_CALC_GROUND_PASSES);
		sout.write(x.toXML(new GroundPassRequest("", "", "", 0, 0, true))
				.getBytes());
		sout.flush();
		GroundPassResponse passes = (GroundPassResponse) x.fromXML(sin);
		assertEquals(CtrlTcpProvider.TEST_PASSES, passes.upcomingPasses);

		// Calculate real gp's
		sout.write(GTG.CTRL_CALC_GROUND_PASSES);
		sout.write(x.toXML(
				new GroundPassRequest("CUSAT", CUSAT_TLE,
						"2006-01-01T00:00:00.0Z", 10.0, 1. / (60. * 60. * 24.),
						false)).getBytes());
		sout.flush();
		passes = (GroundPassResponse) x.fromXML(sin);
		System.out.println(passes.upcomingPasses);
		String[] upPasses = passes.upcomingPasses.split("\n");
		assertEquals(TEST_RESULT[0], upPasses[0]);
		for (int i = 1; i < upPasses.length; i++) {
			String[] expPassParts = TEST_RESULT[i].split(",");
			String[] actPassParts = upPasses[i].split(",");
			// Pass #
			assertEquals(expPassParts[0], actPassParts[0]);
			// Start Time
			DateTime expStart = dtparser.parseDateTime(expPassParts[1])
					.withZoneRetainFields(DateTimeZone.UTC);
			DateTime actStart = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(actPassParts[1]).withZone(DateTimeZone.UTC);
			assertTrue(
					String.format("%d) Expected <%s> but was <%s>", i,
							expStart.toString(), actStart.toString()),
					Math.abs(expStart.getMillis() - actStart.getMillis()) < Duration
							.standardSeconds(1).getMillis());
			// End time
			DateTime expEnd = dtparser.parseDateTime(expPassParts[2])
					.withZoneRetainFields(DateTimeZone.UTC);
			DateTime actEnd = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(actPassParts[2]).withZone(DateTimeZone.UTC);
			assertTrue(String.format("%d) Expected <%s> but was <%s>", i,
					expStart.toString(), actStart.toString()), Math.abs(expEnd
					.getMillis() - actEnd.getMillis()) < Duration
					.standardSeconds(1).getMillis());
			// Duration
			assertEquals(Double.parseDouble(expPassParts[3]),
					Double.parseDouble(actPassParts[3]), 0.01);
		}

		sout.write(GTG.GTG_DISCONNECT);
		sout.flush();
	}

}
