package org.nusco.swimmer.body;

import org.nusco.swimmer.body.pns.DelayNerve;
import org.nusco.swimmer.physics.Vector;

class Segment extends Organ {
	private static final int DELAY = 13;

	public Segment(int length, int thickness, int relativeAngle, int rgb, Organ parent) {
		super(length, thickness, relativeAngle, rgb, new DelayNerve(DELAY), parent);
	}

	@Override
	public Organ getParent() {
		return parent;
	}

	@Override
	public Vector getStartPoint() {
		return parent.getEndPoint();
	}

	@Override
	public double getAngle() {
		double relativeAngle = getRelativeAngle() * getNerve().readOutputSignal().getX();
		return relativeAngle + getParent().getAngle();
	}
}
