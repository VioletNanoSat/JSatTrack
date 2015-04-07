package edu.cusat.gs.rotator;

import edu.cusat.common.mvc.Model;

public class RotatorModel extends Model<RotatorModelListener>{
	public final Property<Double> az = new Property<Double>(0.);
	public final Property<Double> el = new Property<Double>(0.);
	public final Property<Boolean> connected = new Property<Boolean>(false);
}
