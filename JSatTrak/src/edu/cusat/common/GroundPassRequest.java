package edu.cusat.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class GroundPassRequest {

	public final String satName;
	public final String tle;
	public final String start;
	public final double duration;
	public final double timestep;
	public final boolean fake;
	
	public GroundPassRequest(String name, String tle, String start, double duration, double timestep, boolean fake){
		satName = name == null ? "" : name;
		this.tle = tle == null ? "" : tle;
		this.start = start;
		this.duration = duration;
		this.timestep = timestep;
		this.fake = fake;
	}
	
	public GroundPassRequest(String name, String tle, DateTime start, double duration, double timestep, boolean fake){
		this(name, tle, start.withZone(DateTimeZone.UTC).toString(), duration, timestep, fake);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(duration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((satName == null) ? 0 : satName.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((tle == null) ? 0 : tle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroundPassRequest other = (GroundPassRequest) obj;
		if (Double.doubleToLongBits(duration) != Double
				.doubleToLongBits(other.duration))
			return false;
		if (satName == null) {
			if (other.satName != null)
				return false;
		} else if (!satName.equals(other.satName))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (tle == null) {
			if (other.tle != null)
				return false;
		} else if (!tle.equals(other.tle))
			return false;
		return true;
	}
	
}
