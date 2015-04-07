package edu.cusat.gs.ts2000;

import java.util.ArrayList;

public enum Command {
    TUNER_STATUS            ("AC","AC123"),
    AF_GAIN                 ("AG","AG1222"),
    AUTO_INFORMATION        ("AI","AI1"), 
    AUTO_MODES              ("AM","AM1"),
    BUSY_SIGNAL_STATUS      ("BY","BY12"), 
    CARRIER_GAIN            ("CG","CG111"), 
    STEP_FREQ               ("CH",""), 
    SET_FREQ_TO_CALL_FREQ   ("CI",""), 
    TX_DXER_STATUS          ("DC","DC12"), 
    EXTENSION_MENU          ("EX","EX11122345555555555555555"), //Maybe
    VFO_A_FREQ              ("FA","FA11111111111"), 
    VFO_B_FREQ              ("FB","FB11111111111"), 
    VFO_SUB_FREQ            ("FC","FC11111111111"), 
    RX_A_B                  ("FR","FR1"), // Can't send 0 or 1 when in sub ctrl mode
    FINE_FUNC_STATUS        ("FS","FS1"), 
    TX_A_B                  ("FT","FT1"), // Can't send 0 or 1 when in sub ctrl mode
    DSP_RECV_FILTER_WIDTH   ("FW","FW1111"), 
    TRANSCEIVER_ID          ("ID","ID111"),
    TRANSCEIVER_STATUS      ("IF","IF11111111111222233333345677890123445"), 
    MORSE                   ("KY","KY1"),
    KEY_LOCK                ("LK","LK12"),
    ALT_STATUS              ("LT","LT1"), 
    MEM_CHAN                ("MC","MC122"), 	// memory channel
    OPERATIONAL_MODE        ("MD","MD1"), 
    MENU_A_B                ("MF","MF1"), 
    READ_MC_DATA_1          ("MR","MR12334444444444456788990001233333333344566666666"), 
    WRITE_MC_DATA           ("MW",""), 
    NOISE_BLANKER_STATUS    ("NB","NB1"),
    NOISE_BLANKER_LEVEL     ("NL","NL111"),
    NOISE_REDUCTION_STATUS  ("NR","NR1"),
    AUTO_NOTCH_STATUS       ("NT","NT1"),
    OFFSET_FREQ             ("OF","OF111111111"), 
    READ_MC_DATA_2          ("OI","OI11111111111222233333345677890123445"), 
    OFFSET_FUNC_STATUS      ("OS","OS1"),
    PRE_AMP_STATUS          ("PA","PA12"),
    OUTPUT_PWR              ("PC","PC111"), 
    STORE_PROG_MEMORY       ("PI",""),
    PACKET_CLUSTER_DATA     ("PK","PK111111111112222222222223333333333333333333344444"),
    RECALL_PROG_MEMORY      ("PM","PM1"), 
    PWR_STATUS              ("PS","PS1"), //(DOES IT WORK WHEN RADIO'S OFF?)
    RF_GAIN_STATUS          ("RG","RG111"),
    NOISE_REDUCTION_LEVEL   ("RL","RL11"),
    METER                   ("RM","RM12222"),
    RIT_FUNC_STATUS         ("RT","RT1"), 
    RECV_FUNC_STATUS        ("RX","RX1"),
    SUB_TFW_STATUS          ("SB","SB1"),
    S_METER_STATUS          ("SM","SM12222"), //S-meter (Signal Strength Meter) IMPORTANT
    SQUELCH_LEVEL           ("SQ","SQ1222"),
    RESET_TRANSCEIVER       ("SR",""),
    INTERNAL_TNC_MODE       ("TC","TC12"),
    XIT_FUNC_STATUS         ("XT","XT1"),
    COMMAND_ERROR           ("?","?"),
    COMM_ERROR              ("E","E"),
    PROCESSING_NOT_COMPLETE ("O","O")
    ; 		

    private String cmd;
    
    private String response_format;
    
    private Command(String c, String respf){ cmd = c; response_format = respf; }
    
    public String responseFormat() { return response_format; }
    
    @Override
    public String toString() { return cmd; }

	/**
	 * Get the parameters from a TS-2000 response
	 * 
	 * @param response
	 *            the response directly from the TS-2000, but without the
	 *            trailing ';'. Must be at least 2 characters long (i.e. not an
	 *            error response)
	 * @param params
	 *            the parameter string encoded as in the TS-2000 manual
	 * @return An array of the parameters. The first element of the array is the
	 *         two character command, so that the first parameter is at index 1,
	 *         the second at index 2, etc
	 */
	public String[] getParameters(String response) {
		// Allocate worst-case size of the return array
		ArrayList<String> ret = new ArrayList<String>(response.length());
		// Add the command chars
		ret.add(response.substring(0, 2));

		// Walk through the parameter string
		for (int i = 2; i < response_format.length();) {
			char param = response_format.charAt(i);
			int paramend = i;

			// Search through the params string for the start of the next param
			for (; paramend < response_format.length()
					&& response_format.charAt(paramend) == param; paramend++);
			// Now add the range of characters of response from i-paramend to
			// the return array
			ret.add(response.substring(i, paramend));
			// Increment the outer loop to the beginning of the next parameter
			i = paramend;
		}
		return ret.toArray(new String[] {});
	}
}
