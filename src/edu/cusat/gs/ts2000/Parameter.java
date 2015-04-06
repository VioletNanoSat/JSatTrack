package edu.cusat.gs.ts2000;

public class Parameter {

	/** The name of the parameter (optional) **/
	private final String name;
	
	/** How many digits to put in command string **/
	private final int digits;
	
	/** The smallest allowed value **/
	private final int minValue;
	
	private final int maxValue;
	
	private final int value;

	/**
	 * Create a parameter
	 * @param name - optional name of the parameter
	 * @param digits number of digits
	 * @param minVal minimum allowed value
	 * @param maxVal maximum allowed value
	 * @param value TODO
	 */
	public Parameter(String name, int digits, int minVal, int maxVal, int value){
		this.name = (name == null) ? "" : name;
		this.digits = Math.max(0, digits);
		minValue = Math.max(minVal,0);
		maxValue = Math.min(maxVal, (int) Math.pow(10, this.digits+1));
		if(minValue <= value && value <= maxValue)
			this.value = value;
		else
			throw new IllegalArgumentException(value + " is out of range");
	}
	
	/**
	 * Create a new parameter with the same settings as the original, but a 
	 * new value
	 * 
	 * @param original - the original parameter whose setting to keep
	 * @param newValue - the value of the new parameter
	 */
	public Parameter(Parameter original, int newValue){
		name = original.name;
		digits = original.digits;
		minValue = original.minValue;
		maxValue = original.maxValue;
		if(minValue <= newValue && newValue <= maxValue)
			this.value = newValue;
		else
			throw new IllegalArgumentException(newValue + " is out of range");
	}
	
	/** 
	 * @param value the new value of the parameter
	 * @return
	 */
	public Parameter setValue(int value) {
		if(minValue <= value && value <= maxValue)
			return new Parameter(this, value);
		throw new IllegalArgumentException(value + " is out of range");
	}
	
	public String commandForm(){
		String format = String.format("%%0%dd", digits);
		return String.format(format, value);
	}
	
	@Override
	public String toString() {
		return String.format("%s=%d [digits=%d, min=%d, max=%d]", 
				name, value, digits, minValue, maxValue);
	}
}
