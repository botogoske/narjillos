package org.nusco.swimmers.application;

import org.nusco.swimmers.creature.Swimmer;
import org.nusco.swimmers.creature.genetics.DNA;
import org.nusco.swimmers.pond.Pond;
import org.nusco.swimmers.shared.physics.Vector;
import org.nusco.swimmers.shared.things.Thing;

public class Cosmos extends Pond {

	public final static int SIZE = 100_000;
	private static final int INITIAL_NUMBER_OF_FOOD_THINGS = 100;
	private static final int FOOD_RESPAWN_AVERAGE_INTERVAL = 1000;
	private static final int INITIAL_NUMBER_OF_SWIMMERS = 200;

	private int tickCounter = 0;

	public Cosmos() {
		super(SIZE);
		randomize();
	}

	private void updateTargets() {
		for (Thing thing : getThings("swimmer"))
			updateTarget((Swimmer)thing);
	}

	private void randomize() {
		for (int i = 0; i < INITIAL_NUMBER_OF_FOOD_THINGS; i++)
			spawnFood(randomPosition());

		for (int i = 0; i < INITIAL_NUMBER_OF_SWIMMERS; i++)
			spawnSwimmer(randomPosition(), DNA.random());

		updateTargets();
	}

	@Override
	public void tick() {
		super.tick();

		if (Math.random() < 1.0 / FOOD_RESPAWN_AVERAGE_INTERVAL)
			spawnFood(randomPosition());

		if (tickCounter++ > 1000) {
			tickCounter = 0;
			updateTargets();
		}
	}

	protected Vector randomPosition() {
		double randomAngle = Math.random() * 360;
		double radius = getSize() / 2;
		double randomDistance = Math.random() * radius;
		return Vector.cartesian(radius, radius).plus(Vector.polar(randomAngle, randomDistance));
	}
}
