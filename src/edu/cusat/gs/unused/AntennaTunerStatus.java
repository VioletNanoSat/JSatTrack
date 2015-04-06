package edu.cusat.gs.unused;


/**
 * NOT FINSIHED - Not Needed?
 * @author David Bjanes
 *
 */
public class AntennaTunerStatus extends Ts2000Cmd{

    /**
     * Sets Antenna Tuning Status
     * 
     * @param p1
     *            - If '0': RX-AT THRU, '1': RX-AT IN
     * @param p2
     *            - If '0': TX-AT THRU, '1': TX-AT IN
     * @param p3
     *            - If '0': Stop Tuning (Set)/Tuning is stopped (Answer), '1':
     *            Start Tuning (Set)/Tuning is active (Answer), '2': Tuning
     *            cannot be completed
     * @return true if successful, false otherwise
     */
    public boolean setAntennaTunerStat(int p1, int p2, int p3) {
        if (binary(p1) && binary(p2)) {
            if (binary(p3)) {
                //sendSerialPort("AC" + p1 + p2 + p3 + ";");
                return true;
            } else {
                // throw an Exception for why tuning cannot be completed
                return false;
            }
        } else {
            // throws IOException
            return false;
        }
    }

    /**
     * Gets Antenna Tuning Status
     * 
     * @return String:('AC' + P1 + P2 + P3), P1 - If '0': RX-AT THRU, '1': RX-AT
     *         IN, P2 - If '0': TX-AT THRU, '1': TX-AT IN, P3 - If '0': Stop
     *         Tuning (Set)/Tuning is stopped (Answer), '1': Start Tuning
     *         (Set)/Tuning is active (Answer), '2': Tuning cannot be completed
     */
    public String readAntennaTunerStat() {
        //sendSerialPort("AC;");
        return "";//readSerialPort();
    }
}
