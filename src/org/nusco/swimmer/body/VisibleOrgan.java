package org.nusco.swimmer.body;

import org.nusco.swimmer.body.pns.Nerve;
import org.nusco.swimmer.physics.Vector;

abstract class VisibleOrgan extends Organ {

	private final int length;
	private final int thickness;
	private final int rgb;
	private final double relativeAngle;

	protected VisibleOrgan(int length, int thickness, int relativeAngle, int rgb, Nerve neuron, Organ parent) {
		super(neuron, parent);
		this.length = length;
		this.thickness = thickness;
		this.relativeAngle = relativeAngle;
		this.rgb = rgb;
	}

	public int getLength() {
		return length;
	}

	public int getThickness() {
		return thickness;
	}

	public abstract Vector getStartPoint();

	@Override
	public Vector getEndPoint() {
		return getStartPoint().plus(Vector.polar(getAngle(), length));
	}

	public abstract double getAngle();

	@Override
	public abstract Organ getParent();

	@Override
	public Organ getAsParent() {
		return this;
	}

	@Override
	public double getRelativeAngle() {
		return relativeAngle;
	}

	public boolean isVisible() {
		return true;
	}

	public int getRGB() {
		return rgb;
	}

	@Override
	public void tick() {
	}
	
	@Override
	public String toString() {
		return "[" + length + "," + thickness + "," + getAngle() + "]";
	}
}