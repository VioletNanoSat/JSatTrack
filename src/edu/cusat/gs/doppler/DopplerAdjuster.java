package edu.cusat.gs.doppler;

import javax.swing.JTextField;

import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.utilities.Times;
import name.gano.astro.AER;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import edu.cusat.gs.ts2000.KenwoodTs2kController;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.MainVfo;

public class DopplerAdjuster {
	private double baseFrequency; // Satellite Original Frequency
	private AbstractSatellite curSat; // Satellite Currently Tracked
	private GroundStation curGS; // Ground Station
	private KenwoodTs2kController radio;
	private Doppler dop;
	private MainVfo transmitVFO; // Transmit Channel
	private MainVfo receiveVFO; // Receiving Channel
	private JTextField jTextFieldCurTXFreq;
	private JTextField jTextFieldCurRXFreq;

	public DopplerAdjuster(double baseFrequency, AbstractSatellite curSat,
			GroundStation curGS, KenwoodTs2kController radio,
			JTextField jTextFieldCurTXFreq, JTextField jTextFieldCurRXFreq,
			double currentJulianTime) {
		this.baseFrequency = baseFrequency;
		this.curSat = curSat;
		this.curGS = curGS;
		this.radio = radio;
		this.jTextFieldCurTXFreq = jTextFieldCurTXFreq;
		this.jTextFieldCurRXFreq = jTextFieldCurRXFreq;
		dop = new Doppler(50000, new Instant(Times.dateTimeOfMjd(
				currentJulianTime).toDateTime(DateTimeZone.UTC)));
		// arbitrary value for initial distance, will give the wrong shift for a "nano second"
	}

	public void setTransmitVFO(MainVfo vfo) {
		transmitVFO = vfo;
	}

	public void setReceiveVFO(MainVfo vfo) {
		receiveVFO = vfo;
	}

	public void setBaseFrequency(double freq) {
		baseFrequency = freq;
	}

	public void setCurrentSatellite(AbstractSatellite curSat) {
		this.curSat = curSat;
	}

	public void setCurrentGS(GroundStation gs) {
		curGS = gs;
	}

	private double getRange() {
		return AER.calculate(curGS.getLla_deg_m(), curSat.getTEMEPos(),
				curSat.currentJulDate())[2];
	}

	public void updateTime(double currentJulianTime) {
		double[] shiftFreq = { 0, 0 };

		if (curSat != null && curGS != null)
			shiftFreq = dop.getShift(baseFrequency, getRange(),
					new Instant(Times.dateTimeOfMjd(currentJulianTime)
							.toDateTime(DateTimeZone.UTC))); // Calculating the frequency shift
		else {
			// if not enough info to calculate the shifted frequency, assign base frequency
			shiftFreq[0] = baseFrequency;
			shiftFreq[1] = baseFrequency;
		}
		// Transmitter
		radio.setVFOFrequency(shiftFreq[0], transmitVFO); // Setting the new frequencies
		radio.setVFOFrequency(shiftFreq[1], receiveVFO);
		jTextFieldCurTXFreq.setText("" + ((Double) shiftFreq[0]).longValue()); // Displaying the frequencies
		jTextFieldCurRXFreq.setText("" + ((Double) shiftFreq[1]).longValue());

	}
}