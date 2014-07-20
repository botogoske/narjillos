package org.nusco.swimmers.views;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import org.nusco.swimmers.creature.Narjillo;
import org.nusco.swimmers.creature.body.Organ;
import org.nusco.swimmers.shared.physics.Vector;

class OrganView {

	private final static int OVERLAP = 5;

	private final Organ organ;
	private final Narjillo swimmer;
	private final Color color;
	private final Rectangle rectangle;
	
	public OrganView(Organ organ, Narjillo swimmer) {
		this.organ = organ;
		this.swimmer = swimmer;
		color = toRGBColor(organ.getColor());
		rectangle = createRectangle();
	}

	private Rectangle createRectangle() {
		Rectangle result = new Rectangle(0, 0, organ.getLength() + (OVERLAP * 2), organ.getThickness());

		double arc = (organ.getLength() * organ.getThickness()) % 15 + 15;
		result.setArcWidth(arc);
		result.setArcHeight(arc);

		return result;
	}

	public Node toNode() {
		return toDetailView();
	}

	private Node toDetailView() {
		rectangle.setFill(getColor());

		rectangle.getTransforms().clear();
		
		// overlap slightly and shift to center based on thickness
		double widthCenter = organ.getThickness() / 2;
		rectangle.getTransforms().add(new Translate(-OVERLAP, -widthCenter));
		rectangle.getTransforms().add(moveToStartPoint());

		// rotate in position
		rectangle.getTransforms().add(new Rotate(organ.getAbsoluteAngle(), OVERLAP, widthCenter));

		return rectangle;
	}

	// TODO: Level Of Detail
//	private Node toHighDistanceView() {
//		if (organ.getLength() < 40 || organ.getThickness() < 20)
//			return null;
//		Line result = new Line(organ.getStartPoint().getX(), organ.getStartPoint().getY(), organ.getEndPoint().getX(), organ.getEndPoint().getX());
//		result.setStroke(Color.LIGHTGRAY);
//		result.setStrokeWidth(organ.getThickness());
//		return result;
//	}

	private Translate moveToStartPoint() {
		Vector startPoint = organ.getStartPoint();
		return new Translate(startPoint.x, startPoint.y);
	}

	private Color getColor() {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), getAlpha());
	}

	private double getAlpha() {
		return Math.min(0.7, swimmer.getEnergy() / 10_000);
	}

	public static Color toRGBColor(int rgbint) {
		byte rgbByte = (byte)rgbint;
		double MAX_VALUE_IN_3_BITS = 7.0;
		double red = (rgbByte & 0b00000111) / MAX_VALUE_IN_3_BITS;
		double green = ((rgbByte & 0b00111000) >> 3) / MAX_VALUE_IN_3_BITS;
		double blue = ((rgbByte & 0b11000000) >> 5) / MAX_VALUE_IN_3_BITS;
		return new Color(red, green, blue, 1);
	}
}