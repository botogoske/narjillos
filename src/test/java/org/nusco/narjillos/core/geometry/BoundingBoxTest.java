package org.nusco.narjillos.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

public class BoundingBoxTest {
	
	@Test
	public void definesTheBoundariesOfASegment() {
		BoundingBox boundingBox = createBoundingBox(10, 20, 30, 40);
		
		assertEquals(10, boundingBox.left, 0.0);
		assertEquals(30, boundingBox.right, 0.0);
		assertEquals(20, boundingBox.bottom, 0.0);
		assertEquals(40, boundingBox.top, 0.0);
	}

	@Test
	public void worksWithNegativeBoundaries() {
		BoundingBox boundingBox = createBoundingBox(-10, -20, 30, 40);
		
		assertEquals(-10, boundingBox.left, 0.0);
		assertEquals(30, boundingBox.right, 0.0);
		assertEquals(-20, boundingBox.bottom, 0.0);
		assertEquals(40, boundingBox.top, 0.0);
	}

	@Test
	public void worksWithSegmentsInAnyOrientation() {
		BoundingBox boundingBox = createBoundingBox(30, 40, -10, -20);
		
		assertEquals(-10, boundingBox.left, 0.0);
		assertEquals(30, boundingBox.right, 0.0);
		assertEquals(-20, boundingBox.bottom, 0.0);
		assertEquals(40, boundingBox.top, 0.0);
	}

	@Test
	public void isEmptyIfThereAreNoSegments() {
		BoundingBox boundingBox = new BoundingBox(new LinkedHashSet<SegmentShape>());
		
		assertEquals(0, boundingBox.left, 0.0);
		assertEquals(0, boundingBox.right, 0.0);
		assertEquals(0, boundingBox.bottom, 0.0);
		assertEquals(0, boundingBox.top, 0.0);
	}

	@Test
	public void overlapsWithAnotherBoundingBox() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(0, 9, 10, 10);
		
		assertTrue(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void doesNotOverlapIfItIsToTheRightOfTheOtherBoundingBox() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(11, 0, 10, 10);
		
		assertFalse(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void doesNotOverlapIfItIsToTheLeftOfTheOtherBoundingBox() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(-11, 0, 10, 10);
		
		assertFalse(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void doesNotOverlapIfItIsBelowTheOtherBoundingBox() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(0, 11, 10, 10);
		
		assertFalse(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void doesNotOverlapIfItIsAboveTheOtherBoundingBox() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(0, -11, 10, 10);
		
		assertFalse(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void overlapsItself() {
		BoundingBox boundingBox = createBoundingBox(0, 0, 10, 10);
		
		assertFalse(boundingBox.overlaps(boundingBox));
	}

	@Test
	public void doesNotOverlapIfItTouchesOnTheEdge() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(0, 10, 20, 20);
		
		assertFalse(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void neverOverlapsIfBothBoxesHaveAreaZero() {
		BoundingBox boundingBox = createBoundingBox(10, 10, 0, 0);
		
		assertFalse(boundingBox.overlaps(boundingBox));
	}

	@Test
	public void canStillOverlapIfTheFirstBoxHasAreaZero() {
		BoundingBox boundingBox1 = createBoundingBox(10, 10, 0, 0);
		BoundingBox boundingBox2 = createBoundingBox(-10, -10, 20, 20);
		
		assertTrue(boundingBox1.overlaps(boundingBox2));
	}

	@Test
	public void canStillOverlapIfTheSecondBoxHasAreaZero() {
		BoundingBox boundingBox1 = createBoundingBox(0, 0, 10, 10);
		BoundingBox boundingBox2 = createBoundingBox(5, 5, 0, 0);
		
		assertTrue(boundingBox1.overlaps(boundingBox2));
	}
	
	private BoundingBox createBoundingBox(int x, int y, int width, int height) {
		Set<SegmentShape> shapes = new LinkedHashSet<>();
		shapes.add(new SegmentShape() {
			@Override
			public Segment toSegment() {
				return new Segment(Vector.cartesian(x, y), Vector.cartesian(width, height));
			}
		});
		return new BoundingBox(shapes);
	}
}
