package org.nusco.swimmer.body;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nusco.swimmer.physics.Vector;

public class HeadTest extends VisibleOrganTest {

	@Override
	public Head createVisibleOrgan() {
		return new Head(20, THICKNESS, 100);
	}

	@Test
	public void startsAtPointZeroByDefault() {
		assertEquals(Vector.ZERO, organ.getStartPoint());
	}

	@Test
	public void canBeSetAtADifferentPoint() {
		((Head)organ).placeAt(Vector.cartesian(20, 30));
		assertEquals(Vector.cartesian(20, 30), organ.getStartPoint());
	}

	@Override
	public void hasAnEndPoint() {
		assertEquals(Vector.cartesian(20, 0), organ.getEndPoint());
	}

	@Override
	public void hasAParent() {
		assertEquals(null, organ.getParent());
	}

	@Test
	public void emitsASinusoidalSignalWhileTicking() {
		Head head = new Head(0, 0, 0);

		Vector[] expectedSignal = new Vector[] { Vector.cartesian(1.0000, 0),
												 Vector.cartesian(0.9980, 0),
												 Vector.cartesian(0.9921, 0),
												 Vector.cartesian(0.9823, 0),
												 Vector.cartesian(0.9686, 0),
												 Vector.cartesian(0.9511, 0),
												 Vector.cartesian(0.9298, 0)
												};

		for (int i = 0; i < expectedSignal.length; i++) {
			head.tick();
			Vector outputSignal = head.getNerve().readOutputSignal();
			assertEquals(expectedSignal[i].getX(), outputSignal.getX(), 0.0001);
			assertEquals(expectedSignal[i].getY(), outputSignal.getY(), 0.0001);
		}
	}
}