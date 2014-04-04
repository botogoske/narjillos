package org.nusco.swimmer.physics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nusco.swimmer.physics.Angle;

public class AngleTest {
	@Test
	public void normalizesToPlus180DegreesToMinus179Degrees() {
		assertEquals(0.0, Angle.normalize(0), 0);
		assertEquals(90.0, Angle.normalize(90), 0);
		assertEquals(180.0, Angle.normalize(180), 0);
		
		assertEquals(-179.0, Angle.normalize(181), 0);
		assertEquals(-90.0, Angle.normalize(270), 0);
		assertEquals(-1.0, Angle.normalize(359), 0);

		assertEquals(1.0, Angle.normalize(361), 0);
		assertEquals(-1.0, Angle.normalize(-361), 0);
		assertEquals(180.0, Angle.normalize(540), 0);
		assertEquals(-179.0, Angle.normalize(-539), 0);
	}
}
