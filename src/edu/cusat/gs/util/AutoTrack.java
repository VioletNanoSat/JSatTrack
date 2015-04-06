package edu.cusat.gs.util;

import edu.cusat.gs.rotator.GroundPass;

import jsattrak.objects.GroundStation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jsattrak.objects.AbstractSatellite;

/**
 * 
 * AutoTrack automatically selects the current satellite being tracked in
 * GroundSystemsPanel based on availability of satellites above the horizon and
 * the calculated duration of the ground pass.
 * 
 * @author Adam Mendrela
 */
public class AutoTrack {
	private static final double ONE_SEC_IN_DAYS = 1 / (24. * 3600.);

	/** Static access only **/
	private AutoTrack() { }
	
	/**
	 * This method called every time the time has been updated, checks for any
	 * current ground passes and adjust the tracked satellite in
	 * GroundSystemsPanel
	 * <p>
	 * TODO when there are no satellites overhead, return the next one
	 * @param gs TODO
	 * @param currentJulTime
	 *            Current global time in JSatTrak
	 * @param satellites
	 *            Available satellites to check
	 * 
	 * @return The satellite to track, <code>null</code> if there are none 
	 */
	public static AbstractSatellite findOverheadSat(GroundStation gs,
			double currentJulTime, Collection<AbstractSatellite> satellites) {

		if(gs == null) return null;
		
		GroundPass curGP = getCurrentGroundPass(gs, satellites, currentJulTime);

		if (curGP == null) // there is no satellite pass in the upcoming second
			return null;
		else return curGP.getCurrentSatellite();
	}
    /**
     *Calculates the current the current satellite ground pass over the given
     * ground station
     * @param gs TODO
     * @param sats TODO
     * @param curJulTime TODO
     * @param t
     */
    private static GroundPass getCurrentGroundPass(GroundStation gs, Collection<AbstractSatellite> sats, double curJulTime)
    {
        ArrayList<GroundPass> gps = new ArrayList<GroundPass>();


        //Find the first upcoming GroundPass in next 1 for each satellite
		for (AbstractSatellite s : sats) {
			if (!"Sun".equals(s.getName())) // skips the sun
			{
				List<GroundPass> temp = GroundPass.find(gs, s);
				// if a GroundPass is found
				if (temp.size() != 0
						&& checkPassTime(temp.get(0), curJulTime, ONE_SEC_IN_DAYS*70))
					gps.add(temp.get(0));
			}
		}
		if (gps.size() == 0) {// if no upcoming GroundPasses for all satellites
			return null;
		}
        //find the longest GroundPass
        GroundPass longest = gps.get(0);
		for (GroundPass gp : gps) {
			if (gp.passDuration() > longest.passDuration())
				longest = gp;
		}
        return longest;
    }

	/**
	 * Checks if a given pass is happening or will happen in the upcoming
	 * <code>dt</code> days
	 * 
	 * @param gp
	 *            Fround pass to check
	 * @param curJulTime
	 *            Time to start checking from
	 * @param dt
	 *            Number of days to check
	 * @return whether the pass will happen in a given time period
	 */
	private static boolean checkPassTime(GroundPass gp, double curJulTime, double dt) {
		return gp.riseTime() < curJulTime + dt && curJulTime < gp.setTime();
	}
}
